package com.varlike.aetherui.api.layout;

public record EdgeInsets(float top, float right, float bottom, float left) {

    public static final EdgeInsets ZERO = new EdgeInsets(0, 0, 0, 0);

    public static EdgeInsets all(float v)            { return new EdgeInsets(v, v, v, v); }
    public static EdgeInsets symmetric(float h, float v) { return new EdgeInsets(v, h, v, h); }

    public float horizontal() { return left + right; }
    public float vertical()   { return top + bottom; }
}