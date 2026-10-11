package com.varlike.materialworld.registry;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 附属创造标签的物品收集器。
 * 支持分组：同一 groupName 的物品会被连续排列，并在组前插入一个标题锚点。
 * 未指定 groupName 的物品归入无标题的默认组。
 */
public final class AddonCreativeTab {

    /**
     * 单个条目：分组名 + 物品供应器。
     * groupName 为 null 表示不属于任何分组。
     */
    public record Entry(@Nullable String groupName, Supplier<ItemStack> supplier) {}

    private static final List<Entry> ITEMS = new ArrayList<>();

    private AddonCreativeTab() {}

    /** 无分组添加 */
    public static void add(Supplier<ItemStack> supplier) {
        ITEMS.add(new Entry(null, supplier));
    }

    /** 带分组添加 */
    public static void add(String groupName, Supplier<ItemStack> supplier) {
        ITEMS.add(new Entry(groupName, supplier));
    }

    public static List<Entry> getAll() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static boolean isEmpty() {
        return ITEMS.isEmpty();
    }

    /** 返回第一个物品作为标签图标 */
    public static Supplier<ItemStack> getIconSupplier() {
        return ITEMS.isEmpty() ? null : ITEMS.get(0).supplier();
    }
}