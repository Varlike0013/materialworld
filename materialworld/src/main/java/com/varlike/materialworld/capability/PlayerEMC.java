package com.varlike.materialworld.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlayerEMC {
    private long emcBalance = 0L;
    private final Set<ResourceLocation> learnedItems = new HashSet<>();

    public long getEmcBalance() { return emcBalance; }
    public void setEmcBalance(long value) { this.emcBalance = Math.max(0L, value); }
    public void addEmc(long amount) { this.emcBalance = Math.max(0L, emcBalance + amount); }
    public boolean consumeEmc(long amount) {
        if (amount < 0 || emcBalance < amount) return false;
        emcBalance -= amount;
        return true;
    }

    public boolean isLearned(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && learnedItems.contains(key);
    }

    /** @return true 表示这次是新学会的 */
    public boolean learn(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && learnedItems.add(key);
    }

    public Set<ResourceLocation> getLearnedItems() {
        return Collections.unmodifiableSet(learnedItems);
    }

    public void copyFrom(PlayerEMC other) {
        this.emcBalance = other.emcBalance;
        this.learnedItems.clear();
        this.learnedItems.addAll(other.learnedItems);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("emc", emcBalance);
        ListTag list = new ListTag();
        for (ResourceLocation rl : learnedItems) {
            list.add(StringTag.valueOf(rl.toString()));
        }
        tag.put("learned", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        emcBalance = tag.getLong("emc");
        learnedItems.clear();
        ListTag list = tag.getList("learned", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
            if (rl != null) learnedItems.add(rl);
        }
    }
}