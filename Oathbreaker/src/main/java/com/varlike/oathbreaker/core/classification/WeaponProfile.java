package com.varlike.oathbreaker.core.classification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.varlike.oathbreaker.combat.DamageCategory;
import com.varlike.oathbreaker.combat.DamageProfile;
import com.varlike.oathbreaker.core.player.PlayerClassData;
import com.varlike.oathbreaker.core.player.PlayerClasses;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * 武器分类配置（DataMap {@code oathbreaker:weapon_profile}）。
 *
 * <p>由玩家职业决定武器是否可用，魔神不参与；没有 weapon_profile 的物品（空手、方块、
 * 未标记的第三方武器）不受职业限制。</p>
 *
 * <p>{@code required_classes} 与 {@code class_tags} 之间是“或”关系：
 * 玩家当前职业命中任一 required class，或拥有任一 class tag，即视为可用。
 * 两者都为空表示无职业限制。</p>
 *
 * <p>{@code damage} 支持多段伤害，例如 70% 物理 + 30% 魔法；缺省为 100% 物理。
 * 比例会在 {@link DamageProfile} 中归一化。</p>
 */
public record WeaponProfile(
        String category,
        DamageProfile damage,
        Set<ResourceLocation> requiredClasses,
        Set<ResourceLocation> classTags,
        double rangedScale
) {
    public static final String CATEGORY_MELEE = "melee";
    public static final String CATEGORY_HEAVY_MELEE = "heavy_melee";
    public static final String CATEGORY_RANGED = "ranged";
    public static final String CATEGORY_MAGIC = "magic";
    public static final String CATEGORY_OTHER = "other";

    private static final Codec<Set<ResourceLocation>> CLASS_SET_CODEC =
            ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

    public static final Codec<WeaponProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("category", CATEGORY_OTHER).forGetter(WeaponProfile::category),
            DamageProfile.CODEC.optionalFieldOf("damage", DamageProfile.PHYSICAL).forGetter(WeaponProfile::damage),
            CLASS_SET_CODEC.optionalFieldOf("required_classes", Set.of()).forGetter(WeaponProfile::requiredClasses),
            CLASS_SET_CODEC.optionalFieldOf("class_tags", Set.of()).forGetter(WeaponProfile::classTags),
            Codec.DOUBLE.optionalFieldOf("ranged_scale", 1.0D).forGetter(WeaponProfile::rangedScale)
    ).apply(instance, WeaponProfile::new));

    public WeaponProfile {
        requiredClasses = Set.copyOf(requiredClasses);
        classTags = Set.copyOf(classTags);
        if (category == null || category.isBlank()) {
            category = CATEGORY_OTHER;
        }
        if (damage == null) {
            damage = DamageProfile.PHYSICAL;
        }
    }

    public boolean isRanged() {
        return CATEGORY_RANGED.equals(category);
    }

    public boolean isUnrestricted() {
        return requiredClasses.isEmpty() && classTags.isEmpty();
    }

    public boolean hasMagicComponent() {
        return damage.ratioOf(DamageCategory.MAGIC) > 0.0D;
    }

    public boolean isUsableBy(Player player) {
        if (isUnrestricted()) {
            return true;
        }
        PlayerClassData data = PlayerClasses.get(player);
        boolean classMatch = data.activeClass().map(requiredClasses::contains).orElse(false);
        boolean tagMatch = data.hasAnyTag(classTags);
        return classMatch || tagMatch;
    }
}
