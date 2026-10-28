package com.varlike.spiritworld.block;

/** 提供魔力核心上限加成的方块接口。 */
public interface ManaCapacityProvider {
    /** 该方块给相邻魔力核心提供的上限加成 */
    long getCapacityBonus();
}