package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritGameHandler {

    /**
     * 玩家死亡重生时：
     * 1. 复制技能等级、灵视点、技能栏、灵视碎片等全部进度
     * 2. 清空所有临时 modifier —— 死亡后 buff 不保留
     * 3. 按配置清空精神力
     * 4. 立即落盘，避免重生后又崩溃导致进度丢失
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        PlayerSpirit oldData = event.getOriginal().getCapability(ModCapabilities.PLAYER_SPIRIT);
        PlayerSpirit newData = event.getEntity().getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (oldData == null || newData == null) return;

        // 1. 复制全部数据（含技能等级、可用灵视点、已消耗灵视点）
        newData.copyFrom(oldData);

        // 2. 清空临时 modifier —— 死亡后 buff 不保留
        newData.clearMaxSpiritModifiers();

        // 3. 按配置清空精神力
        if (Config.spiritClearOnDeath) {
            newData.setSpirit(0L);
        }

        newData.markDirty();
        newData.markPersistenceReady();

        // 4. 立刻写入玩家 NBT
        if (event.getEntity() instanceof ServerPlayer player) {
            SpiritPersistence.save(player, newData);
        }
    }

    /**
     * 玩家登录时：
     * 1. 先从玩家 NBT 恢复进度（技能、灵视点、技能栏、被动、灵视碎片……）
     * 2. 按配置决定是否清空精神力
     * 3. 同步给客户端，并写回一次（新玩家由此建立存档）
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        boolean restored = SpiritPersistence.load(player, data);
        if (!restored) {
            SpiritWorld.LOGGER.debug("玩家 {} 没有历史进度，使用初始数据",
                    player.getScoreboardName());
        }

        if (Config.spiritClearOnLogin) {
            data.setSpirit(0L);
        }
        if (data.isPassiveActive(ModSkills.SPIRIT_FLIGHT_ID)
                && !player.isCreative() && !player.isSpectator()
                && data.getSpirit() >= 1L) {
            SkillHelper.setMayFly(player, true);
            player.onUpdateAbilities();
        }
        // 标记为“已接上存档”，此后才允许把内存数据写回 NBT，
        // 防止登录握手阶段用默认值覆盖真实存档
        data.markPersistenceReady();
        data.markDirty();

        // 主动同步一次，保证客户端数据与服务端一致
        NetworkHandler.syncTo(player);
        data.clearDirty();

        // 立即落盘：老玩家同步一次，新玩家建立初始存档
        SpiritPersistence.save(player, data);

    }

    /** 玩家登出时：落盘，然后清理内存缓存 */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data != null && data.isPersistenceReady()) {
                SpiritPersistence.save(player, data);
            }
            ModCapabilities.PROVIDER.remove(player.getUUID());
            SpiritWorld.LOGGER.debug("已清理玩家 {} 的精神力缓存", player.getUUID());
        }
    }

    /**
     * 服务器关闭前再写一遍：不依赖登出事件的触发顺序，
     * 保证正常关服时所有在线玩家的进度都已落盘。
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data != null && data.isPersistenceReady()) {
                SpiritPersistence.save(player, data);
            }
        }
        SpiritWorld.LOGGER.debug("关服前已保存所有在线玩家的精神世界进度");
    }
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (data == null) return;

        if (data.isPassiveActive(ModSkills.SPIRIT_FLIGHT_ID)
                && !player.isCreative() && !player.isSpectator()
                && data.getSpirit() >= 1L) {
            SkillHelper.setMayFly(player, true);
            player.onUpdateAbilities();
        }
    }
}
