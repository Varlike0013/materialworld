package com.varlike.materialworld.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Set;

/**
 * 玩家 EMC 数据接口。
 * Player EMC data interface.
 *
 * <p>附属 mod 通过 {@link MaterialWorldAPI#getPlayerEMC} 获取本接口实例，
 * 不要直接引用内部 {@code PlayerEMC} 类。</p>
 * <p>Addon mods should obtain this interface via {@link MaterialWorldAPI#getPlayerEMC},
 * and must not reference the internal {@code PlayerEMC} class directly.</p>
 */
public interface IPlayerEMC {

    /** 获取当前 EMC 余额 / Get current EMC balance */
    long getBalance();

    /** 设置 EMC 余额，负值归零 / Set EMC balance, negative values are clamped to 0 */
    void setBalance(long value);

    /** 增加 EMC 余额 / Add to EMC balance */
    void addBalance(long amount);

    /**
     * 尝试扣除 EMC，余额不足时返回 false 且不扣除。
     * Try to consume EMC, returns false and does nothing if insufficient.
     */
    boolean consumeBalance(long amount);

    /** 判断物品是否已被学习 / Check whether an item has been learned */
    boolean isLearned(Item item);

    /**
     * 学习一个物品。
     * Learn an item.
     *
     * @return true 表示首次学习 / true if this is the first time
     */
    boolean learn(Item item);

    /** 获取已学习物品集合（不可修改）/ Get learned items (unmodifiable) */
    Set<ResourceLocation> getLearnedItems();
}