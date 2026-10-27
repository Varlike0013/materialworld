package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.PlayerSpirit;

/**
 * 技能加成计算：从技能等级 → 加成数值。纯函数，不持有状态。
 * 所有方法接收 PlayerSpirit 参数，便于未来天赋叠加扩展。
 */
public final class SkillBonusCalculator {

    private SkillBonusCalculator() {}

    public static long maxSpiritFlat(PlayerSpirit data) {
        return data.getSkillLevel(ModSkills.SPIRIT_ENDURANCE_ID)
                * SkillValues.SPIRIT_ENDURANCE_MAX_PER_LEVEL;
    }

    public static double maxSpiritPercent(PlayerSpirit data) {
        double bonus = data.getSkillLevel(ModSkills.SPIRIT_STRENGTH_ID)
                * SkillValues.SPIRIT_STRENGTH_MAX_PERCENT_PER_LEVEL;
        if (data.getSkillLevel(ModSkills.SPIRIT_SOURCE_ID) > 0) {
            bonus += SkillValues.SPIRIT_SOURCE_MAX_PERCENT;
        }
        return bonus;
    }

    public static double regenFlat(PlayerSpirit data) {
        return data.getSkillLevel(ModSkills.SPIRIT_TOUGHNESS_ID)
                * SkillValues.SPIRIT_TOUGHNESS_REGEN_PER_LEVEL;
    }

    public static double regenPercent(PlayerSpirit data) {
        double bonus = data.getSkillLevel(ModSkills.SPIRIT_SURGE_ID)
                * SkillValues.SPIRIT_SURGE_REGEN_PERCENT_PER_LEVEL;
        if (data.getSkillLevel(ModSkills.SPIRIT_SOURCE_ID) > 0) {
            bonus += SkillValues.SPIRIT_SOURCE_REGEN_PERCENT;
        }
        return bonus;
    }

    public static double flowRegenPerSecond(PlayerSpirit data) {
        return data.getSkillLevel(ModSkills.SPIRIT_FLOW_ID)
                * SkillValues.SPIRIT_FLOW_REGEN_PER_SECOND_PER_LEVEL;
    }

    public static long visionFragmentFlat(PlayerSpirit data) {
        return data.getSkillLevel(ModSkills.VISION_SHARPNESS_ID)
                * SkillValues.VISION_SHARPNESS_FRAGMENTS_PER_LEVEL;
    }

    public static double visionFragmentPercent(PlayerSpirit data) {
        return data.getSkillLevel(ModSkills.VISION_MASTERY_ID)
                * SkillValues.VISION_MASTERY_FRAGMENT_PERCENT_PER_LEVEL;
    }

    public static double emcCostMultiplier(PlayerSpirit data) {
        double reduction = data.getSkillLevel(ModSkills.EMC_AFFINITY_ID)
                * SkillValues.EMC_AFFINITY_COST_REDUCTION_PER_LEVEL;
        return Math.max(0.0, 1.0 - reduction);
    }
}