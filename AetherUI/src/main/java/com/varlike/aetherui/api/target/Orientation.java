package com.varlike.aetherui.api.target;

import com.varlike.aetherui.api.binding.Bindable;

import java.util.function.Supplier;

/**
 * 世界空间 UI 的朝向。支持：
 * <ul>
 *     <li>{@link Center} —— 完全面向玩家（billboard，默认）；</li>
 *     <li>{@link Vertical} —— 仅绕 Y 轴面向玩家，保持竖直；</li>
 *     <li>{@link Fixed} —— 固定世界朝向；</li>
 *     <li>{@link Dynamic} —— 绑定参数或表达式，每帧求值朝向。</li>
 * </ul>
 */
public sealed interface Orientation {

    /** 固定世界朝向：yaw/pitch 为角度制，与 Minecraft 的朝向语义一致。 */
    record Fixed(float yawDegrees, float pitchDegrees) implements Orientation {}

    /** 仅绕 Y 轴旋转，抵消摄像机偏航角。血条场景首选。 */
    record Vertical() implements Orientation {}

    /** 完全面向摄像机（yaw + pitch）。悬浮信息面板首选。 */
    record Center() implements Orientation {}

    /** 动态朝向：每帧从 Bindable 读取朝向，可绑定参数或写表达式。 */
    record Dynamic(Bindable<Orientation> supplier) implements Orientation {}

    Orientation VERTICAL = new Vertical();
    Orientation CENTER   = new Center();

    // ---------------- 便捷工厂 ----------------

    static Orientation fixed(float yawDegrees, float pitchDegrees) {
        return new Fixed(yawDegrees, pitchDegrees);
    }

    static Orientation vertical() { return VERTICAL; }

    static Orientation center() { return CENTER; }

    /** 绑定到每帧求值的 supplier（表达式变换）。 */
    static Orientation dynamic(Supplier<Orientation> supplier) {
        return new Dynamic(Bindable.supplier(supplier));
    }

    /** 绑定到 Bindable，可配合 .map(...) 做表达式变换。 */
    static Orientation bindable(Bindable<Orientation> bindable) { return new Dynamic(bindable); }
}
