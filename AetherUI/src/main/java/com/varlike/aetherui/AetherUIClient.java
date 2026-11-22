package com.varlike.aetherui;

import com.varlike.aetherui.core.render.ScreenRenderHandler;
import com.varlike.aetherui.core.tick.ClientTickHandler;
import com.varlike.aetherui.demo.BuffHud;
import com.varlike.aetherui.demo.CustomHud;
import com.varlike.aetherui.demo.DemoUI;
import com.varlike.aetherui.demo.TargetInfoHud;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
        CustomHud.init();
        BuffHud.init();
        TargetInfoHud.init();
        if (Config.showDemo()) DemoUI.show();
    }
}
