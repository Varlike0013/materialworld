package com.varlike.spiritworld.block;

import net.minecraft.world.level.block.Block;

public class ManaBlock extends Block implements ManaCapacityProvider {

    private final long capacityBonus;

    public ManaBlock(Properties props, long capacityBonus) {
        super(props);
        this.capacityBonus = capacityBonus;
    }

    @Override
    public long getCapacityBonus() {
        return capacityBonus;
    }
}