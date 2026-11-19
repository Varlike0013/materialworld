package com.varlike.aetherui.core.tick;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ClientTickHandler {

    private ClientTickHandler() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) return;
        // Phase 6 之前为空
    }
}