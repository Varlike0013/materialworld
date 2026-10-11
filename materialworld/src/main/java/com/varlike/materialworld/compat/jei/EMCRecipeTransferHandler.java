package com.varlike.materialworld.compat.jei;

import com.varlike.materialworld.client.ClientEMCData;
import com.varlike.materialworld.emc.EMCManager;
import com.varlike.materialworld.network.ServerboundEMCCraftPacket;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class EMCRecipeTransferHandler
        implements IRecipeTransferHandler<CraftingMenu, RecipeHolder<CraftingRecipe>> {

    private final IRecipeTransferHandlerHelper helper;

    public EMCRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    @MethodsReturnNonnullByDefault
    public Class<CraftingMenu> getContainerClass() {
        return CraftingMenu.class;
    }

    @Override
    @MethodsReturnNonnullByDefault
    public Optional<MenuType<CraftingMenu>> getMenuType() {
        return Optional.of(MenuType.CRAFTING);
    }

    @Override
    @MethodsReturnNonnullByDefault
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public IRecipeTransferError transferRecipe(
            CraftingMenu container,
            RecipeHolder<CraftingRecipe> recipeHolder,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        // 从 RecipeHolder 取出真正的配方对象
        CraftingRecipe recipe = recipeHolder.value();

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
                if (ItemStack.isSameItemSameComponents(k, key)) {
                    existing = k;
                    break;
                }
            }
            if (existing != null) inventoryCounts.merge(existing, stack.getCount(), Integer::sum);
            else inventoryCounts.put(key, stack.getCount());
        }

        // ===== 2. 逐槽位处理 =====
        Map<Integer, ItemStack> slotItems = new LinkedHashMap<>();
        long totalCost = 0L;
        boolean hasMissing = false;

        for (int slot = 0; slot < 9 && slot < ingredients.size(); slot++) {
            Ingredient ingredient = ingredients.get(slot);
            if (ingredient.isEmpty()) continue;

            boolean satisfied = false;
            for (Map.Entry<ItemStack, Integer> e : inventoryCounts.entrySet()) {
                if (ingredient.test(e.getKey()) && e.getValue() > 0) {
                    e.setValue(e.getValue() - 1);
                    satisfied = true;
                    break;
                }
            }
            if (satisfied) continue;

            ItemStack[] matching = ingredient.getItems();
            if (matching.length == 0) continue;
            ItemStack target = matching[0].copy();
            target.setCount(1);

            if (ClientEMCData.isLearned(target.getItem()) && EMCManager.getEMC(target.getItem()) > 0) {
                slotItems.put(slot, target);
                totalCost += EMCManager.getEMC(target.getItem());
                hasMissing = true;
            }
        }

        if (!hasMissing) {
            return null;
        }

        // ===== 3. 执行 / 预检查 =====
        if (doTransfer) {
            PacketDistributor.sendToServer(new ServerboundEMCCraftPacket(slotItems));
        } else {
            if (ClientEMCData.getBalance() < totalCost) {
                return helper.createUserErrorWithTooltip(
                        Component.translatable("message.materialworld.emc_insufficient",
                                totalCost, ClientEMCData.getBalance()));
            }
        }

        return null;
    }
}