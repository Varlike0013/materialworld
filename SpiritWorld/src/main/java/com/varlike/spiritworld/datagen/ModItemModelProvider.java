package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SpiritWorld.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(ModItems.BOTTLED_SPIRIT.get());
        this.basicItem(ModItems.SPIRIT_IRON_RING.get());
        this.basicItem(ModItems.SPIRIT_GOLD_RING.get());
        this.basicItem(ModItems.SPIRIT_DIAMOND_RING.get());
    }
}