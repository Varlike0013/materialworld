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
        this.basicItem(ModItems.SPIRIT_SALT.get());
        this.basicItem(ModItems.SPIRIT_TOOL.get());
        basicItem(ModItems.MANA_CRYSTAL_BASIC.get());
        basicItem(ModItems.MANA_CRYSTAL_ADVANCED.get());
        basicItem(ModItems.MANA_CRYSTAL_ULTIMATE.get());
        basicItem(ModItems.MANA_CLAY.get());
        basicItem(ModItems.MANA_ALLOY.get());
        basicItem(ModItems.MANA_ALLOY_HELMET.get());
        basicItem(ModItems.MANA_ALLOY_CHESTPLATE.get());
        basicItem(ModItems.MANA_ALLOY_LEGGINGS.get());
        basicItem(ModItems.MANA_ALLOY_BOOTS.get());
        basicItem(ModItems.MANA_ALLOY_SWORD.get());
        basicItem(ModItems.MANA_ALLOY_PICKAXE.get());
        basicItem(ModItems.MANA_ALLOY_AXE.get());
        basicItem(ModItems.MANA_ALLOY_SHOVEL.get());
        basicItem(ModItems.MANA_ALLOY_HOE.get());
        basicItem(ModItems.MANA_ALLOY_ARROW.get());
        basicItem(ModItems.MANA_ALLOY_BOW.get());
    }
}