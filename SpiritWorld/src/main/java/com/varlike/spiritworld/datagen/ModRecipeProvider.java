package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRIT_IRON_RING.get())
                .pattern(" I ")
                .pattern("I I")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRIT_GOLD_RING.get())
                .pattern(" G ")
                .pattern("G G")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRIT_DIAMOND_RING.get())
                .pattern(" D ")
                .pattern("D D")
                .pattern(" D ")
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);
        // ===== 熔炼：瓶装精神力 → 精神盐 =====
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.BOTTLED_SPIRIT.get()),
                        RecipeCategory.MISC,
                        ModItems.SPIRIT_SALT.get(),
                        0.3f,     // 经验
                        200       // 熔炼时间（tick）
                )
                .unlockedBy("has_bottled_spirit", has(ModItems.BOTTLED_SPIRIT.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(
                        SpiritWorld.MOD_ID, "spirit_salt_from_smelting"));
        // 高炉（时间减半）
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.BOTTLED_SPIRIT.get()),
                        RecipeCategory.MISC,
                        ModItems.SPIRIT_SALT.get(),
                        0.3f, 100)
                .unlockedBy("has_bottled_spirit", has(ModItems.BOTTLED_SPIRIT.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(
                        SpiritWorld.MOD_ID, "spirit_salt_from_blasting"));
    }
}