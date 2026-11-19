package com.varlike.aetherui.api.target;

/**
 * 3×3 对齐枚举，供 RenderTarget 决定"Panel 的哪个点对齐到锚点解析出的位置"。
 * u/v 是 Panel 内的归一化坐标，u ∈ {0,0.5,1}，v ∈ {0,0.5,1}。
 */
public enum Alignment {
    TOP_LEFT(0, 0),     TOP_CENTER(0.5f, 0),     TOP_RIGHT(1, 0),
    CENTER_LEFT(0, 0.5f), CENTER(0.5f, 0.5f),    CENTER_RIGHT(1, 0.5f),
    BOTTOM_LEFT(0, 1),  BOTTOM_CENTER(0.5f, 1),  BOTTOM_RIGHT(1, 1);

    private final float u, v;

    Alignment(float u, float v) { this.u = u; this.v = v; }
    public float u() { return u; }
    public float v() { return v; }
}