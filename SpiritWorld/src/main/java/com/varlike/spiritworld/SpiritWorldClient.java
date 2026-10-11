package com.varlike.spiritworld;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SpiritWorld.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldClient {

    public SpiritWorldClient(ModContainer container) {
        // 允许 NeoForge 生成配置界面
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // 注册客户端配置
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        SpiritWorld.LOGGER.info("精神世界：客户端初始化");
        SpiritWorld.LOGGER.info("玩家名：{}", Minecraft.getInstance().getUser().getName());
    }
}