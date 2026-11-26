package com.varlike.aetherui.api.paint;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 可复用的绘制样式集合，全部只依赖 {@link CanvasContext} 原语，
 * 因此在屏幕空间（2D）与世界空间（3D）后端都可用。
 *
 * <p>典型用途：
 * <ul>
 *     <li>{@link #panel(CanvasContext, float, float, float, float)}：半透明黑底 + 白灰边框的信息面板；</li>
 *     <li>{@link #framedImage(CanvasContext, ResourceLocation, float, float, float)}：带边框的 PNG 图标；</li>
 *     <li>{@link #bar(CanvasContext, float, float, float, float, float, int)}：带底框和标签的进度条。</li>
 * </ul>
 *
 * <p>注意：{@link CanvasContext#fill(float, float, float, float, int)} 的第 3、4 个参数是
 * <b>宽和高</b>，不是右下角坐标。尺寸单位均为逻辑像素。
 */
public final class CanvasStyles {

    /** 半透明灰白色边框（信息面板 / 图标外框）。 */
    public static final int LIGHT_BORDER = 0x99D0D0D0;
    /** 半透明黑色底板（约 44% 不透明）。 */
    public static final int PANEL_BG = 0x70000000;
    /** 深色半透明底板（HUD 槽位）。 */
    public static final int DARK_BG = 0xB0080808;
    /** 实心灰色边框（HUD 槽位）。 */
    public static final int DARK_BORDER = 0xFF5A5A5A;
    /** 不透明的白色（tint 默认值，表示不改变原图颜色）。 */
    public static final int WHITE = 0xFFFFFFFF;
    /** 数值方块内的文字颜色（浅灰）。 */
    public static final int VALUE_TEXT_COLOR = 0xFFC0C0C0;
    /** 铁锭/护甲数值常用的银白色。 */
    public static final int IRON_SILVER = 0xFFE8E8E8;

    private static final float DEFAULT_BORDER = 1f;
    private static final float BAR_PAD = 2f;

    private CanvasStyles() {}

    // ---------------- 边框与面板 ----------------

    /**
     * 绘制四边等宽的矩形边框，不填充内部。
     *
     * @param t 边框宽度；小于等于 0 时不绘制
     */
    public static void border(CanvasContext ctx, float x, float y, float w, float h,
                              int argb, float t) {
        if (t <= 0f || w <= 0f || h <= 0f) return;
        float line = Math.min(t, Math.min(w, h) / 2f);
        ctx.fill(x, y, w, line, argb);
        ctx.fill(x, y + h - line, w, line, argb);
        ctx.fill(x, y, line, h, argb);
        ctx.fill(x + w - line, y, line, h, argb);
    }

    /**
     * 绘制半透明黑底 + 1px 灰白边框的信息面板（Demo 瞄准信息 HUD 同款样式）。
     */
    public static void panel(CanvasContext ctx, float x, float y, float w, float h) {
        panel(ctx, x, y, w, h, PANEL_BG, LIGHT_BORDER, DEFAULT_BORDER);
    }

    /** 使用自定义底板色与边框色的面板。传 bg = 0 可只画边框。 */
    public static void panel(CanvasContext ctx, float x, float y, float w, float h,
                             int bg, int borderArgb, float borderWidth) {
        if (w <= 0f || h <= 0f) return;
        if (bg != 0) ctx.fill(x, y, w, h, bg);
        border(ctx, x, y, w, h, borderArgb, borderWidth);
    }

    /** 绘制深色底板 + 1px 实心灰边框的 HUD 槽位（Demo 状态 HUD 同款样式）。 */
    public static void darkSlot(CanvasContext ctx, float x, float y, float w, float h) {
        panel(ctx, x, y, w, h, DARK_BG, DARK_BORDER, DEFAULT_BORDER);
    }

    // ---------------- 带边框的图片 ----------------

    /**
     * 绘制一张 PNG 图标并自动套上半透明白灰边框。整体尺寸为 {@code size} × {@code size}，
     * 边框宽 1px，图片等比铺满边框内的剩余区域。开发者只需提供纹理与尺寸，
     * 位置交给 UI 树（{@code UIBuilder.framedIcon(...)} 或直接放进 CanvasPainter）决定。
     */
    public static void framedImage(CanvasContext ctx, ResourceLocation texture,
                                   float x, float y, float size) {
        framedImage(ctx, texture, x, y, size, size, DEFAULT_BORDER, PANEL_BG, LIGHT_BORDER);
    }

    /** 同上，可指定边框宽度。整体尺寸仍为 size × size。 */
    public static void framedImage(CanvasContext ctx, ResourceLocation texture,
                                   float x, float y, float size, float borderWidth) {
        framedImage(ctx, texture, x, y, size, size, borderWidth, PANEL_BG, LIGHT_BORDER);
    }

    /**
     * 完全可定制的带框图片。
     *
     * @param w/h         整体外框宽高（含边框）
     * @param borderWidth 边框宽度；图片绘制在外框内侧，缩放填满剩余区域
     * @param bg          底板色，传 0 则无底板
     * @param borderArgb  边框颜色
     */
    public static void framedImage(CanvasContext ctx, ResourceLocation texture,
                                   float x, float y, float w, float h, float borderWidth,
                                   int bg, int borderArgb) {
        if (w <= 0f || h <= 0f) return;
        float b = Math.max(0f, Math.min(borderWidth, Math.min(w, h) / 2f));
        if (bg != 0) ctx.fill(x, y, w, h, bg);
        if (texture != null) {
            ctx.blit(texture,
                    x + b, y + b, w - 2f * b, h - 2f * b,
                    0f, 0f, 1f, 1f, WHITE);
        }
        border(ctx, x, y, w, h, borderArgb, b);
    }

    // ---------------- 进度条（两种可选样式） ----------------
    //
    // 两个变体都是可选工具，不影响自定义绘制权：
    // 在 HudBar.paint() 中可直接调用，也可完全用 ctx 原语自行绘制。
    // 变体一 = 饥饿条式：条 + 边框 + 条内文字标签（valueBox 无）
    // 变体二 = 血条式：条 + 文字标签 + 右侧独立数值方块（护甲值）

    /** 无文字版本：深色底框 + 比例填充，不绘制任何标签。ratio 钳制到 [0,1]。 */
    public static void bar(CanvasContext ctx, float x, float y, float w, float h,
                           float ratio, int fillArgb) {
        bar(ctx, x, y, w, h, ratio, BarColor.solid(fillArgb), null);
    }

    /**
     * 变体一（饥饿条式）：条内文字标签型进度条 —— 深色底框 + 比例填充 +
     * 条内带阴影的白色文字（如 "Food 20/20"）。标签为 null 时不绘制文字。
     * ratio 钳制到 [0,1]。
     */
    public static void bar(CanvasContext ctx, float x, float y, float w, float h,
                           float ratio, int fillArgb, Component label) {
        bar(ctx, x, y, w, h, ratio, BarColor.solid(fillArgb), label);
    }

    /** 使用 {@link BarColor} 的无文字版本；可传单色或渐变色。 */
    public static void bar(CanvasContext ctx, float x, float y, float w, float h,
                           float ratio, BarColor fill) {
        bar(ctx, x, y, w, h, ratio, fill, null);
    }

    /** 使用 {@link BarColor} 的标签版本；渐变在填充区域内从左到右插值。 */
    public static void bar(CanvasContext ctx, float x, float y, float w, float h,
                           float ratio, BarColor fill, Component label) {
        darkSlot(ctx, x, y, w, h);
        float r = Math.max(0f, Math.min(1f, ratio));
        if (r > 0f) {
            float fillX = x + BAR_PAD;
            float fillY = y + BAR_PAD;
            float fillW = Math.max(0f, (w - 2f * BAR_PAD) * r);
            float fillH = Math.max(0f, h - 2f * BAR_PAD);
            fillBar(ctx, fillX, fillY, fillW, fillH, fill != null ? fill : BarColor.solid(WHITE));
        }
        if (label != null) ctx.drawString(label, x + 4f, y + 1f, WHITE, true);
    }

    private static void fillBar(CanvasContext ctx, float x, float y, float w, float h, BarColor fill) {
        if (w <= 0f || h <= 0f) return;
        int start = fill.colorAt(0f);
        int end = fill.colorAt(1f);
        if (start == end) {
            ctx.fill(x, y, w, h, start);
            return;
        }

        int steps = Math.min(256, Math.max(1, (int) Math.ceil(w)));
        float stepW = w / steps;
        for (int i = 0; i < steps; i++) {
            float t = steps == 1 ? 0f : i / (float) (steps - 1);
            ctx.fill(x + i * stepW, y, stepW + 0.25f, h, fill.colorAt(t));
        }
    }

    /**
     * 变体二（血条式）：条 + 文字标签 + 右侧独立数值方块（如护甲值）。
     * 左侧条占用宽度 = w - boxWidth - 2f，右侧方块显示 {@code value}。
     * 单色时数字颜色与条填充色一致；渐变时默认使用渐变末端色。
     * 两个区域都自带深色底框；ratio 钳制到 [0,1]。
     *
     * @param boxWidth 右侧数值方块宽度
     */
    public static void barWithValue(CanvasContext ctx, float x, float y, float w, float h,
                                    float ratio, int fillArgb, Component label,
                                    int value, float boxWidth) {
        barWithValue(ctx, x, y, w, h, ratio, BarColor.solid(fillArgb), label,
                value, boxWidth, fillArgb);
    }

    /** 使用 {@link BarColor} 的血条式变体，数值颜色默认取渐变末端色。 */
    public static void barWithValue(CanvasContext ctx, float x, float y, float w, float h,
                                    float ratio, BarColor fill, Component label,
                                    int value, float boxWidth) {
        BarColor safeFill = fill != null ? fill : BarColor.solid(WHITE);
        barWithValue(ctx, x, y, w, h, ratio, safeFill, label, value, boxWidth, safeFill.colorAt(1f));
    }

    /**
     * 变体二，数值方块使用独立文字颜色（如血条红色渐变、护甲值银白色）。
     */
    public static void barWithValue(CanvasContext ctx, float x, float y, float w, float h,
                                    float ratio, int fillArgb, Component label,
                                    int value, float boxWidth, int valueArgb) {
        barWithValue(ctx, x, y, w, h, ratio, BarColor.solid(fillArgb), label,
                value, boxWidth, valueArgb);
    }

    /** 使用 {@link BarColor} 且数值颜色独立的血条式变体。 */
    public static void barWithValue(CanvasContext ctx, float x, float y, float w, float h,
                                    float ratio, BarColor fill, Component label,
                                    int value, float boxWidth, int valueArgb) {
        float barW = Math.max(0f, w - boxWidth - 2f);
        bar(ctx, x, y, barW, h, ratio, fill, label);
        valueBox(ctx, x + w - boxWidth, y, boxWidth, h, value, valueArgb);
    }

    /** 独立的数值方块：深色底框 + 居中的浅灰数字（如血条右侧的护甲值）。 */
    public static void valueBox(CanvasContext ctx, float x, float y, float w, float h,
                                int value) {
        valueBox(ctx, x, y, w, h, value, VALUE_TEXT_COLOR);
    }

    /** 可指定数字颜色的数值方块；{@link #barWithValue} 用它让数字颜色跟随条填充色。 */
    public static void valueBox(CanvasContext ctx, float x, float y, float w, float h,
                                int value, int textColor) {
        darkSlot(ctx, x, y, w, h);
        ctx.drawString(Component.literal(String.valueOf(value)),
                x + (w - 6f) / 2f, y + 2f, textColor, false);
    }
}
