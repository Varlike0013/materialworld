package com.varlike.materialworld.emc;

import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * 从合成表自动推导 EMC 值。
 * 算法：多轮迭代，每轮扫描所有配方，为"所有材料都有 EMC 的结果物品"计算数值。
 */
public class EMCRecipeCalculator {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static int calculate(MinecraftServer server) {
        RegistryAccess registryAccess = server.registryAccess();
        Collection<Recipe<?>> allRecipes = server.getRecipeManager().getRecipes();

        // 筛选我们关心的配方类型
        List<Recipe<?>> relevantRecipes = allRecipes.stream()
                .filter(r -> r instanceof CraftingRecipe
                        || r instanceof SmeltingRecipe
                        || r instanceof BlastingRecipe
                        || r instanceof SmokingRecipe
                        || r instanceof CampfireCookingRecipe
                        || r instanceof StonecutterRecipe)
                .toList();

        LOGGER.info("[物质世界] 开始从 {} 个配方推导 EMC", relevantRecipes.size());

        int calculated = 0;
        boolean changed = true;
        int iterations = 0;
        int maxIterations = 200; // 防止循环配方导致死循环

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;
            for (Recipe<?> recipe : relevantRecipes) {
                if (tryCalculate(recipe, registryAccess)) {
                    calculated++;
                    changed = true;
                }
            }
        }

        LOGGER.info("[物质世界] 合成表推导完成：新增 {} 项，共 {} 轮迭代", calculated, iterations);
        return calculated;
    }

    private static boolean tryCalculate(Recipe<?> recipe, RegistryAccess registryAccess) {
        ItemStack result = recipe.getResultItem(registryAccess);
        if (result.isEmpty()) return false;

        Item resultItem = result.getItem();
        // 已有 EMC 的物品跳过（默认值或用户设置优先）
        if (EMCManager.hasEMC(resultItem)) return false;

        List<Ingredient> ingredients = recipe.getIngredients();
        long totalCost = 0;
        int nonEmptyCount = 0;

        for (Ingredient ing : ingredients) {
            if (ing.isEmpty()) continue;
            nonEmptyCount++;

            // 多选一 Ingredient：取所有候选中 EMC 最低的
            long minCost = Long.MAX_VALUE;
            for (ItemStack stack : ing.getItems()) {
                long e = EMCManager.getEMC(stack.getItem());
                if (e > 0 && e < minCost) minCost = e;
            }
            // 有材料没 EMC → 本次无法推导
            if (minCost == Long.MAX_VALUE) return false;
            totalCost += minCost;
        }

        if (nonEmptyCount == 0) return false;

        int count = result.getCount();
        long resultEMC = totalCost / count;
        if (resultEMC <= 0) return false;

        EMCManager.setEMC(resultItem, resultEMC);
        return true;
    }
}