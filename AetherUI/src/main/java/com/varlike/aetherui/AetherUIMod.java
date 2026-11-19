package com.varlike.aetherui;

import com.mojang.logging.LogUtils;
import com.varlike.aetherui.core.tick.ClientTickHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = AetherUIMod.MOD_ID)
public final class AetherUIMod {

    public static final String MOD_ID = "aetherui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AetherUIMod(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // 后续阶段：注册渲染器、注册 /aetherui 命令、注册 demo
        NeoForge.EVENT_BUS.addListener(ClientTickHandler::onClientTick);
    }
}