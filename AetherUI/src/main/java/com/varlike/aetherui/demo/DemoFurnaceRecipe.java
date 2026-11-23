package com.varlike.aetherui.demo;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.hud.RecipeInfo;
import com.varlike.aetherui.api.hud.RecipeInfoProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;

/**
 * Demo：瞄准熔炉（普通/烟熏炉/熔岩炉）时显示当前配方的
 * [输入] → [时间] → [输出]。
 *
 * <p>说明：输入/输出直接读熔炉槽位 0/2；配方与时间通过 {@code RecipeManager.getRecipeFor}
 * 查询得到。熔炉的实时 cookingProgress 是包私有字段，普通 Mod 无法读取，
 * 因此这里展示的是"该输入对应的配方时间"而非剩余进度条。
 */
public final class DemoFurnaceRecipe {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("aetherui", "demo_furnace_recipe");

    private static final RecipeInfoProvider PROVIDER = new RecipeInfoProvider() {
        @Override public ResourceLocation id() { return ID; }

        @Override public Component title() {
            return Component.literal("熔炼配方:");
        }

        @Override public List<RecipeInfo> getRecipes(Level level, BlockPos pos, BlockState state,
                                                     BlockEntity be, BlockHitResult hit, LocalPlayer player) {
            if (!(be instanceof AbstractFurnaceBlockEntity furnace)) return List.of();

            ItemStack input = furnace.getItem(0);
            if (input.isEmpty()) return List.of();

            // 查询输入物品对应的熔炼配方（1 个输入槽）
            Optional<RecipeHolder<SmeltingRecipe>> found = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
            if (found.isEmpty()) return List.of();

            SmeltingRecipe recipe = found.get().value();
            ItemStack output = recipe.getResultItem(level.registryAccess());
            if (output.isEmpty()) output = furnace.getItem(2); // 兜底：取产物槽

            int seconds = recipe.getCookingTime() / 20; // tick → 秒
            return List.of(RecipeInfo.of(input, output, seconds));
        }
    };

    private DemoFurnaceRecipe() {}

    public static void register()   { AetherUI.registerRecipeInfoProvider(PROVIDER); }
    public static void unregister() { AetherUI.unregisterRecipeInfoProvider(ID); }
}
