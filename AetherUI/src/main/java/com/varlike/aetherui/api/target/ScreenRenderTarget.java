package com.varlike.aetherui.api.target;

import com.varlike.aetherui.api.layout.Alignment;

public final class ScreenRenderTarget implements RenderTarget {

    private final ScreenAnchor screenAnchor;
    private Alignment alignment = Alignment.TOP_LEFT;
    private float guiScale = 1f;
    private int layer = 0;   // 用于未来在 RegisterGuiLayersEvent 中的顺序控制

    public ScreenRenderTarget(ScreenAnchor a) { this.screenAnchor = a; }

    public ScreenRenderTarget alignment(Alignment a) { this.alignment = a; return this; }
    public ScreenRenderTarget guiScale(float v)      { this.guiScale = v; return this; }
    public ScreenRenderTarget layer(int v)           { this.layer = v; return this; }

    public ScreenAnchor screenAnchor() { return screenAnchor; }
    public Alignment alignment()       { return alignment; }
    public float guiScale()            { return guiScale; }
    public int layer()                 { return layer; }
}