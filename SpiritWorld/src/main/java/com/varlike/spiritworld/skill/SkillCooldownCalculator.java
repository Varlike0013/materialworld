package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.PlayerSpirit;
import net.minecraft.resources.ResourceLocation;

public final class SkillCooldownCalculator {

    private SkillCooldownCalculator() {}

    public static double multiplier(PlayerSpirit data) {
        return SkillValues.cooldownMultiplier(
                data.getSkillLevel(ModSkills.COOLDOWN_REDUCTION_ID));
    }

    public static long effectiveTicks(PlayerSpirit data, ResourceLocation skillId) {
        SkillNode node = SkillTree.get(skillId);
        if (node == null) return 0L;
        long base = node.getBaseCooldownTicks();
        if (base <= 0L) return 0L;
        return (long) Math.ceil(base * multiplier(data));
    }
}