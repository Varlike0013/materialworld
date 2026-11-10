package com.varlike.spiritworld.world;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class PocketChunkLoader {

    // 记录当前处于活跃状态的玩家 UUID
    private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();

    // 记录每个活跃玩家当前加载的区块列表（方便退出时精准卸载）
    private static final Map<UUID, List<ChunkPos>> LOADED_CHUNKS = new HashMap<>();

    /** 检查是否还有空余名额 */
    public static boolean canJoinActiveList() {
        return ACTIVE_PLAYERS.size() < Config.pocketDimensionMaxActivePlayers;
    }

    public static boolean isActive(UUID uuid) {
        return ACTIVE_PLAYERS.contains(uuid);
    }

    /** 玩家进入领域 / 升级时，更新加载的区块 */
    public static void updateChunks(ServerPlayer player, BlockPos center, int level) {
        UUID uuid = player.getUUID();
        ServerLevel levelObj = player.server.getLevel(PocketDimensionManager.POCKET_DIMENSION);
        if (levelObj == null) return;

        // 如果玩家还没在活跃列表里，尝试加入
        if (!ACTIVE_PLAYERS.contains(uuid)) {
            if (!canJoinActiveList()) return; // 名额已满
            ACTIVE_PLAYERS.add(uuid);
        }

        // 先卸载旧区块
        unloadChunks(levelObj, uuid);

        // 计算新区块范围
        int radius = SkillValues.POCKET_SIZE_XZ[level] / 2;
        List<ChunkPos> newChunks = new ArrayList<>();

        // 将世界坐标转换为区块坐标（>> 4 相当于除以 16）
        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius - 1) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius - 1) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                newChunks.add(cp);
                levelObj.setChunkForced(cx, cz, true); // 强制加载
            }
        }

        LOADED_CHUNKS.put(uuid, newChunks);
        SpiritWorld.LOGGER.info("Player {} loaded {} chunks in pocket dimension.", player.getName().getString(), newChunks.size());
    }

    /** 玩家退出服务器时调用，卸载所有区块并释放名额 */
    public static void releaseChunks(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ServerLevel levelObj = player.server.getLevel(PocketDimensionManager.POCKET_DIMENSION);
        if (levelObj != null) {
            unloadChunks(levelObj, uuid);
        }
        ACTIVE_PLAYERS.remove(uuid);
        LOADED_CHUNKS.remove(uuid);
    }

    /** 内部方法：卸载记录过的区块 */
    private static void unloadChunks(ServerLevel level, UUID uuid) {
        List<ChunkPos> chunks = LOADED_CHUNKS.get(uuid);
        if (chunks != null) {
            for (ChunkPos cp : chunks) {
                level.setChunkForced(cp.x, cp.z, false); // 取消强制加载
            }
        }
    }
}