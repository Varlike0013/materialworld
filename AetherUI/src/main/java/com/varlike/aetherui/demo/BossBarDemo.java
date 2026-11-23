package com.varlike.aetherui.demo;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.DualTree;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.Anchor;
import com.varlike.aetherui.api.target.Orientation;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * 演示"一次声明，两处绘制"：同一个 UIComponent 同时注册到
 * 屏幕顶部 HUD 与玩家视野前方（世界空间），共享同一套样式与动态数据。
 *
 * <p>真实场景中把 {@code Anchor.PlayerRelative} 换成
 * {@code new Anchor.Entity(bossUUID, new Vec3(0, bossHeight + 0.5f, 0))}
 * 即可让世界血条跟随指定 Boss 头顶；HUD 部分保持不变。
 */
public final class BossBarDemo {

    private static DualTree dual;

    private BossBarDemo() {}

    public static void show() {
        if (dual != null) return;

        // 样式只声明一次：名字 + 血条 + 数值，血量随时间演示动态变化
        UIComponent bossBar = UIBuilder.canvas(Size.fixed(180f), Size.fixed(38f))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(ctx -> {
                    float ratio = 0.35f + 0.65f * (0.5f + 0.5f *
                            (float) Math.sin(System.currentTimeMillis() / 1200.0));
                    CanvasStyles.panel(ctx, 0f, 0f, ctx.width(), ctx.height());
                    ctx.drawString(Component.literal("演示 Boss"),
                            4f, 3f, 0xFFFF5555, false);
                    CanvasStyles.bar(ctx, 4f, 16f, ctx.width() - 8f, 14f, ratio,
                            0xFFE03333,
                            Component.literal("HP " + Math.round(ratio * 1000) + "/1000"));
                })
                .build();

        dual = AetherUI.buildAndRegisterBoth(
                bossBar,
                new ScreenRenderTarget(ScreenAnchor.percent(0.5f, 0f))
                        .alignment(Alignment.TOP_CENTER),
                // 位置：表达式绑定（每帧计算玩家前方 3 格、轻微上下浮动）
                // 朝向：表达式绑定（在面向玩家 / 竖直朝向间切换，演示两种模式）
                new WorldRenderTarget(Anchor.dynamic(() -> {
                    net.minecraft.client.player.LocalPlayer p =
                            net.minecraft.client.Minecraft.getInstance().player;
                    if (p == null) return new Vec3(0, 0, 0);
                    float bob = 0.1f * (float) Math.sin(System.currentTimeMillis() / 400.0);
                    Vec3 look = p.getLookAngle();
                    return p.position().add(0, p.getEyeHeight() + 0.6f + bob, 0)
                            .add(look.x * 3f, 0, look.z * 3f);
                }))
                        .orientation(() -> (System.currentTimeMillis() / 3000) % 2 == 0
                                ? Orientation.CENTER : Orientation.VERTICAL)
                        .alignment(Alignment.BOTTOM_CENTER)
                        .pixelsPerBlock(64f)
                        .maxRenderDistance(32f)
        );
    }

    public static void destroy() {
        if (dual == null) return;
        dual.destroy();
        dual = null;
    }

    public static boolean isShowing() { return dual != null; }
}
