package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.item.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Item.Properties;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(SpiritWorld.MOD_ID);

    /** 瓶装精神力 / Bottled Spirit */
    public static final DeferredItem<BottledSpiritItem> BOTTLED_SPIRIT =
            ITEMS.register("bottled_spirit",
                    () -> new BottledSpiritItem(new Item.Properties().stacksTo(8)));

    // ===== 精神秘戒 / Spirit Rings =====
    public static final DeferredItem<SpiritRingItem> SPIRIT_IRON_RING =
            ITEMS.register("spirit_iron_ring",
                    () -> new SpiritRingItem(100L,
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SpiritRingItem> SPIRIT_GOLD_RING =
            ITEMS.register("spirit_gold_ring",
                    () -> new SpiritRingItem(300L,
                            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<SpiritRingItem> SPIRIT_DIAMOND_RING =
            ITEMS.register("spirit_diamond_ring",
                    () -> new SpiritRingItem(500L,
                            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    /** 精神盐 / Spirit Salt */
    public static final DeferredItem<Item> SPIRIT_SALT =
            ITEMS.register("spirit_salt",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<SpiritToolItem> SPIRIT_TOOL =
            ITEMS.register("spirit_tool",
                    () -> new SpiritToolItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final DeferredItem<Item> MANA_BRICK =
            ITEMS.register("mana_brick", () -> new Item(new Item.Properties()));
    public static final DeferredItem<ManaCrystalItem> MANA_CRYSTAL_BASIC =
            ITEMS.register("mana_crystal_basic", () -> new ManaCrystalItem(
                    new Item.Properties().stacksTo(1), 500L));

    public static final DeferredItem<ManaCrystalItem> MANA_CRYSTAL_ADVANCED =
            ITEMS.register("mana_crystal_advanced", () -> new ManaCrystalItem(
                    new Item.Properties().stacksTo(1), 2000L));

    public static final DeferredItem<ManaCrystalItem> MANA_CRYSTAL_ULTIMATE =
            ITEMS.register("mana_crystal_ultimate", () -> new ManaCrystalItem(
                    new Item.Properties().stacksTo(1), 10000L));
    public static final DeferredItem<Item> MANA_CLAY =
            ITEMS.register("mana_clay", () -> new Item(new Item.Properties()));
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
    // ===== 魔力合金锭 =====
    public static final DeferredItem<Item> MANA_ALLOY =
            ITEMS.register("mana_alloy", () -> new Item(new Item.Properties()));

    // ===== 盔甲（耐久倍率 35，介于钻石 33 和下界合金 37 之间）=====
    public static final DeferredItem<ArmorItem> MANA_ALLOY_HELMET =
            ITEMS.register("mana_alloy_helmet", () -> new ArmorItem(
                    ModArmorMaterials.MANA_ALLOY, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(35))));

    public static final DeferredItem<ArmorItem> MANA_ALLOY_CHESTPLATE =
            ITEMS.register("mana_alloy_chestplate", () -> new ArmorItem(
                    ModArmorMaterials.MANA_ALLOY, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(35))));

    public static final DeferredItem<ArmorItem> MANA_ALLOY_LEGGINGS =
            ITEMS.register("mana_alloy_leggings", () -> new ArmorItem(
                    ModArmorMaterials.MANA_ALLOY, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(35))));

    public static final DeferredItem<ArmorItem> MANA_ALLOY_BOOTS =
            ITEMS.register("mana_alloy_boots", () -> new ArmorItem(
                    ModArmorMaterials.MANA_ALLOY, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(35))));

    // ===== 工具 =====
    public static final DeferredItem<SwordItem> MANA_ALLOY_SWORD =
            ITEMS.register("mana_alloy_sword", () -> new SwordItem(
                    ManaToolTier.INSTANCE,
                    new Item.Properties().attributes(
                            SwordItem.createAttributes(ManaToolTier.INSTANCE, 3, -2.4f))));

    public static final DeferredItem<PickaxeItem> MANA_ALLOY_PICKAXE =
            ITEMS.register("mana_alloy_pickaxe", () -> new PickaxeItem(
                    ManaToolTier.INSTANCE,
                    new Item.Properties().attributes(
                            PickaxeItem.createAttributes(ManaToolTier.INSTANCE, 1, -2.8f))));

    public static final DeferredItem<AxeItem> MANA_ALLOY_AXE =
            ITEMS.register("mana_alloy_axe", () -> new AxeItem(
                    ManaToolTier.INSTANCE,
                    new Item.Properties().attributes(
                            AxeItem.createAttributes(ManaToolTier.INSTANCE, 6.0f, -3.1f))));

    public static final DeferredItem<ShovelItem> MANA_ALLOY_SHOVEL =
            ITEMS.register("mana_alloy_shovel", () -> new ShovelItem(
                    ManaToolTier.INSTANCE,
                    new Item.Properties().attributes(
                            ShovelItem.createAttributes(ManaToolTier.INSTANCE, 1.5f, -3.0f))));

    public static final DeferredItem<HoeItem> MANA_ALLOY_HOE =
            ITEMS.register("mana_alloy_hoe", () -> new HoeItem(
                    ManaToolTier.INSTANCE,
                    new Item.Properties().attributes(
                            HoeItem.createAttributes(ManaToolTier.INSTANCE, -3.0f, 0.0f))));
    public static final DeferredItem<ManaAlloyArrowItem> MANA_ALLOY_ARROW =
            ITEMS.register("mana_alloy_arrow", () -> new ManaAlloyArrowItem(new Item.Properties()));
    // 魔力合金弓：耐久 1200（原版弓 384），拉弓速度不变
    public static final DeferredItem<BowItem> MANA_ALLOY_BOW =
            ITEMS.register("mana_alloy_bow", () -> new BowItem(
                    new Item.Properties().durability(1200)));
}