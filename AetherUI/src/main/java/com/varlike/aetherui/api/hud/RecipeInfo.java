package com.varlike.aetherui.api.hud;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 一条配方显示数据，供 {@link RecipeInfoProvider} 返回、由右下角 HUD 渲染为：
 *
 * <pre>
 *   [材料] → [时间] → [产物]
 * </pre>
 *
 * <p>材料与产物以 {@link ItemStack} 形式提供，HUD 会用 {@code ctx.renderItem} 绘制图标；
 * 时间以秒为单位（可为 0 表示不显示）。所有列表在构造时做不可变拷贝，避免外部修改。
 */
public final class RecipeInfo {

    private final List<ItemStack> ingredients;
    private final ItemStack result;
    private final int cookingSeconds;

    /**
     * @param ingredients 材料物品（可多个，通常会是配方的 ingredient 展开结果）；空列表时不绘制材料位
     * @param result      产物；传 {@link ItemStack#EMPTY} 时不绘制产物位
     * @param cookingSeconds 熔炼/加工时间（秒）；传 0 则不显示时间标签
     */
    public RecipeInfo(List<ItemStack> ingredients, ItemStack result, int cookingSeconds) {
        this.ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        this.result = result == null ? ItemStack.EMPTY : result;
        this.cookingSeconds = Math.max(0, cookingSeconds);
    }

    /** 快速构造：单材料 → 产物，带时间（秒）。 */
    public static RecipeInfo of(ItemStack ingredient, ItemStack result, int cookingSeconds) {
        return new RecipeInfo(List.of(ingredient), result, cookingSeconds);
    }

    public List<ItemStack> ingredients() { return ingredients; }
    public ItemStack result()           { return result; }
    public int cookingSeconds()         { return cookingSeconds; }
}
