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
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientScanData;
import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public final class SpiritScanRenderer {

    /** 略大于服务端最大扫描半径 48，覆盖方块质心与图标抬升的偏移。 */
    private static final float MAX_DISTANCE = 56f;
    private static final float ICON_SIZE = 40f;
    private static final float RISE = 1.6f;

    private static final Map<String, UITree> ICONS = new HashMap<>();
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
            ICONS.computeIfAbsent(key, k -> createIcon(base, new ItemStack(item), icon.count()));
        }

        ICONS.entrySet().removeIf(entry -> {
            if (alive.contains(entry.getKey())) return false;
            AetherUI.destroy(entry.getValue());
            return true;
        });
    }

    private static String keyOf(OreIcon icon) {
        return String.format("%.2f/%.2f/%.2f", icon.x(), icon.y(), icon.z());
    }

    private static UITree createIcon(Vec3 base, ItemStack stack, int count) {
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
                .pixelsPerBlock(48f)
                .seeThrough(true)
                .maxRenderDistance(MAX_DISTANCE);

        return AetherUI.register(AetherUI.build(component, target));
    }

    private static void clearIcons() {
        for (UITree tree : ICONS.values()) {
            AetherUI.destroy(tree);
        }
        ICONS.clear();
    }
}
