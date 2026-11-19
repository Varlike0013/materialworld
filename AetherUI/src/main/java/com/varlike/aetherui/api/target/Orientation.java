package com.varlike.aetherui.api.target;

public sealed interface Orientation {

    /** 固定世界朝向：yaw/pitch 为角度制，与 Minecraft 的朝向语义一致。 */
    record Fixed(float yawDegrees, float pitchDegrees) implements Orientation {}

    /** 仅绕 Y 轴旋转，抵消摄像机偏航角。血条场景首选。 */
    record Vertical() implements Orientation {}

    /** 完全面向摄像机（yaw + pitch）。悬浮信息面板首选。 */
    record Center() implements Orientation {}

    Orientation VERTICAL = new Vertical();
    Orientation CENTER   = new Center();
}