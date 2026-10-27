package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.PlayerSpirit;

/**
 * 精神力上限计算。
 * baseMax = (maxSpirit + Σfixed + Σfixed_skills) × (1 + Σpercent + Σpercent_skills)
 * effectiveMax = baseMax × (1 - 夜视惩罚) × (1 - 饱食惩罚)
 */
public final class SpiritMaxCalculator {

    private SpiritMaxCalculator() {}

    public static long baseMax(PlayerSpirit data) {
        long fixedMods = 0L;
        double percentMods = 0.0;
        for (PlayerSpirit.MaxSpiritModifier m : data.getMaxSpiritModifiers().values()) {
            fixedMods += m.fixed();
            percentMods += m.percent();
        }

        long flat = data.getMaxSpirit()
                + fixedMods
                + SkillBonusCalculator.maxSpiritFlat(data);
        double percent = percentMods
                + SkillBonusCalculator.maxSpiritPercent(data);

        return Math.max(0L, (long) (flat * (1.0 + percent)));
    }

    public static long effectiveMax(PlayerSpirit data) {
        long total = baseMax(data);

        if (data.isPassiveActive(ModSkills.SPIRIT_NIGHT_VISION_ID)) {
            int lv = data.getSkillLevel(ModSkills.SPIRIT_NIGHT_VISION_ID);
            double penalty = SkillValues.nightVisionMaxPenalty(lv);
            if (penalty > 0.0) total = (long) (total * (1.0 - penalty));
        }

        if (data.isPassiveActive(ModSkills.SPIRIT_SATIETY_ID)) {
            int lv = data.getSkillLevel(ModSkills.SPIRIT_SATIETY_ID);
            double penalty = SkillValues.satietyMaxPenalty(lv);
            if (penalty > 0.0) total = (long) (total * (1.0 - penalty));
        }

        return Math.max(0L, total);
    }
}