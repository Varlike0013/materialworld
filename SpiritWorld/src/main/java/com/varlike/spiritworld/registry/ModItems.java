package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.item.BottledSpiritItem;
import com.varlike.spiritworld.item.SpiritRingItem;
import com.varlike.spiritworld.item.SpiritToolItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}