package com.varlike.spiritworld.client.skill;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.Anchor;
import com.varlike.aetherui.api.target.Orientation;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import com.varlike.spiritworld.ClientConfig;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientScanData;
import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 精神扫描的世界标记渲染：基于 AetherUI 的 WorldRenderTarget。
 *
 * <p>相比原来的手写 3D 渲染，这里把世界坐标直接投影到屏幕，
 * 不再混用相机空间平移与朝向四元数，避免部分视角下图标飞出视野的问题。
 * 服务端最大扫描半径为 48 格；渲染距离留出方块质心与抬升的余量，避免边缘矿物被裁掉。
 *
 * <p>AetherUI 默认按屏幕投影计算缩放，会让图标在正对视线和斜看时大小不同。
 * 这里每帧按视线夹角反补偿 {@code pixelsPerBlock}，使最终缩放只取决于距离；
 * 再按客户端配置把缩放钳制在最小/最大尺寸之间，距离很远的图标不会小于设定下限。
 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public final class SpiritScanRenderer {

    /** 略大于服务端最大扫描半径 48，覆盖方块质心与图标抬升的偏移。 */
    private static final float MAX_DISTANCE = 56f;
    private static final float ICON_SIZE = 40f;
    private static final float RISE = 1.6f;
    private static final float BASE_PIXELS_PER_BLOCK = 48f;

    private record IconEntry(UITree tree, WorldRenderTarget target, Vec3 base) {}

    private static final Map<String, IconEntry> ICONS = new HashMap<>();
    private static ResourceKey<Level> lastDimension;

    private SpiritScanRenderer() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearIcons();
            return;
        }
        if (!com.varlike.aetherui.Config.worldUIEnabled()) {
            clearIcons();
            return;
        }
        if (!mc.level.dimension().equals(lastDimension)) {
            lastDimension = mc.level.dimension();
            clearIcons();
        }

        List<OreIcon> icons = ClientScanData.getActiveIcons();
        Set<String> alive = new HashSet<>();
        Vec3 playerPos = mc.player.position();

        for (OreIcon icon : icons) {
            Item item = icon.iconItem();
            if (item == null || item == Items.AIR) continue;

            Vec3 base = new Vec3(icon.x(), icon.y(), icon.z()).add(0, RISE, 0);
            if (base.distanceTo(playerPos) > MAX_DISTANCE) continue;

            String key = keyOf(icon);
            alive.add(key);
            float ppb = compensatedPixelsPerBlock(base, mc);
            IconEntry entry = ICONS.get(key);
            if (entry == null) {
                ICONS.put(key, createIcon(base, new ItemStack(item), icon.count(), ppb));
            } else {
                entry.target().pixelsPerBlock(ppb);
            }
        }

        ICONS.entrySet().removeIf(entry -> {
            if (alive.contains(entry.getKey())) return false;
            AetherUI.destroy(entry.getValue().tree());
            return true;
        });
    }

    private static String keyOf(OreIcon icon) {
        return String.format("%.2f/%.2f/%.2f", icon.x(), icon.y(), icon.z());
    }

    /**
     * AetherUI 用世界单位块的屏幕投影求缩放，正对视线与斜看时大小不同。
     * 这里按视线夹角反补偿 pixelsPerBlock，使最终缩放只随距离衰减，
     * 并按客户端配置钳制到最小/最大尺寸之间。
     */
    private static float compensatedPixelsPerBlock(Vec3 base, Minecraft mc) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) return BASE_PIXELS_PER_BLOCK;

        Vec3 toIcon = base.subtract(cameraEntity.getEyePosition());
        double dist = toIcon.length();
        if (dist < 1.0E-4) return BASE_PIXELS_PER_BLOCK;

        double cos = Math.max(0.05, toIcon.scale(1.0 / dist).dot(cameraEntity.getLookAngle()));
        double blockPx = guiFocalLength(mc) / (dist * cos);

        float minScale = Math.min(ClientConfig.scanIconMinScale, ClientConfig.scanIconMaxScale);
        float maxScale = Math.max(ClientConfig.scanIconMinScale, ClientConfig.scanIconMaxScale);
        double distanceScale = guiFocalLength(mc) / (dist * BASE_PIXELS_PER_BLOCK);
        double desiredScale = Math.max(minScale, Math.min(maxScale, distanceScale));

        return (float) (blockPx / Math.max(0.01, desiredScale));
    }

    /** AetherUI 投影使用的 GUI 像素焦距：guiHeight / (2 * tan(fovY/2))。 */
    private static double guiFocalLength(Minecraft mc) {
        double fov = mc.options.fov().get();
        double half = Math.toRadians(fov) * 0.5;
        return mc.getWindow().getGuiScaledHeight() / (2.0 * Math.tan(half));
    }

    private static IconEntry createIcon(Vec3 base, ItemStack stack, int count, float pixelsPerBlock) {
        final long birth = System.currentTimeMillis();

        UIComponent component = UIBuilder.canvas(Size.fixed(ICON_SIZE), Size.fixed(ICON_SIZE))
                .canvasAnchor(Alignment.CENTER)
                .painter(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return;

                    CanvasStyles.panel(ctx, 0f, 0f, ICON_SIZE, ICON_SIZE);
                    ctx.renderItem(stack, (ICON_SIZE - 16f) * 0.5f, (ICON_SIZE - 16f) * 0.5f);

                    String label = count + "块  "
                            + String.format("%.1f米", base.distanceTo(mc.player.position()));
                    int tw = mc.font.width(label);
                    ctx.drawString(Component.literal(label), (ICON_SIZE - tw) * 0.5f,
                            ICON_SIZE - 10f, 0xFFFFFFFF, true);
                })
                .build();

        WorldRenderTarget target = new WorldRenderTarget(Anchor.dynamic(() -> {
            float t = (System.currentTimeMillis() - birth) / 1000f;
            return base.add(0, 0.12f * (float) Math.sin(t * 3f), 0);
        }))
                .orientation(Orientation.CENTER)
                .alignment(Alignment.CENTER)
                .pixelsPerBlock(pixelsPerBlock)
                .seeThrough(true)
                .maxRenderDistance(MAX_DISTANCE);

        return new IconEntry(AetherUI.register(AetherUI.build(component, target)), target, base);
    }

    private static void clearIcons() {
        for (IconEntry entry : ICONS.values()) {
            AetherUI.destroy(entry.tree());
        }
        ICONS.clear();
    }
}
