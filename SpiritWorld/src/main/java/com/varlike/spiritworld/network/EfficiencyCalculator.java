package com.varlike.spiritworld.network;

import net.minecraft.core.BlockPos;

public class EfficiencyCalculator {

    /**
     * 获取节点的最大连接距离
     */
    public static double getMaxRange(NodeType type) {
        return switch (type) {
            case CORE, RELAY_1 -> 100.0;
            case RELAY_2 -> 200.0;
            case RELAY_3 -> 300.0;
            default -> 0.0;
        };
    }

    /**
     * 计算两个坐标之间的段效率
     * @param from 父节点类型
     * @param pos1 父节点坐标
     * @param pos2 子节点坐标
     * @return 效率 (0.1 ~ 1.0)，如果超距返回 -1.0
     */
    public static double calculateSegmentEfficiency(NodeType from, BlockPos pos1, BlockPos pos2) {
        double maxRange = getMaxRange(from);
        if (maxRange <= 0) return -1.0;

        double dist = Math.sqrt(pos1.distSqr(pos2));
        if (dist > maxRange) return -1.0;

        double losslessRange = maxRange * 0.35;

        // 1. 如果在无损距离内，效率 100%
        if (dist <= losslessRange) {
            return 1.0;
        }

        // 2. 超出无损距离，按比例衰减。到最大距离时效率为 10% (损耗 90%)
        double ratio = (dist - losslessRange) / (maxRange - losslessRange);
        double efficiency = 1.0 - (0.9 * ratio);

        // 保留两位小数，防止浮点数精度问题
        return Math.max(0.1, Math.round(efficiency * 100.0) / 100.0);
    }
}