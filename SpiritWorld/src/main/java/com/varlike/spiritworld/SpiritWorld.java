package com.varlike.spiritworld;

import com.mojang.logging.LogUtils;
import com.varlike.materialworld.api.MaterialWorldAPI;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.component.ModDataComponents;
import com.varlike.spiritworld.entity.ModEntities;
import com.varlike.spiritworld.registry.ModBlockEntities;
import com.varlike.spiritworld.registry.ModBlocks;
import com.varlike.spiritworld.registry.ModItems;
import com.varlike.spiritworld.registry.ModMenus;
import com.varlike.spiritworld.skill.ModSkills;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(SpiritWorld.MOD_ID)
public class SpiritWorld {

    public static final String MOD_ID = "spiritworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 物质世界附属创造标签的分组名 */
    public static final String SPIRIT_TAB_GROUP = "materialworld.addon_group.spirit_world";

    public SpiritWorld(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("精神世界：初始化中...");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModCapabilities::register);

        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModSkills.registerAll();
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModEntities.register(modEventBus);
        registerCreativeTabContents();

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void registerCreativeTabContents() {
        // 物品
        addToSpiritTab(ModItems.BOTTLED_SPIRIT);
        addToSpiritTab(ModItems.SPIRIT_SALT);
        addToSpiritTab(ModItems.SPIRIT_TOOL);
        addToSpiritTab(ModItems.SPIRIT_IRON_RING);
        addToSpiritTab(ModItems.SPIRIT_GOLD_RING);
        addToSpiritTab(ModItems.SPIRIT_DIAMOND_RING);
        addToSpiritTab(ModItems.MANA_BRICK);
        addToSpiritTab(ModItems.MANA_CRYSTAL_BASIC);
        addToSpiritTab(ModItems.MANA_CRYSTAL_ADVANCED);
        addToSpiritTab(ModItems.MANA_CRYSTAL_ULTIMATE);
        addToSpiritTab(ModItems.MANA_CLAY);
        addToSpiritTab(ModItems.MANA_ALLOY);
        addToSpiritTab(ModItems.MANA_ALLOY_HELMET);
        addToSpiritTab(ModItems.MANA_ALLOY_CHESTPLATE);
        addToSpiritTab(ModItems.MANA_ALLOY_LEGGINGS);
        addToSpiritTab(ModItems.MANA_ALLOY_BOOTS);
        addToSpiritTab(ModItems.MANA_ALLOY_SWORD);
        addToSpiritTab(ModItems.MANA_ALLOY_PICKAXE);
        addToSpiritTab(ModItems.MANA_ALLOY_AXE);
        addToSpiritTab(ModItems.MANA_ALLOY_SHOVEL);
        addToSpiritTab(ModItems.MANA_ALLOY_HOE);
        addToSpiritTab(ModItems.MANA_ALLOY_ARROW);
        addToSpiritTab(ModItems.MANA_ALLOY_BOW);

        // 方块
        addToSpiritTab(ModBlocks.SPIRIT_COLLECTOR_BASIC);
        addToSpiritTab(ModBlocks.SPIRIT_COLLECTOR_ADVANCED);
        addToSpiritTab(ModBlocks.SPIRIT_COLLECTOR_ULTIMATE);
        addToSpiritTab(ModBlocks.MANA_BLOCK);
        addToSpiritTab(ModBlocks.MANA_SLAB);
        addToSpiritTab(ModBlocks.MANA_STAIRS);
        addToSpiritTab(ModBlocks.SPIRIT_CORE);
        addToSpiritTab(ModBlocks.MANA_RELAY_1);
        addToSpiritTab(ModBlocks.MANA_RELAY_2);
        addToSpiritTab(ModBlocks.MANA_RELAY_3);
    }

    // ==================== 辅助：注册到物质世界附属标签 ====================

    private static void addToSpiritTab(DeferredItem<?> item) {
        MaterialWorldAPI.addToAddonTab(SPIRIT_TAB_GROUP, () -> new ItemStack(item.get()));
    }

    private static void addToSpiritTab(DeferredBlock<?> block) {
        MaterialWorldAPI.addToAddonTab(SPIRIT_TAB_GROUP, () -> new ItemStack(block.get()));
    }

    /** 兜底：自定义 ItemStack（带 NBT / 特殊属性） */
    private static void addToSpiritTab(Supplier<ItemStack> supplier) {
        MaterialWorldAPI.addToAddonTab(SPIRIT_TAB_GROUP, supplier);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Config.loadValues();
        LOGGER.info("精神世界：配置已加载");
    }
}