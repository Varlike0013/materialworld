package com.varlike.oathbreaker.core.classification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 怪物分类配置（DataMap {@code oathbreaker:mob_profile}）。
 *
 * <p>P1 只定义数据结构与 JSON 契约；真正的阶段缩放、护甲缩放、技能抗性在 P3 落地。</p>
 */
public record MobProfile(
        String tier,
        String stageScaling,
        double skillResistance,
        boolean scaleArmor
) {
    public static final String TIER_COMMON = "common";
    public static final String TIER_ELITE = "elite";
    public static final String TIER_BOSS = "boss";

    public static final String CURVE_COMMON = "common_curve";
    public static final String CURVE_ELITE = "elite_curve";
    public static final String CURVE_BOSS = "boss_curve";

    public static final Codec<MobProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("tier", TIER_COMMON).forGetter(MobProfile::tier),
            Codec.STRING.optionalFieldOf("stage_scaling", CURVE_COMMON).forGetter(MobProfile::stageScaling),
            Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("skill_resistance", 0.0D).forGetter(MobProfile::skillResistance),
            Codec.BOOL.optionalFieldOf("scale_armor", false).forGetter(MobProfile::scaleArmor)
    ).apply(instance, MobProfile::new));

    public MobProfile {
        if (tier == null || tier.isBlank()) {
            tier = TIER_COMMON;
        }
        if (stageScaling == null || stageScaling.isBlank()) {
            stageScaling = CURVE_COMMON;
        }
    }

    public boolean isBoss() {
        return TIER_BOSS.equals(tier);
    }

    public boolean isElite() {
        return TIER_ELITE.equals(tier);
    }
}
