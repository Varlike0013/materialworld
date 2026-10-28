package com.varlike.spiritworld.component;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SpiritWorld.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpiritStored>> SPIRIT_STORED =
            DATA_COMPONENTS.register("spirit_stored",
                    () -> DataComponentType.<SpiritStored>builder()
                            .persistent(SpiritStored.CODEC)
                            .networkSynchronized(SpiritStored.STREAM_CODEC)
                            .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CrystalMana>> CRYSTAL_MANA =
            DATA_COMPONENTS.register("crystal_mana", () -> DataComponentType.<CrystalMana>builder()
                    .persistent(CrystalMana.CODEC)
                    .networkSynchronized(CrystalMana.STREAM_CODEC)
                    .build());

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}