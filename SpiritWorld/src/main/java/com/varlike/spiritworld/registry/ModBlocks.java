package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.block.SpiritCauldronBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SpiritWorld.MOD_ID);

    public static final DeferredBlock<SpiritCauldronBlock> SPIRIT_CAULDRON =
            BLOCKS.register("spirit_cauldron",
                    () -> new SpiritCauldronBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}