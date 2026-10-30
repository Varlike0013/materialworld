package com.varlike.spiritworld.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {

    // 记录每个维度下所有的节点： Map<维度, Map<坐标, 节点类型>>
    private static final Map<ResourceKey<Level>, Map<BlockPos, NodeType>> ALL_NODES = new ConcurrentHashMap<>();

    // 离线影子缓存： Map<聚集器坐标, 影子数据>
    private static final Map<BlockPos, OfflineCollectorData> SHADOW_DATA = new ConcurrentHashMap<>();

    // ==================== 节点注册与注销 ====================

    public static void registerNode(Level level, BlockPos pos, NodeType type) {
        ALL_NODES.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>()).put(pos.immutable(), type);
    }

    public static void unregisterNode(Level level, BlockPos pos) {
        Map<BlockPos, NodeType> nodes = ALL_NODES.get(level.dimension());
        if (nodes != null) {
            nodes.remove(pos);
        }
        // 如果是聚集器，同步清理影子数据
        SHADOW_DATA.remove(pos);
    }

    public static Map<BlockPos, NodeType> getNodesInLevel(Level level) {
        return ALL_NODES.getOrDefault(level.dimension(), new HashMap<>());
    }

    // ==================== 影子数据操作 ====================

    public static void updateShadowData(BlockPos pos, OfflineCollectorData data) {
        SHADOW_DATA.put(pos.immutable(), data);
    }

    public static OfflineCollectorData getShadowData(BlockPos pos) {
        return SHADOW_DATA.get(pos);
    }
}