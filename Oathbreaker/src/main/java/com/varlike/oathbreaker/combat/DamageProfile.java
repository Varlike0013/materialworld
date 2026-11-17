package com.varlike.oathbreaker.combat;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次命中/一个技能的伤害构成。
 *
 * <p>组件比例会合并同类并归一化到总和 1；空列表或非法比例回退为
 * 100% 物理。武器与技能共用这一数据结构。</p>
 */
public record DamageProfile(List<DamageComponent> components) {
    public static final DamageProfile PHYSICAL = new DamageProfile(
            List.of(new DamageComponent(DamageCategory.PHYSICAL, 1.0D)));

    public static final Codec<DamageProfile> CODEC = DamageComponent.CODEC.listOf()
            .xmap(DamageProfile::new, DamageProfile::components);

    public DamageProfile {
        Map<DamageCategory, Double> merged = new LinkedHashMap<>();
        for (DamageComponent component : components) {
            if (component != null && component.ratio() > 0.0D) {
                merged.merge(component.type(), component.ratio(), Double::sum);
            }
        }
        if (merged.isEmpty()) {
            merged.put(DamageCategory.PHYSICAL, 1.0D);
        }
        double total = merged.values().stream().mapToDouble(Double::doubleValue).sum();
        List<DamageComponent> normalized = new ArrayList<>(merged.size());
        merged.forEach((type, ratio) -> normalized.add(new DamageComponent(type, ratio / total)));
        components = List.copyOf(normalized);
    }

    public double ratioOf(DamageCategory type) {
        return components.stream()
                .filter(component -> component.type() == type)
                .mapToDouble(DamageComponent::ratio)
                .sum();
    }

    /** 占比最高的类型，用于面板值/调试显示；平局时按枚举顺序。 */
    public DamageCategory primaryType() {
        DamageComponent best = components.getFirst();
        for (DamageComponent component : components) {
            if (component.ratio() > best.ratio()) {
                best = component;
            }
        }
        return best.type();
    }

    public boolean isPure(DamageCategory type) {
        return components.size() == 1 && components.getFirst().type() == type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (DamageComponent component : components) {
            if (builder.length() > 0) {
                builder.append(" + ");
            }
            builder.append(Math.round(component.ratio() * 100.0D)).append("% ")
                    .append(component.type().getSerializedName());
        }
        return builder.toString();
    }
}
