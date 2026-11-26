package com.varlike.aetherui;

import com.varlike.aetherui.core.render.ScreenRenderHandler;
import com.varlike.aetherui.core.tick.ClientTickHandler;
import com.varlike.aetherui.demo.BossBarDemo;
import com.varlike.aetherui.demo.BuffHud;
import com.varlike.aetherui.demo.CustomHud;
import com.varlike.aetherui.demo.DemoUI;
import com.varlike.aetherui.demo.SmeltingIconDemo;
import com.varlike.aetherui.demo.TargetInfoHud;
import com.varlike.aetherui.network.InspectCache;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/** 客户端入口：注册 GUI layer、HUD、demo 与配置界面。 */
@Mod(value = AetherUIMod.MOD_ID, dist = Dist.CLIENT)
public class AetherUIClient {

    public AetherUIClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(ScreenRenderHandler::registerLayer);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        AetherUIMod.LOGGER.info("AetherUI client setup; user={}",
                Minecraft.getInstance().getUser().getName());
        NeoForge.EVENT_BUS.addListener(ClientTickHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(AetherUIClient::onClientTick);
        CustomHud.init();
        BuffHud.init();
        TargetInfoHud.init();
        applyDemoConfig();
    }

    /** 每帧轮询配置，Demo 开关无需重启。 */
    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            InspectCache.clear();
            return;
        }
        applyDemoConfig();
        CustomHud.update();
        BuffHud.update();
        SmeltingIconDemo.onClientTick(event);
    }

    private static void applyDemoConfig() {
        if (Config.showDemo()) DemoUI.show();
        else DemoUI.hide();

        if (Config.showBossBarDemo()) BossBarDemo.show();
        else BossBarDemo.destroy();

        if (Config.showSmeltingIconDemo()) SmeltingIconDemo.show();
        else SmeltingIconDemo.destroy();
    }
}
