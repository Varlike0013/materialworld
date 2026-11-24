package com.varlike.aetherui;

import com.mojang.logging.LogUtils;
import com.varlike.aetherui.network.AetherUINetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = AetherUIMod.MOD_ID)
public final class AetherUIMod {

    public static final String MOD_ID = "aetherui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AetherUIMod(IEventBus modBus, ModContainer container) {
        Config.register(container);
        modBus.addListener(AetherUINetwork::register);
    }
}
