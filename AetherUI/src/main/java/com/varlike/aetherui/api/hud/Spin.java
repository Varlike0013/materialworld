package com.varlike.aetherui.api.hud;

import java.util.function.Function;

/**
 * 三轴自旋配置：每帧按时间 t（秒）求值 yaw / pitch / roll 角度（度）。
 * 用于 {@link WorldIcon#show} 让图标绕多个轴旋转。
 *
 * <p>三个轴含义：
 * <ul>
 *     <li>{@code yaw} —— 绕 Y 轴（垂直轴），像硬币翻转，转到 90° 时侧面看不见；</li>
 *     <li>{@code pitch} —— 绕 X 轴（水平轴），前后翻；</li>
 *     <li>{@code roll} —— 绕 Z 轴（屏幕法线），平面内转圈，始终面向玩家。</li>
 * </ul>
 */
public final class Spin {

    private final Function<Float, Float> yaw;
    private final Function<Float, Float> pitch;
    private final Function<Float, Float> roll;

    private Spin(Function<Float, Float> yaw,
                 Function<Float, Float> pitch,
                 Function<Float, Float> roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    /** 只绕垂直轴（yaw）旋转。 */
    public static Spin yaw(Function<Float, Float> fn) {
        return new Spin(fn, null, null);
    }

    /** 只绕水平轴（pitch）旋转。 */
    public static Spin pitch(Function<Float, Float> fn) {
        return new Spin(null, fn, null);
    }

    /** 只在屏幕平面转圈（roll），始终面向玩家。 */
    public static Spin roll(Function<Float, Float> fn) {
        return new Spin(null, null, fn);
    }

    /** 同时配置三个轴；任一参数可为 null 表示该轴不旋转。 */
    public static Spin of(Function<Float, Float> yaw,
                          Function<Float, Float> pitch,
                          Function<Float, Float> roll) {
        return new Spin(yaw, pitch, roll);
    }

    /** 不旋转。 */
    public static Spin none() {
        return new Spin(null, null, null);
    }

    public Function<Float, Float> yaw()   { return yaw; }
    public Function<Float, Float> pitch() { return pitch; }
    public Function<Float, Float> roll()  { return roll; }
}
