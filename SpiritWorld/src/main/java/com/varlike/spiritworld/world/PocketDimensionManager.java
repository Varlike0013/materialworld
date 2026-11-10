package com.varlike.spiritworld.world;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.*;

public class PocketDimensionManager {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "pocket_world")
    );

    // 间距为 256
    public static final int SPACING = 256;

    // 记录玩家分配的领地中心
    private static final Map<UUID, BlockPos> PLAYER_CENTERS = new HashMap<>();
    private static final Set<BlockPos> OCCUPIED_CENTERS = new HashSet<>();
    private static final Map<UUID, Integer> DOMAIN_LEVELS = new HashMap<>();
    private static final List<PocketGenerationTask> ACTIVE_TASKS = new java.util.ArrayList<>();

    public static int getDomainLevel(UUID playerUUID) {
        return DOMAIN_LEVELS.getOrDefault(playerUUID, 0);
    }

    public static void setDomainLevel(UUID playerUUID, int level) {
        DOMAIN_LEVELS.put(playerUUID, level);
    }
    public static void queueTask(PocketGenerationTask task) {
        ACTIVE_TASKS.add(task);
    }
    public static void tickTasks() {
        if (ACTIVE_TASKS.isEmpty()) return;

        // 使用迭代器防止 ConcurrentModificationException
        java.util.Iterator<PocketGenerationTask> iterator = ACTIVE_TASKS.iterator();
        while (iterator.hasNext()) {
            PocketGenerationTask task = iterator.next();
            task.tick();
            if (task.isFinished()) {
                iterator.remove();
            }
        }
    }
    /** 获取或分配领地中心 */
    public static BlockPos getOrAssignCenter(UUID playerUUID, MinecraftServer server) {
        PocketDimensionData data = PocketDimensionData.get(server);

        // 1. 如果已经有记录，直接返回
        if (data.getPlayerCenters().containsKey(playerUUID)) {
            return data.getPlayerCenters().get(playerUUID);
        }

        // 2. 检查配置上限
        int maxPlayers = Config.pocketDimensionMaxActivePlayers;
        if (data.getOccupiedCenters().size() >= maxPlayers) {
            return null; // 已满
        }

        // 3. 螺旋寻找空位
        BlockPos center = findNextSpiralCenter(data.getOccupiedCenters());

        // 4. 记录并保存
        data.getOccupiedCenters().add(center);
        data.getPlayerCenters().put(playerUUID, center);
        data.setDirty(); // 标记为脏，让服务器在下一个 tick 将数据写进硬盘

        return center;
    }

    /** 螺旋矩阵寻找空位（需接收已占用的 Set） */
    private static BlockPos findNextSpiralCenter(Set<BlockPos> occupiedCenters) {
        if (occupiedCenters.isEmpty()) return BlockPos.ZERO;

        int x = 0, z = 0;
        int dx = 0, dz = -1;
        for (int i = 0; i < 1024; i++) {
            BlockPos center = new BlockPos(x * SPACING, 0, z * SPACING);
            if (!occupiedCenters.contains(center)) {
                return center;
            }
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
        return BlockPos.ZERO;
    }

    // ==================== 边界生成队列 ====================

    // 待生成的边界任务
    private static final List<BoundaryTask> TASKS = new ArrayList<>();

    public record BoundaryTask(BlockPos center, int radius, int height, boolean isRemoving) {}

    public static void queueBoundaryTask(BlockPos center, int radius, int height, boolean isRemoving) {
        TASKS.add(new BoundaryTask(center, radius, height, isRemoving));
    }

    /** 每个 tick 处理 1000 个方块，防止卡顿 */
    public static void tickTasks(Level level) {
        if (TASKS.isEmpty()) return;

        BoundaryTask task = TASKS.get(0);
        // 这里的生成逻辑放到稍后的 Handler 中执行
    }
}