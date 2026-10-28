package com.varlike.spiritworld;

import com.varlike.spiritworld.client.SpiritCoreScreen;
import com.varlike.spiritworld.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SpiritWorld.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldClient {

    public SpiritWorldClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 客户端设置阶段加载客户端配置
        ClientConfig.loadValues();

        SpiritWorld.LOGGER.info("精神世界：客户端初始化");
        SpiritWorld.LOGGER.info("玩家名：{}", Minecraft.getInstance().getUser().getName());
    }
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SPIRIT_CORE.get(), SpiritCoreScreen::new);
    }
}