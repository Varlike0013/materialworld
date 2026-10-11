package com.varlike.spiritworld;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SpiritWorld.MOD_ID)
public class SpiritWorld {

    public static final String MOD_ID = "spiritworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpiritWorld(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("精神世界：初始化中...");

        // 注册能力
        modEventBus.addListener(com.varlike.spiritworld.capability.ModCapabilities::register);

        // 注册网络
        // NetworkHandler 使用 @EventBusSubscriber 自动注册，无需在此手动调用

        // 注册物品与创造标签
        //com.varlike.spiritworld.registry.ModItems.register(modEventBus);
        //com.varlike.spiritworld.registry.ModCreativeTabs.register(modEventBus);

        // 注册配置
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,
                com.varlike.spiritworld.Config.SPEC);
    }
}