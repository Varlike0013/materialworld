package com.varlike.oathbreaker.combat;

import net.minecraft.util.Mth;

/**
 * LoL 风格伤害公式。
 *
 * <p>抗性减伤：{@code multiplier = 100 / (100 + resistance)}；
 * 负抗性按 LoL 的放大公式 {@code 2 - 100 / (100 - resistance)} 处理。
 * 穿透先算百分比、再算固定值，且不会把抗性压到 0 以下。</p>
 */
public final class CombatMath {
    private CombatMath() {}

    public static float mitigate(float damage, double resistance) {
        if (damage <= 0.0F) {
            return 0.0F;
        }
        double multiplier = resistance >= 0.0D
                ? 100.0D / (100.0D + resistance)
                : 2.0D - 100.0D / (100.0D - resistance);
        return (float) (damage * multiplier);
    }

    public static double effectiveResistance(double resistance, double percentPenetration, double flatPenetration) {
        double afterPercent = resistance * (1.0D - Mth.clamp(percentPenetration, 0.0D, 1.0D));
        return Math.max(0.0D, afterPercent - Math.max(0.0D, flatPenetration));
    }
}
