package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModBlocks;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
        spiritCollector(output);
        manaCrafting(output);
        manaClayRecipes(output);
        manaAlloyCrafting(output);
    }
    private void spiritCollector(RecipeOutput output) {
        // Tier 1: 4 铁 + 4 玻璃 + 1 红石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPIRIT_COLLECTOR_BASIC.get())
                .pattern("IGI")
                .pattern("GRG")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        // Tier 2: Tier1 + 4 金 + 4 青金石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPIRIT_COLLECTOR_ADVANCED.get())
                .pattern("GLG")
                .pattern("LBL")
                .pattern("GLG")
                .define('G', Items.GOLD_INGOT)
                .define('L', Items.LAPIS_LAZULI)
                .define('B', ModBlocks.SPIRIT_COLLECTOR_BASIC.get())
                .unlockedBy("has_basic", has(ModBlocks.SPIRIT_COLLECTOR_BASIC.get()))
                .save(output);

        // Tier 3: Tier2 + 4 钻石 + 4 绿宝石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPIRIT_COLLECTOR_ULTIMATE.get())
                .pattern("DED")
                .pattern("EAE")
                .pattern("DED")
                .define('D', Items.DIAMOND)
                .define('E', Items.EMERALD)
                .define('A', ModBlocks.SPIRIT_COLLECTOR_ADVANCED.get())
                .unlockedBy("has_advanced", has(ModBlocks.SPIRIT_COLLECTOR_ADVANCED.get()))
                .save(output);
    }
    private void manaCrafting(RecipeOutput output) {
        // 4 魔力砖 → 1 魔力方块
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MANA_BLOCK.get())
                .pattern("BB")
                .pattern("BB")
                .define('B', ModItems.MANA_BRICK.get())
                .unlockedBy("has_brick", has(ModItems.MANA_BRICK.get()))
                .save(output);

        // 3 魔力方块 → 6 魔力台阶
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MANA_SLAB.get(), 6)
                .pattern("BBB")
                .define('B', ModBlocks.MANA_BLOCK.get())
                .unlockedBy("has_block", has(ModBlocks.MANA_BLOCK.get()))
                .save(output);

        // 6 魔力方块 → 4 魔力楼梯
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MANA_STAIRS.get(), 4)
                .pattern("B  ")
                .pattern("BB ")
                .pattern("BBB")
                .define('B', ModBlocks.MANA_BLOCK.get())
                .unlockedBy("has_block", has(ModBlocks.MANA_BLOCK.get()))
                .save(output);

        // 魔力核心：4 魔力砖 + 1 末影之眼 + 4 紫水晶碎片
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPIRIT_CORE.get())
                .pattern("BAB")
                .pattern("AEA")
                .pattern("BAB")
                .define('B', ModItems.MANA_BRICK.get())
                .define('A', Items.AMETHYST_SHARD)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_brick", has(ModItems.MANA_BRICK.get()))
                .save(output);
    }
    private void manaClayRecipes(RecipeOutput output) {
        // 8 黏土 + 中间 1 瓶装精神力 → 8 魔力黏土
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MANA_CLAY.get(), 8)
                .pattern("CCC")
                .pattern("CBC")
                .pattern("CCC")
                .define('C', Items.CLAY_BALL)
                .define('B', ModItems.BOTTLED_SPIRIT.get())
                .unlockedBy("has_bottled_spirit", has(ModItems.BOTTLED_SPIRIT.get()))
                .save(output);

        // 魔力黏土 → 魔力砖（熔炉，5 秒）
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.MANA_CLAY.get()),
                        RecipeCategory.MISC,
                        ModItems.MANA_BRICK.get(),
                        0.3f,
                        100)
                .unlockedBy("has_mana_clay", has(ModItems.MANA_CLAY.get()))
                .save(output, "spiritworld:mana_brick_from_smelting");

        // 魔力黏土 → 魔力砖（高炉，2.5 秒）
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.MANA_CLAY.get()),
                        RecipeCategory.MISC,
                        ModItems.MANA_BRICK.get(),
                        0.3f,
                        50)
                .unlockedBy("has_mana_clay", has(ModItems.MANA_CLAY.get()))
                .save(output, "spiritworld:mana_brick_from_blasting");
    }
    private void manaAlloyCrafting(RecipeOutput output) {
        Item alloy = ModItems.MANA_ALLOY.get();

        // 剑
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_SWORD.get())
                .pattern("A").pattern("A").pattern("S")
                .define('A', alloy).define('S', Items.STICK)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 镐
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MANA_ALLOY_PICKAXE.get())
                .pattern("AAA").pattern(" S ").pattern(" S ")
                .define('A', alloy).define('S', Items.STICK)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 斧
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MANA_ALLOY_AXE.get())
                .pattern("AA ").pattern("AS ").pattern(" S ")
                .define('A', alloy).define('S', Items.STICK)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 铲
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MANA_ALLOY_SHOVEL.get())
                .pattern("A").pattern("S").pattern("S")
                .define('A', alloy).define('S', Items.STICK)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 锄
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MANA_ALLOY_HOE.get())
                .pattern("AA").pattern(" S").pattern(" S")
                .define('A', alloy).define('S', Items.STICK)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 头盔
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_HELMET.get())
                .pattern("AAA").pattern("A A")
                .define('A', alloy)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 胸甲
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_CHESTPLATE.get())
                .pattern("A A").pattern("AAA").pattern("AAA")
                .define('A', alloy)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 护腿
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_LEGGINGS.get())
                .pattern("AAA").pattern("A A").pattern("A A")
                .define('A', alloy)
                .unlockedBy("has_alloy", has(alloy)).save(output);

        // 靴子
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_BOOTS.get())
                .pattern("A A").pattern("A A")
                .define('A', alloy)
                .unlockedBy("has_alloy", has(alloy)).save(output);
        // 4 魔力合金箭（箭身 = 魔力合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_ARROW.get(), 4)
                .pattern("A")
                .pattern("S")
                .pattern("F")
                .define('A', ModItems.MANA_ALLOY.get())
                .define('S', Items.STICK)
                .define('F', Items.FEATHER)
                .unlockedBy("has_alloy", has(ModItems.MANA_ALLOY.get()))
                .save(output);
        //魔力合金弓
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MANA_ALLOY_BOW.get())
                .pattern(" AS")
                .pattern("A S")
                .pattern(" AS")
                .define('A', ModItems.MANA_ALLOY.get())
                .define('S', Items.STRING)
                .unlockedBy("has_alloy", has(ModItems.MANA_ALLOY.get()))
                .save(output);
    }
}