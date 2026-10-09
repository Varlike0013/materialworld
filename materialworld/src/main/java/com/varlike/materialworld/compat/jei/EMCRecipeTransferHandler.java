package com.varlike.materialworld.compat.jei;

import com.varlike.materialworld.client.ClientEMCData;
import com.varlike.materialworld.emc.EMCManager;
import com.varlike.materialworld.network.NetworkHandler;
import com.varlike.materialworld.network.ServerboundEMCCraftPacket;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EMCRecipeTransferHandler implements IRecipeTransferHandler<CraftingMenu, CraftingRecipe> {

    private final IRecipeTransferHandlerHelper helper;

    public EMCRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<CraftingMenu> getContainerClass() {
        return CraftingMenu.class;
    }

    @Override
    public Optional<MenuType<CraftingMenu>> getMenuType() {
        return Optional.of(MenuType.CRAFTING);
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    // ✅ 只保留这一个方法：接口要求的抽象方法
    @Nullable
    @Override
    @SuppressWarnings("removal")
    public IRecipeTransferError transferRecipe(
            CraftingMenu container,
            CraftingRecipe recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            return helper.createInternalError();
        }

        // ===== 1. 统计背包物品（count=1 作 key）=====
        Map<ItemStack, Integer> inventoryCounts = new LinkedHashMap<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            ItemStack key = stack.copy();
            key.setCount(1);
            ItemStack existing = null;
            for (ItemStack k : inventoryCounts.keySet()) {
                if (ItemStack.isSameItemSameTags(k, key)) { existing = k; break; }
            }
            if (existing != null) inventoryCounts.merge(existing, stack.getCount(), Integer::sum);
            else inventoryCounts.put(key, stack.getCount());
        }

        // ===== 2. 逐槽位处理 =====
        // slotItems: 合成网格索引 → 需要从 EMC 合成的物品（只填背包缺的部分）
        Map<Integer, ItemStack> slotItems = new LinkedHashMap<>();
        long totalCost = 0L;
        boolean hasMissing = false;

        for (int slot = 0; slot < 9 && slot < ingredients.size(); slot++) {
            Ingredient ingredient = ingredients.get(slot);
            if (ingredient.isEmpty()) continue;

            // 尝试从背包扣一个
            boolean satisfied = false;
            for (Map.Entry<ItemStack, Integer> e : inventoryCounts.entrySet()) {
                if (ingredient.test(e.getKey()) && e.getValue() > 0) {
                    e.setValue(e.getValue() - 1);
                    satisfied = true;
                    break;
                }
            }
            if (satisfied) continue;

            // 背包没有 → 看是否已学
            ItemStack[] matching = ingredient.getItems();
            if (matching.length == 0) continue;
            ItemStack target = matching[0].copy();
            target.setCount(1);

            if (ClientEMCData.isLearned(target.getItem()) && EMCManager.getEMC(target.getItem()) > 0) {
                slotItems.put(slot, target);
                totalCost += EMCManager.getEMC(target.getItem());
                hasMissing = true;
            }
            // 未学 → 跳过，让 JEI 默认逻辑处理（合成台那个格子留空）
        }

        if (!hasMissing) {
            return null; // 背包材料够，交给 JEI
        }

        // ===== 3. 执行 / 预检查 =====
        if (doTransfer) {
            NetworkHandler.CHANNEL.sendToServer(new ServerboundEMCCraftPacket(slotItems));
        } else {
            if (ClientEMCData.getBalance() < totalCost) {
                return helper.createUserErrorWithTooltip(
                        Component.literal("§cEMC 不足，需要 " + totalCost
                                + "，当前 " + ClientEMCData.getBalance()));
            }
        }

        return null;
    }
}