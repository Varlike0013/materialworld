package com.varlike.materialworld.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

public class PlayerEMCProvider
        implements ICapabilityProvider<Entity, Void, PlayerEMC> {

    private final PlayerEMC data = new PlayerEMC();

    @Override
    public PlayerEMC getCapability(Entity entity, Void context) {
        return data;
    }

    /** 供 CapabilityHandler 手动序列化/反序列化使用 */
    public CompoundTag serializeNBT() { return data.save(); }

    public void deserializeNBT(CompoundTag nbt) { data.load(nbt); }
}