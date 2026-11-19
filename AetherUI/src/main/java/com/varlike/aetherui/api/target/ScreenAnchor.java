package com.varlike.aetherui.api.target;

public sealed interface ScreenAnchor {

    record Corner(Alignment corner, float offsetX, float offsetY) implements ScreenAnchor {}
    record EdgeMid(Alignment edge, float offset) implements ScreenAnchor {}
    record Percent(float xRatio, float yRatio) implements ScreenAnchor {}
    record Center() implements ScreenAnchor {}

    static ScreenAnchor topLeft(float x, float y)   { return new Corner(Alignment.TOP_LEFT, x, y); }
    static ScreenAnchor bottomCenter(float off)     { return new EdgeMid(Alignment.BOTTOM_CENTER, off); }
    static ScreenAnchor center()                     { return new Center(); }
    static ScreenAnchor percent(float xr, float yr)  { return new Percent(xr, yr); }
}