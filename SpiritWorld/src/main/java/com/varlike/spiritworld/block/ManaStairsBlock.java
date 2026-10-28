package com.varlike.spiritworld.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ManaStairsBlock extends StairBlock implements ManaCapacityProvider {

    private final long capacityBonus;

    public ManaStairsBlock(BlockState baseState, Properties props, long capacityBonus) {
        super(baseState, props);
        this.capacityBonus = capacityBonus;
    }

    @Override
    public long getCapacityBonus() {
        return capacityBonus;
    }
}