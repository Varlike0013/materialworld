package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        // 1. 精神力恢复（使用有效上限作为判断）
        if (Config.spiritRegenInterval > 0
                && player.tickCount % Config.spiritRegenInterval == 0) {
            if (data.getSpirit() < data.getEffectiveMaxSpirit()) {
                data.addSpirit(Config.spiritRegenAmount);
            }
        }

        // 2. 节流同步
        if (Config.syncInterval > 0
                && player.tickCount % Config.syncInterval == 0
                && data.isDirty()) {
            NetworkHandler.syncTo(player);
            data.clearDirty();
        }
    }
}