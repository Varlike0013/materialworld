package com.varlike.aetherui.api.hud;

import com.varlike.aetherui.api.paint.CanvasContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 可注册到 AetherUI 左下角 HUD 的自定义状态条。
 *
 * <p>其它 mod 实现此接口并调用 {@code AetherUI.registerHudBar(bar)}，
 * 即可把自己的条渲染在饥饿条下方。条按注册顺序从上到下排列。
 *
 * <p>绘制坐标相对本条左上角，单位为逻辑像素。
 * 内置条宽度为 142，建议自定义条宽度不超过此值。
 */
public interface HudBar {

    /** 唯一标识，用于去重和注销。 */
    ResourceLocation id();

    /** 逻辑宽度，默认与内置条一致。 */
    default float width() { return 142f; }

    /** 逻辑高度，默认与内置条一致。 */
    default float height() { return 12f; }

    /** 是否渲染该条（例如魔力为 0 时可隐藏）。 */
    default boolean visible(LocalPlayer player) { return true; }

    /**
     * 绘制本条。背景框也由实现方自行绘制，以便自定义样式。
     *
     * @param ctx    已对齐到本条左上角的画布上下文
     * @param player 当前客户端玩家
     */
    void paint(CanvasContext ctx, LocalPlayer player);
}
