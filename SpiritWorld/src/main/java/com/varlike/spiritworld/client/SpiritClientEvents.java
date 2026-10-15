package com.varlike.spiritworld.client;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.skill.RadialMenuScreen;
import com.varlike.spiritworld.client.skill.SkillLoadoutScreen;
import com.varlike.spiritworld.client.skill.SkillTreeScreen;
import com.varlike.spiritworld.network.ServerboundUseSkillPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class SpiritClientEvents {

    @EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(SpiritKeyBindings.OPEN_SKILL_TREE);
            event.register(SpiritKeyBindings.OPEN_LOADOUT);
            event.register(SpiritKeyBindings.OPEN_RADIAL);
            event.register(SpiritKeyBindings.USE_SKILL);
        }
    }

    @EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (mc.screen != null) return;

            while (SpiritKeyBindings.OPEN_SKILL_TREE.consumeClick()) {
                mc.setScreen(new SkillTreeScreen());
            }
            while (SpiritKeyBindings.OPEN_LOADOUT.consumeClick()) {
                mc.setScreen(new SkillLoadoutScreen());
            }
            while (SpiritKeyBindings.OPEN_RADIAL.consumeClick()) {
                mc.setScreen(new RadialMenuScreen());
            }
            while (SpiritKeyBindings.USE_SKILL.consumeClick()) {
                PacketDistributor.sendToServer(new ServerboundUseSkillPacket());
            }
        }
    }
}