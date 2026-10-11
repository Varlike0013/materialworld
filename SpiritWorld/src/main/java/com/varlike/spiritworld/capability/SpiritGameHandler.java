package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritGameHandler {

    /**
     * 玩家死亡重生时：
     * 1. 复制基础上限、灵视等级
     * 2. 清空所有临时 modifier
     * 3. 按配置清空精神力
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        PlayerSpirit oldData = event.getOriginal().getCapability(ModCapabilities.PLAYER_SPIRIT);
        PlayerSpirit newData = event.getEntity().getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (oldData == null || newData == null) return;

        // 1. 复制全部数据（含基础上限、灵视等级、当前精神力）
        newData.copyFrom(oldData);

        // 2. 清空临时 modifier —— 死亡后 buff 不保留
        newData.clearMaxSpiritModifiers();

        // 3. 按配置清空精神力
        if (Config.spiritClearOnDeath) {
            newData.setSpirit(0L);
        }

        newData.markDirty();
    }

    /**
     * 玩家登录时：
     * 1. 按配置清空精神力
     * 2. 向客户端同步一次
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        PlayerSpirit data = sp.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data != null) {
            if (Config.spiritClearOnLogin) {
                data.setSpirit(0L);
            }
            // 主动同步一次，保证客户端数据与服务端一致
            NetworkHandler.syncTo(sp);
            data.clearDirty();
        }
    }

    /** 玩家登出时清理内存缓存 */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ModCapabilities.PROVIDER.remove(sp.getUUID());
            SpiritWorld.LOGGER.debug("已清理玩家 {} 的精神力缓存", sp.getUUID());
        }
    }
}