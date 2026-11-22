package com.varlike.aetherui.api.hud;

import com.varlike.aetherui.api.paint.CanvasContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 物品栏上方 HUD 中的一个 buff 图标。其它 mod 可实现此接口并注册，
 * 图标按注册顺序从下到上、每列从左到右排列。
 *
 * <p>为常见情况提供静态工厂：
 * <ul>
 *   <li>{@link #texture(ResourceLocation, ResourceLocation, String)} — 整张纹理图片</li>
 *   <li>{@link #sprite(ResourceLocation, ResourceLocation, String)} — 图集 sprite</li>
 *   <li>{@link #custom(ResourceLocation, int, BuffPainter)} — 自定义绘制（支持任意类型）</li>
 * </ul>
 */
public interface BuffIcon {

    ResourceLocation id();

    /** 显示名称（可用于 tooltip，当前版本仅作标识）。 */
    String label();

    default float width()  { return 24f; }
    default float height() { return 24f; }

    /** 是否显示该图标。 */
    default boolean visible(LocalPlayer player) { return true; }

    /** 绘制到已对齐本图标左上角的画布。 */
    void paint(CanvasContext ctx, LocalPlayer player);

    /** 自定义 buff 绘制器，支持任意类型。 */
    @FunctionalInterface
    interface BuffPainter {
        void paint(CanvasContext ctx, LocalPlayer player);
    }

    static BuffIcon texture(ResourceLocation id, ResourceLocation texture, String label) {
        return new BuffIcon() {
            @Override public ResourceLocation id() { return id; }
            @Override public String label() { return label; }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) {
                ctx.blit(texture, 0f, 0f, width(), height(), 0f, 0f, 1f, 1f, 0xFFFFFFFF);
            }
        };
    }

    static BuffIcon sprite(ResourceLocation id, ResourceLocation sprite, String label) {
        return new BuffIcon() {
            @Override public ResourceLocation id() { return id; }
            @Override public String label() { return label; }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) {
                ctx.blitSprite(sprite, 0f, 0f, width(), height());
            }
        };
    }

    static BuffIcon custom(ResourceLocation id, String label, BuffPainter painter) {
        return new BuffIcon() {
            @Override public ResourceLocation id() { return id; }
            @Override public String label() { return label; }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) { painter.paint(ctx, player); }
        };
    }
}
