package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.block.SpiritCollectorBlockEntity;
import com.varlike.spiritworld.block.SpiritCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SpiritWorld.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpiritCollectorBlockEntity>> SPIRIT_COLLECTOR =
            BLOCK_ENTITIES.register("spirit_collector", () -> BlockEntityType.Builder.of(
                    SpiritCollectorBlockEntity::new,
                    ModBlocks.SPIRIT_COLLECTOR_BASIC.get(),
                    ModBlocks.SPIRIT_COLLECTOR_ADVANCED.get(),
                    ModBlocks.SPIRIT_COLLECTOR_ULTIMATE.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpiritCoreBlockEntity>> SPIRIT_CORE =
            BLOCK_ENTITIES.register("spirit_core", () -> BlockEntityType.Builder.of(
                    SpiritCoreBlockEntity::new,
                    ModBlocks.SPIRIT_CORE.get()
            ).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }

}