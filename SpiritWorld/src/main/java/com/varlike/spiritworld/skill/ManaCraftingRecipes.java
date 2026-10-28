package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.component.CrystalMana;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ManaCraftingRecipes {

    private ManaCraftingRecipes() {}

    private static final List<ManaCraftingRecipe> ALL = new ArrayList<>();

    static {
        // 魔力砖：10 精神 + 5 土，60 秒
        add("mana_brick", new ItemStack(ModItems.MANA_BRICK.get(), 1),
                Map.of(ManaType.SPIRIT, 10L, ManaType.EARTH, 5L), 60);

        // 钻石：30 精神 + 20 土，120 秒
        add("diamond", new ItemStack(Items.DIAMOND, 1),
                Map.of(ManaType.SPIRIT, 30L, ManaType.EARTH, 20L), 120);

        // 绿宝石：30 精神 + 20 水，120 秒
        add("emerald", new ItemStack(Items.EMERALD, 1),
                Map.of(ManaType.SPIRIT, 30L, ManaType.WATER, 20L), 120);

        // 金锭：15 精神 + 10 火，45 秒
        add("gold_ingot", new ItemStack(Items.GOLD_INGOT, 2),
                Map.of(ManaType.SPIRIT, 15L, ManaType.FIRE, 10L), 45);

        // 红石：8 精神 + 5 火，20 秒
        add("redstone", new ItemStack(Items.REDSTONE, 4),
                Map.of(ManaType.SPIRIT, 8L, ManaType.FIRE, 5L), 20);

        // 青金石：8 精神 + 5 水，20 秒
        add("lapis", new ItemStack(Items.LAPIS_LAZULI, 4),
                Map.of(ManaType.SPIRIT, 8L, ManaType.WATER, 5L), 20);

        // 附魔之瓶：20 精神 + 10 风，90 秒
        add("xp_bottle", new ItemStack(Items.EXPERIENCE_BOTTLE, 3),
                Map.of(ManaType.SPIRIT, 20L, ManaType.WIND, 10L), 90);

        // 紫水晶碎片：5 精神，15 秒
        add("amethyst_shard", new ItemStack(Items.AMETHYST_SHARD, 2),
                Map.of(ManaType.SPIRIT, 5L), 15);
        add("mana_crystal_basic",
                createCrystalStack(ModItems.MANA_CRYSTAL_BASIC.get()),
                Map.of(ManaType.SPIRIT, 20L, ManaType.EARTH, 20L), 60);

        add("mana_crystal_advanced",
                createCrystalStack(ModItems.MANA_CRYSTAL_ADVANCED.get()),
                Map.of(ManaType.SPIRIT, 100L, ManaType.EARTH, 100L), 180);

        add("mana_crystal_ultimate",
                createCrystalStack(ModItems.MANA_CRYSTAL_ULTIMATE.get()),
                Map.of(ManaType.SPIRIT, 500L, ManaType.EARTH, 500L), 600);
        add("mana_alloy",
                new ItemStack(ModItems.MANA_ALLOY.get(), 1),
                Map.of(
                        ManaType.SPIRIT, 30L,
                        ManaType.FIRE,   20L,
                        ManaType.EARTH,  20L),
                90);   // 90 秒
    }

    private static void add(String path, ItemStack output, Map<ManaType, Long> cost, int seconds) {
        ALL.add(new ManaCraftingRecipe(
                ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "forge/" + path),
                output, cost, seconds * 20));
    }

    public static List<ManaCraftingRecipe> all() { return ALL; }
    public static int size()                     { return ALL.size(); }
    public static ManaCraftingRecipe get(int i)  { return (i >= 0 && i < ALL.size()) ? ALL.get(i) : null; }
    private static ItemStack createCrystalStack(Item item) {
        ItemStack stack = new ItemStack(item);
        ManaCrystalItem.setMana(stack, CrystalMana.EMPTY);
        return stack;
    }
}