package com.varlike.spiritworld.block;

import net.minecraft.world.level.block.SlabBlock;

public class ManaSlabBlock extends SlabBlock implements ManaCapacityProvider {

    private final long capacityBonus;

    public ManaSlabBlock(Properties props, long capacityBonus) {
        super(props);
        this.capacityBonus = capacityBonus;
    }

    @Override
    public long getCapacityBonus() {
        return capacityBonus;
    }
}