package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家进度落盘工具。
 *
 * <p>进度直接写进玩家自己的持久化 NBT（NeoForge 的 NeoForgeData，随
 * {@code playerdata/<uuid>.dat} 一起保存），所以退出重进、服务器重启、
 * 切换维度都不会丢失，也不需要额外的世界存档文件或手动维护 UUID 表。</p>
 */
public final class SpiritPersistence {

    /** 玩家持久化 NBT 中的键名 */
    private static final String KEY = SpiritWorld.MOD_ID + ":player_spirit";

    private SpiritPersistence() {}

    /** 把内存中的进度写入玩家 NBT */
    public static boolean save(Player player) {
        return save(player, player.getCapability(ModCapabilities.PLAYER_SPIRIT));
    }

    public static boolean save(Player player, PlayerSpirit data) {
        if (player == null || data == null) return false;

        player.getPersistentData().put(KEY, data.save());

        if (SpiritWorld.LOGGER.isDebugEnabled()) {
            SpiritWorld.LOGGER.debug("已保存玩家 {} 的进度：{} 项技能，可用灵视点 {}，总消耗 {}，灵视碎片 {}",
                    player.getScoreboardName(),
                    data.getLearnedSkills().size(),
                    data.getAvailableVisionPoints(),
                    data.getTotalSpentVisionPoints(),
                    data.getVisionFragments());
        }
        return true;
    }

    /** 从玩家 NBT 恢复进度；该玩家没有存档时返回 false 并保留当前默认值 */
    public static boolean load(Player player) {
        return load(player, player.getCapability(ModCapabilities.PLAYER_SPIRIT));
    }

    public static boolean load(Player player, PlayerSpirit data) {
        if (player == null || data == null) return false;

        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) return false;

        CompoundTag tag = root.getCompound(KEY);
        if (tag.isEmpty()) return false;

        data.load(tag);

        if (SpiritWorld.LOGGER.isDebugEnabled()) {
            SpiritWorld.LOGGER.debug("已恢复玩家 {} 的进度：{} 项技能，可用灵视点 {}，总消耗 {}，灵视碎片 {}",
                    player.getScoreboardName(),
                    data.getLearnedSkills().size(),
                    data.getAvailableVisionPoints(),
                    data.getTotalSpentVisionPoints(),
                    data.getVisionFragments());
        }
        return true;
    }

    /** 玩家是否有可恢复的存档 */
    public static boolean hasSavedData(Player player) {
        if (player == null) return false;
        CompoundTag root = player.getPersistentData();
        return root.contains(KEY) && !root.getCompound(KEY).isEmpty();
    }
}
