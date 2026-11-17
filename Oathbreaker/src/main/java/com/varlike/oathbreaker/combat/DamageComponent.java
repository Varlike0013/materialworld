package com.varlike.oathbreaker.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 伤害构成的一段：类型 + 占本次命中的比例。
 *
 * <p>同一件武器或同一个技能可以有多段，例如 60% 物理 + 40% 魔法，
 * 或 100% 真实伤害。比例会在 {@link DamageProfile} 中归一化。</p>
 */
public record DamageComponent(DamageCategory type, double ratio) {
    public static final Codec<DamageComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DamageCategory.CODEC.fieldOf("type").forGetter(DamageComponent::type),
            Codec.DOUBLE.fieldOf("ratio").forGetter(DamageComponent::ratio)
    ).apply(instance, DamageComponent::new));
}
