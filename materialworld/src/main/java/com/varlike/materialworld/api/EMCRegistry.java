package com.varlike.materialworld.api;

import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * 物品 EMC 值注册与查询接口。
 * Item EMC value registration and query interface.
 *
 * <p>附属 mod 可在 {@code FMLCommonSetupEvent} 或自身初始化阶段调用这些方法，
 * 为自定义物品注册 EMC 值。</p>
 */
public final class EMCRegistry {

    private EMCRegistry() {}

    /**
     * 获取物品的 EMC 值，未定义时返回 0。
     * Get the EMC value of an item, returns 0 if undefined.
     */
    public static long getEMC(Item item) {
        return EMCManager.getEMC(item);
    }

    /**
     * 设置或覆盖物品的 EMC 值。
     * Set or override an item's EMC value.
     */
    public static void setEMC(Item item, long value) {
        EMCManager.setEMC(item, value);
    }

    /**
     * 通过注册名获取 EMC 值，物品不存在时返回 0。
     * Get EMC by registry name, returns 0 if not found.
     */
    public static long getEMC(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == Items.AIR ? 0L : EMCManager.getEMC(item);
    }

    /**
     * 通过注册名设置 EMC 值，物品不存在时静默失败。
     * Set EMC by registry name, silently fails if not found.
     */
    public static void setEMC(ResourceLocation itemId, long value) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item != Items.AIR) {
            EMCManager.setEMC(item, value);
        }
    }

    /** 判断物品是否已定义 EMC / Check whether an item has a defined EMC */
    public static boolean hasEMC(Item item) {
        return EMCManager.hasEMC(item);
    }

    /** 返回已定义 EMC 的物品数量 / Number of items with defined EMC */
    public static int size() {
        return EMCManager.size();
    }
}