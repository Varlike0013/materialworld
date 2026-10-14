package com.varlike.spiritworld.client;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.skill.SkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class SpiritClientEvents {

    @EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(SpiritKeyBindings.OPEN_SKILL_TREE);
        }
    }

    @EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            while (SpiritKeyBindings.OPEN_SKILL_TREE.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new SkillTreeScreen());
                }
            }
        }
    }
}