package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SpiritWorld.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // 改为引用我们新创建的自定义模型
        ModelFile level1 = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_level1"));
        ModelFile level2 = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_level2"));
        ModelFile full = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_full"));

        getVariantBuilder(ModBlocks.SPIRIT_CAULDRON.get())
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 1)
                .modelForState().modelFile(level1).addModel()
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 2)
                .modelForState().modelFile(level2).addModel()
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 3)
                .modelForState().modelFile(full).addModel();
    }
}