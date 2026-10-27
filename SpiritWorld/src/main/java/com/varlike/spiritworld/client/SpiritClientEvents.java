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
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
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

        /** 是否正处于某个世界中（用于检测“退出世界”这个瞬间） */
        private static boolean inWorld = false;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            // 离开世界时清掉客户端缓存，避免切存档后残留上一个世界的进度
            if (mc.level == null) {
                if (inWorld) {
                    inWorld = false;
                    ClientSpiritData.reset();
                }
                return;
            }
            inWorld = true;

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
        /** 客户端登出：清空扫描图标缓存 */
        @SubscribeEvent
        public static void onScanLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientScanData.clear();
        }
        /** 客户端重生 / 切维度：LocalPlayer 被替换，旧图标失效 */
        @SubscribeEvent
        public static void onScanClone(ClientPlayerNetworkEvent.Clone event) {
            ClientScanData.clear();
        }
    }
}