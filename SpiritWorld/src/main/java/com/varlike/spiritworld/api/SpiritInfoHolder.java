package com.varlike.spiritworld.api;

import java.util.List;

/**
 * 灵视信息提供者。
 * <p>
 * 任何 {@link net.minecraft.world.entity.Entity} 或
 * {@link net.minecraft.world.level.block.entity.BlockEntity} 实现此接口后，
 * 玩家准星选中时会在屏幕右侧看到 {@link #getSpiritInfo()} 返回的信息。
 * <p>
 * 返回空列表表示不显示。
 * 每条信息自带 requiredVisionLevel，HUD 会按玩家灵视等级过滤。
 */
public interface SpiritInfoHolder {

    /** 返回原始信息列表（未过滤）。渲染层按玩家灵视等级过滤。 */
    List<SpiritInfoLine> getSpiritInfo();
}