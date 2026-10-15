package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class SkillRefundHelper {

    private SkillRefundHelper() {}

    /**
     * 重置单个技能，返还该技能及其下游技能已消耗的灵视点。
     * 下游技能指所有直接或间接依赖该技能的节点。
     * @return true 表示成功退点
     */
    public static boolean resetSkill(Player player, ResourceLocation skillId) {
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return false;

        if (data.getSkillLevel(skillId) <= 0) return false;

        SkillNode node = SkillTree.get(skillId);
        if (node == null) return false;

        // 1. 收集需要重置的技能：自身 + 所有下游
        List<ResourceLocation> toReset = collectWithDescendants(skillId);

        // 2. 累计退款
        long refund = 0L;
        for (ResourceLocation id : toReset) {
            int lvl = data.getSkillLevel(id);
            if (lvl <= 0) continue;
            SkillNode n = SkillTree.get(id);
            if (n == null) continue;

            refund += calculateTotalCost(n, lvl);
            data.setSkillLevel(id, 0);
        }

        // 3. 应用退款
        data.setTotalSpentVisionPoints(data.getTotalSpentVisionPoints() - refund);
        data.addVisionPoints(refund);

        if (player instanceof ServerPlayer sp) {
            NetworkHandler.syncTo(sp);
        }
        return true;
    }

    /**
     * 重置全部技能，返还所有已消耗灵视点。
     */
    public static boolean resetAllSkills(Player player) {
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return false;
        if (data.getLearnedSkills().isEmpty()) return false;

        long refund = data.getTotalSpentVisionPoints();
        data.clearLearnedSkills();
        data.setTotalSpentVisionPoints(0);
        data.addVisionPoints(refund);

        if (player instanceof ServerPlayer sp) {
            NetworkHandler.syncTo(sp);
        }
        return true;
    }

    // ==================== 内部工具 ====================

    /**
     * 计算某个技能从 0 升到 targetLevel 的总消耗。
     * 公式: level * baseCost + costIncrement * (level * (level - 1) / 2)
     */
    public static long calculateTotalCost(SkillNode node, int targetLevel) {
        if (targetLevel <= 0) return 0L;
        long linear = (long) targetLevel * node.getBaseCost();
        long incremental = node.getCostIncrement() * ((long) targetLevel * (targetLevel - 1) / 2);
        return linear + incremental;
    }

    /**
     * 收集目标技能自身 + 所有直接或间接依赖它的技能。
     * 使用 BFS 从目标节点向下遍历。
     */
    private static List<ResourceLocation> collectWithDescendants(ResourceLocation root) {
        List<ResourceLocation> result = new ArrayList<>();
        Deque<ResourceLocation> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            ResourceLocation current = queue.poll();
            if (result.contains(current)) continue;
            result.add(current);

            // 遍历所有节点，找到依赖 current 的
            for (SkillNode candidate : SkillTree.getAll()) {
                if (result.contains(candidate.getId())) continue;
                for (SkillNode.Prerequisite p : candidate.getPrerequisites()) {
                    if (p.id().equals(current)) {
                        queue.add(candidate.getId());
                        break;
                    }
                }
            }
        }
        return result;
    }
}