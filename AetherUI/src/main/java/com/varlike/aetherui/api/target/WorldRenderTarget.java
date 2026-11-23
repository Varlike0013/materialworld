package com.varlike.aetherui.api.target;

import com.varlike.aetherui.api.layout.Alignment;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public final class WorldRenderTarget implements RenderTarget {

    private final Anchor anchor;
    private Alignment alignment = Alignment.BOTTOM_CENTER;
    private Orientation orientation = Orientation.CENTER;
    private float pixelsPerBlock = 128f;
    private boolean seeThrough = true;
    private boolean doubleSided = false;
    private float maxRenderDistance = 32f;
    private Supplier<Float> yawDegrees   = () -> 0f;
    private Supplier<Float> pitchDegrees = () -> 0f;
    private Supplier<Float> rollDegrees  = () -> 0f;

    public WorldRenderTarget(Anchor anchor) { this.anchor = anchor; }

    public WorldRenderTarget alignment(Alignment a)        { this.alignment = a; return this; }
    public WorldRenderTarget orientation(Orientation o)    { this.orientation = o; return this; }

    /**
     * 把朝向绑定到每帧求值的 supplier（表达式变换）。
     * 若手上是 {@link Bindable}，用 {@code orientation(bindable::get)} 即可。
     */
    public WorldRenderTarget orientation(Supplier<Orientation> supplier) {
        this.orientation = Orientation.dynamic(supplier); return this;
    }
    public WorldRenderTarget pixelsPerBlock(float v)       { this.pixelsPerBlock = v; return this; }
    public WorldRenderTarget seeThrough(boolean v)         { this.seeThrough = v; return this; }
    public WorldRenderTarget doubleSided(boolean v)        { this.doubleSided = v; return this; }
    public WorldRenderTarget maxRenderDistance(float v)    { this.maxRenderDistance = v; return this; }

    /**
     * 绕 Y 轴旋转角（度，垂直轴，像硬币翻转）。可传 lambda 每帧求值。
     * 配合 {@link #doubleSided(boolean)} 控制背面是否渲染。
     */
    public WorldRenderTarget yawDegrees(Supplier<Float> supplier) {
        this.yawDegrees = supplier != null ? supplier : () -> 0f; return this;
    }

    /** 固定 yaw 旋转角（度）。 */
    public WorldRenderTarget yawDegrees(float degrees) {
        this.yawDegrees = () -> degrees; return this;
    }

    /** 绕 X 轴旋转角（度，水平轴，前后翻）。可传 lambda 每帧求值。 */
    public WorldRenderTarget pitchDegrees(Supplier<Float> supplier) {
        this.pitchDegrees = supplier != null ? supplier : () -> 0f; return this;
    }

    /** 固定 pitch 旋转角（度）。 */
    public WorldRenderTarget pitchDegrees(float degrees) {
        this.pitchDegrees = () -> degrees; return this;
    }

    /**
     * 绕 Z 轴旋转角（度，屏幕法线，平面内转圈）。可传 lambda，每帧求值实现"转圈"动画。
     */
    public WorldRenderTarget rollDegrees(Supplier<Float> supplier) {
        this.rollDegrees = supplier != null ? supplier : () -> 0f; return this;
    }

    /** 固定旋转角（度）。 */
    public WorldRenderTarget rollDegrees(float degrees) {
        this.rollDegrees = () -> degrees; return this;
    }

    public Anchor anchor()               { return anchor; }
    public Alignment alignment()         { return alignment; }
    public Orientation orientation()     { return orientation; }
    public float pixelsPerBlock()        { return pixelsPerBlock; }
    public boolean seeThrough()          { return seeThrough; }
    public boolean doubleSided()         { return doubleSided; }
    public float maxRenderDistance()     { return maxRenderDistance; }
    public Supplier<Float> yawDegrees()   { return yawDegrees; }
    public Supplier<Float> pitchDegrees() { return pitchDegrees; }
    public Supplier<Float> rollDegrees()  { return rollDegrees; }
}