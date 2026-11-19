package com.varlike.aetherui.api.target;

import net.minecraft.world.phys.Vec3;

public sealed interface Anchor {

    /** 固定世界坐标。 */
    record Fixed(Vec3 worldPos) implements Anchor {}

    /** 跟随实体：offset 是相对实体脚底、实体局部坐标系的偏移。 */
    record Entity(java.util.UUID entityId, Vec3 offset) implements Anchor {}

    /** 相对玩家：offset 在玩家的视线坐标系中解释。 */
    record PlayerRelative(Vec3 offset) implements Anchor {}
}