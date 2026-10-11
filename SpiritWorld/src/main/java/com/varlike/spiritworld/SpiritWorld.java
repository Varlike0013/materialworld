package com.varlike.spiritworld;

import com.mojang.logging.LogUtils;
import com.varlike.materialworld.api.MaterialWorldAPI;
import com.varlike.spiritworld.capability.ModCapabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(SpiritWorld.MOD_ID)
public class SpiritWorld {

    public static final String MOD_ID = "spiritworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpiritWorld(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("精神世界：初始化中...");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModCapabilities::register);
//        ModItems.register(modEventBus);
//
//        MaterialWorldAPI.addToAddonTab(
//                "materialworld.addon_group.spirit_world",
//                () -> new ItemStack(ModItems.BOTTLED_SPIRIT.get()));

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Config.loadValues();
        LOGGER.info("精神世界：配置已加载");
    }
}