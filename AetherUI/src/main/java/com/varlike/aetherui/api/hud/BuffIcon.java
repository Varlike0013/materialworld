package com.varlike.aetherui.api.hud;

import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;

/**
 * 物品栏上方 HUD 中的一个 buff 图标。其它 mod 可实现此接口并注册，
 * 图标按注册顺序从下到上、每列从左到右排列。
 *
 * <p>为常见情况提供静态工厂：
 * <ul>
 *   <li>{@link #texture(ResourceLocation, ResourceLocation, String)} — 整张纹理图片</li>
 *   <li>{@link #sprite(ResourceLocation, ResourceLocation, String)} — 图集 sprite</li>
 *   <li>{@link #custom(ResourceLocation, String, BuffPainter)} — 自定义绘制（支持任意类型）</li>
 *   <li>{@link #textBadge(ResourceLocation, String, String)} — 17×17 文字徽章（如“好”）</li>
 *   <li>{@link #pngBadge(ResourceLocation, String, ResourceLocation)} — 17×17 PNG 徽章（16×16 内容 + 1px 边框）</li>
 * </ul>
 */
public interface BuffIcon {

    /** 徽章图标整体尺寸：16×16 内容 + 1px 边框。 */
    int BADGE_SIZE = 17;
    int BADGE_CONTENT = 16;
    float BADGE_BORDER = 1f;

    ResourceLocation id();

    /** 显示名称（可用于 tooltip，当前版本仅作标识）。 */
    String label();

    default float width()  { return 24f; }
    default float height() { return 24f; }

    /** 是否显示该图标。 */
    default boolean visible(LocalPlayer player) { return true; }

    /**
     * 图标层数，BuffHud 会把它转换为罗马数字并画在图标右下角。
     * 返回 {@code null} 或小于 1 表示不绘制层数；1 显示为 I，3 显示为 III。
     */
    default Integer level(LocalPlayer player) { return null; }

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

    /** 17×17 文字徽章：深色背景 + 1px 边框 + 居中的文字内容（如“好”）。 */
    static BuffIcon textBadge(ResourceLocation id, String label, String text) {
        return new BuffIcon() {
            @Override public ResourceLocation id() { return id; }
            @Override public String label() { return label; }
            @Override public float width()  { return BADGE_SIZE; }
            @Override public float height() { return BADGE_SIZE; }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) {
                drawBadgeBackground(ctx);
                Component component = Component.literal(text != null ? text : "");
                Font font = Minecraft.getInstance().font;
                float x = BADGE_BORDER + Math.max(0f, (BADGE_CONTENT - font.width(component)) * 0.5f);
                float y = BADGE_BORDER + Math.max(0f, (BADGE_CONTENT - font.lineHeight) * 0.5f);
                ctx.drawString(component, x, y, 0xFFFFFFFF, false);
                drawBadgeBorder(ctx);
            }
        };
    }

    /** 同上，并附加固定层数；层数会由 BuffHud 转成罗马数字。 */
    static BuffIcon textBadge(ResourceLocation id, String label, String text, int level) {
        return withLevel(textBadge(id, label, text), level);
    }

    /** 17×17 PNG 徽章：16×16 PNG 铺满内容区，外圈 1px 边框。 */
    static BuffIcon pngBadge(ResourceLocation id, String label, ResourceLocation texture) {
        return new BuffIcon() {
            @Override public ResourceLocation id() { return id; }
            @Override public String label() { return label; }
            @Override public float width()  { return BADGE_SIZE; }
            @Override public float height() { return BADGE_SIZE; }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) {
                drawBadgeBackground(ctx);
                if (texture != null) {
                    ctx.blit(texture, BADGE_BORDER, BADGE_BORDER,
                            BADGE_CONTENT, BADGE_CONTENT,
                            0f, 0f, 1f, 1f, CanvasStyles.WHITE);
                }
                drawBadgeBorder(ctx);
            }
        };
    }

    /** 同上，并附加固定层数。 */
    static BuffIcon pngBadge(ResourceLocation id, String label, ResourceLocation texture, int level) {
        return withLevel(pngBadge(id, label, texture), level);
    }

    private static void drawBadgeBackground(CanvasContext ctx) {
        ctx.fill(0f, 0f, BADGE_SIZE, BADGE_SIZE, CanvasStyles.DARK_BG);
    }

    private static void drawBadgeBorder(CanvasContext ctx) {
        CanvasStyles.border(ctx, 0f, 0f, BADGE_SIZE, BADGE_SIZE,
                CanvasStyles.LIGHT_BORDER, BADGE_BORDER);
    }

    /** 给任意图标附加固定层数；不调用则默认不绘制层数。 */
    static BuffIcon withLevel(BuffIcon base, int level) {
        return withLevel(base, player -> level);
    }

    /** 给任意图标附加每帧求值的层数。 */
    static BuffIcon withLevel(BuffIcon base, IntSupplier level) {
        Objects.requireNonNull(level, "level");
        return withLevel(base, player -> level.getAsInt());
    }

    /** 给任意图标附加按玩家求值的层数；返回 null 表示不绘制。 */
    static BuffIcon withLevel(BuffIcon base, Function<LocalPlayer, Integer> level) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(level, "level");
        return new BuffIcon() {
            @Override public ResourceLocation id() { return base.id(); }
            @Override public String label() { return base.label(); }
            @Override public float width()  { return base.width(); }
            @Override public float height() { return base.height(); }
            @Override public boolean visible(LocalPlayer player) { return base.visible(player); }
            @Override public void paint(CanvasContext ctx, LocalPlayer player) { base.paint(ctx, player); }
            @Override public Integer level(LocalPlayer player) { return level.apply(player); }
        };
    }
}
