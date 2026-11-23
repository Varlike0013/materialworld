package com.varlike.aetherui.api.target;

import com.varlike.aetherui.api.binding.Bindable;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * 世界空间 UI 的位置来源。支持三种静态定位与一种动态定位：
 * <ul>
 *     <li>{@link Fixed} —— 默认固定世界坐标；</li>
 *     <li>{@link Entity} —— 绑定实体，自动跟随；</li>
 *     <li>{@link PlayerRelative} —— 相对玩家视线的坐标；</li>
 *     <li>{@link Dynamic} —— 绑定参数 / 表达式，每帧求值世界坐标。</li>
 * </ul>
 */
public sealed interface Anchor {

    /** 固定世界坐标。 */
    record Fixed(Vec3 worldPos) implements Anchor {}

    /** 跟随实体：offset 是相对实体脚底、实体局部坐标系的偏移。 */
    record Entity(java.util.UUID entityId, Vec3 offset) implements Anchor {}

    /** 相对玩家：offset 在玩家的视线坐标系中解释。 */
    record PlayerRelative(Vec3 offset) implements Anchor {}

    /**
     * 动态位置：每帧从 {@link Bindable} 读取世界坐标。
     * 可用于参数绑定（如每帧读取实体的 live 位置）或任意表达式变换。
     */
    record Dynamic(Bindable<Vec3> supplier) implements Anchor {}

    // ---------------- 便捷工厂 ----------------

    static Anchor fixed(Vec3 worldPos) { return new Fixed(worldPos); }

    static Anchor entity(java.util.UUID entityId, Vec3 offset) { return new Entity(entityId, offset); }

    static Anchor playerRelative(Vec3 offset) { return new PlayerRelative(offset); }

    /** 绑定到每帧求值的 supplier（等价于表达式变换）。 */
    static Anchor dynamic(Supplier<Vec3> supplier) { return new Dynamic(Bindable.supplier(supplier)); }

    /** 绑定到 Bindable，可配合 .map(...) 做表达式变换。 */
    static Anchor bindable(Bindable<Vec3> bindable) { return new Dynamic(bindable); }
}
