package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
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
import com.varlike.aetherui.network.ClientNetwork;
import com.varlike.aetherui.network.InspectCache;
import com.varlike.aetherui.network.InspectPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 自动为玩家附近“正在燃烧且产物槽非空”的熔炉显示悬浮产物图标。
 *
 * <p>数据来自服务端查询（客户端 BlockEntity 的容器内容通常不可靠），
 * 每个活跃熔炉一棵独立世界 UI，每帧绘制产物槽物品并做上下浮动与 yaw/roll 自旋。
 */
public final class SmeltingIconDemo {

    private static final float SIZE = 40f;
    private static final float RADIUS = 12f;
    private static final long REQUEST_INTERVAL_MS = 500L;

    private static boolean enabled;
    private static long lastRequestMs = Long.MIN_VALUE;
    private static ResourceKey<Level> lastDimension;
    private static final Map<BlockPos, UITree> ICONS = new HashMap<>();

    private SmeltingIconDemo() {}

    /** 开启动态演示：无需指定坐标，自动跟随附近熔炉。 */
    public static void show() {
        enabled = true;
    }

    /** 关闭演示并释放所有图标树。 */
    public static void destroy() {
        enabled = false;
        clearIcons();
    }

    public static boolean isShowing() {
        return enabled;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearIcons();
            return;
        }
        if (!Config.worldUIEnabled()) {
            clearIcons();
            return;
        }

        if (!mc.level.dimension().equals(lastDimension)) {
            lastDimension = mc.level.dimension();
            InspectCache.clear();
            clearIcons();
        }

        long now = System.currentTimeMillis();
        if (now - lastRequestMs >= REQUEST_INTERVAL_MS) {
            lastRequestMs = now;
            ClientNetwork.request(null, mc.player.blockPosition());
        }

        Set<BlockPos> active = new HashSet<>();
        for (InspectPayloads.FurnaceInfo info : InspectCache.furnaces()) {
            if (!info.lit() || info.output().isEmpty()) continue;

            Vec3 center = new Vec3(info.pos().getX() + 0.5, info.pos().getY() + 0.5, info.pos().getZ() + 0.5);
            if (center.distanceTo(mc.player.position()) > RADIUS) continue;

            active.add(info.pos());
            ICONS.computeIfAbsent(info.pos(), SmeltingIconDemo::createIcon);
        }

        ICONS.entrySet().removeIf(entry -> {
            if (active.contains(entry.getKey())) return false;
            AetherUI.destroy(entry.getValue());
            return true;
        });
    }

    private static UITree createIcon(BlockPos pos) {
        final long birth = System.currentTimeMillis();
        final float size = SIZE;
        final Vec3 base = new Vec3(pos.getX() + 0.5, pos.getY() + 1.35, pos.getZ() + 0.5);

        UIComponent component = UIBuilder.canvas(Size.fixed(size), Size.fixed(size))
                .canvasAnchor(Alignment.CENTER)
                .painter(ctx -> {
                    InspectPayloads.FurnaceInfo info = InspectCache.furnace(pos);
                    if (info == null || !info.lit() || info.output().isEmpty()) return;

                    CanvasStyles.panel(ctx, 0f, 0f, size, size);
                    ItemStack output = info.output();
                    ctx.renderItem(output, (size - 16f) * 0.5f, (size - 16f) * 0.5f);
                })
                .build();

        WorldRenderTarget target = new WorldRenderTarget(Anchor.dynamic(() -> {
            float t = (System.currentTimeMillis() - birth) / 1000f;
            return base.add(0, 0.12f * (float) Math.sin(t * 3f), 0);
        }))
                .orientation(Orientation.CENTER)
                .alignment(Alignment.CENTER)
                .pixelsPerBlock(64f)
                .seeThrough(true)
                .maxRenderDistance(32f)
                .yawDegrees(() -> (System.currentTimeMillis() - birth) * 0.020f)
                .rollDegrees(() -> (System.currentTimeMillis() - birth) * 0.060f);

        return AetherUI.register(AetherUI.build(component, target));
    }

    private static void clearIcons() {
        for (UITree tree : ICONS.values()) {
            AetherUI.destroy(tree);
        }
        ICONS.clear();
    }
}
