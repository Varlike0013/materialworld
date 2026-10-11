package com.varlike.materialworld;

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

@Mod(value = MaterialWorld.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MaterialWorld.MOD_ID, value = Dist.CLIENT)
public class MaterialWorldClient {

    public MaterialWorldClient(ModContainer container) {
        // 注册配置界面扩展点
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // 注册客户端配置
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MaterialWorld.LOGGER.info("物质世界：客户端初始化");
        MaterialWorld.LOGGER.info("玩家名：{}", Minecraft.getInstance().getUser().getName());
    }
}