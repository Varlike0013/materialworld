package com.varlike.materialworld.client;

import com.varlike.materialworld.MaterialWorld;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class ClientEvents {

    @EventBusSubscriber(modid = MaterialWorld.MOD_ID)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.OPEN_TRANSMUTATION);
        }
    }

    @EventBusSubscriber(modid = MaterialWorld.MOD_ID)
    public static class GameBus {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;

            while (KeyBindings.OPEN_TRANSMUTATION.consumeClick()) {
                mc.setScreen(new TransmutationScreen());
            }
        }
    }
}