package com.varlike.aetherui.demo;

import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.Spin;
import com.varlike.aetherui.api.hud.WorldIcon;
import com.varlike.aetherui.api.target.Orientation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * 演示 WorldIcon：在方块上方画"边框 + 背景 + 图片"，并让位置随时间上下浮动、
 * 图标随时间转圈（模拟熔炼产物悬浮展示）。
 *
 * <p>真实场景把 {@code DEMO_TEXTURE} 换成你的产物 PNG（如
 * {@code ResourceLocation.fromNamespaceAndPath("mymod", "textures/item/mana.png")}），
 * 把 {@code basePos} 设为熔炉坐标上方即可。
 */
public final class SmeltingIconDemo {

    // 用原版 16x16 独立纹理做演示；实际请换成产物 PNG
    private static final ResourceLocation DEMO_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/dirt.png");

    private static UITree tree;

    private SmeltingIconDemo() {}

    public static void show(Vec3 blockPos) {
        if (tree != null) return;
        Vec3 base = blockPos.add(0.5, 1.6, 0.5);

        tree = WorldIcon.show(
                base,
                DEMO_TEXTURE,
                32f,
                Orientation.CENTER,  // 始终面向玩家；如需固定世界方向可用 Orientation.fixed(yaw, pitch)
                // 位置：随时间上下浮动（正弦），基准位置上方
                (t, p) -> p.add(0, 0.12f * (float) Math.sin(t * 3f), 0),
                // 多轴自旋：绕 Y 轴慢翻 + 绕 Z 轴转圈（可单独用 Spin.yaw / Spin.pitch / Spin.roll）
                Spin.of(
                        t -> t * 20f,   // yaw：绕垂直轴
                        null,           // pitch：不转
                        t -> t * 60f    // roll：屏幕平面转圈
                )
        );
    }

    public static void destroy() {
        if (tree == null) return;
        com.varlike.aetherui.api.AetherUI.destroy(tree);
        tree = null;
    }

    public static boolean isShowing() { return tree != null; }
}
