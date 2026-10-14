package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 技能退点接口。
 * 供精神世界自己的物品（如退点道具）调用，不对外暴露。
 */
public final class SkillRefundHelper {

    private SkillRefundHelper() {}

    /**
     * 重置玩家全部技能，返还所有已消耗灵视点。
     * @return true 表示成功退点，false 表示玩家无数据或无技能可退
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

    /**
     * 重置单个技能，返还该技能已消耗的灵视点。
     * @return true 表示成功退点
     */
    public static boolean resetSkill(Player player, ResourceLocation skillId) {
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return false;

        int level = data.getSkillLevel(skillId);
        if (level <= 0) return false;

        SkillNode node = SkillTree.get(skillId);
        if (node == null) return false;

        long refund = level * node.getCostPerLevel();
        data.setSkillLevel(skillId, 0);
        data.setTotalSpentVisionPoints(data.getTotalSpentVisionPoints() - refund);
        data.addVisionPoints(refund);

        if (player instanceof ServerPlayer sp) {
            NetworkHandler.syncTo(sp);
        }
        return true;
    }
}