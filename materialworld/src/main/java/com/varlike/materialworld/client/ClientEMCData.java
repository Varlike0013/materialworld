package com.varlike.materialworld.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClientEMCData {
    private static long balance = 0L;
    private static final Set<ResourceLocation> learned = new HashSet<>();

    public static long getBalance() { return balance; }

    public static boolean isLearned(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && learned.contains(key);
    }

    public static void update(long newBalance, Collection<ResourceLocation> newLearned) {
        balance = newBalance;
        learned.clear();
        learned.addAll(newLearned);
    }
}