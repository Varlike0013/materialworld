package com.varlike.materialworld.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerEMCProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerEMC data = new PlayerEMC();
    private final LazyOptional<PlayerEMC> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_EMC.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() { return data.save(); }

    @Override
    public void deserializeNBT(CompoundTag nbt) { data.load(nbt); }
}