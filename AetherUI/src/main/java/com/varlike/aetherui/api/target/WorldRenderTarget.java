package com.varlike.aetherui.api.target;

import com.varlike.aetherui.api.layout.Alignment;

public final class WorldRenderTarget implements RenderTarget {

    private final Anchor anchor;
    private Alignment alignment = Alignment.BOTTOM_CENTER;
    private Orientation orientation = Orientation.CENTER;
    private float pixelsPerBlock = 128f;
    private boolean seeThrough = true;
    private boolean doubleSided = false;
    private float maxRenderDistance = 32f;

    public WorldRenderTarget(Anchor anchor) { this.anchor = anchor; }

    public WorldRenderTarget alignment(Alignment a)        { this.alignment = a; return this; }
    public WorldRenderTarget orientation(Orientation o)    { this.orientation = o; return this; }
    public WorldRenderTarget pixelsPerBlock(float v)       { this.pixelsPerBlock = v; return this; }
    public WorldRenderTarget seeThrough(boolean v)         { this.seeThrough = v; return this; }
    public WorldRenderTarget doubleSided(boolean v)        { this.doubleSided = v; return this; }
    public WorldRenderTarget maxRenderDistance(float v)    { this.maxRenderDistance = v; return this; }

    public Anchor anchor()               { return anchor; }
    public Alignment alignment()         { return alignment; }
    public Orientation orientation()     { return orientation; }
    public float pixelsPerBlock()        { return pixelsPerBlock; }
    public boolean seeThrough()          { return seeThrough; }
    public boolean doubleSided()         { return doubleSided; }
    public float maxRenderDistance()     { return maxRenderDistance; }
}