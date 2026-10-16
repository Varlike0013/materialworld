package com.varlike.MwConstructors.datagen;

import com.varlike.MwConstructors.MwConstructorsMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MwConstructorsMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }
}