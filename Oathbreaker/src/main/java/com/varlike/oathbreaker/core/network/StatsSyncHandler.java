package com.varlike.oathbreaker.core.network;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.combat.CombatStats;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 玩家面板同步。
 *
 * <p>登录、重生、跨维度、装备变化时立即推送；每 10 tick 做一次差分检查，
 * 属性/生命/饥饿变化才发包，避免每 tick 全量发送。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class StatsSyncHandler {
    private static final Map<UUID, CombatStats> LAST_SENT = new HashMap<>();
    private static int tickCounter;

    private StatsSyncHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, true);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, true);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, true);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 10 != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            send(player, false);
        }
    }

    private static void send(ServerPlayer player, boolean force) {
        CombatStats stats = CombatStats.capture(player);
        if (!force && stats.equals(LAST_SENT.get(player.getUUID()))) {
            return;
        }
        LAST_SENT.put(player.getUUID(), stats);
        PacketDistributor.sendToPlayer(player, PlayerStatsPayload.from(stats));
    }
}
