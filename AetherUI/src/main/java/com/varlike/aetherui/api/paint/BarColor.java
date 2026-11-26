package com.varlike.aetherui.api.paint;

/**
 * 进度条填充色：可以是单一颜色，也可以是从左到右的渐变色。
 *
 * <p>{@link #colorAt(float)} 的 t 为填充区域内的归一化位置，范围 0~1。
 */
@FunctionalInterface
public interface BarColor {

    /** 取 t 位置的颜色（ARGB）。 */
    int colorAt(float t);

    /** 单一颜色。 */
    static BarColor solid(int argb) {
        return t -> argb;
    }

    /** 从 startArgb 渐变到 endArgb。 */
    static BarColor gradient(int startArgb, int endArgb) {
        return t -> lerpArgb(startArgb, endArgb, t);
    }

    /** ARGB 分量线性插值。 */
    static int lerpArgb(int from, int to, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        int a = lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, k);
        int r = lerp((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, k);
        int g = lerp((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, k);
        int b = lerp(from & 0xFF, to & 0xFF, k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerp(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
