package com.varlike.oathbreaker.combat;

import com.mojang.serialization.Codec;
import com.varlike.oathbreaker.core.classification.ModTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

/**
 * LoL 风格的三类伤害。
 *
 * <p>物理伤害走护甲，魔法伤害走魔抗，真实伤害无视抗性。
 * 数据包里的 {@code weapon_profile.damage_type} 与伤害类型 Tag 都使用这里的字符串。</p>
 */
public enum DamageCategory implements StringRepresentable {
    PHYSICAL("physical"),
    MAGIC("magic"),
    TRUE("true");

    public static final Codec<DamageCategory> CODEC = StringRepresentable.fromEnum(DamageCategory::values);

    private final String name;

    DamageCategory(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** 从伤害类型 Tag 推断；未标记的环境伤害返回 null，表示继续走原版逻辑。 */
    @Nullable
    public static DamageCategory fromSource(DamageSource source) {
        if (source.is(ModTags.DamageTypes.TRUE)) {
            return TRUE;
        }
        if (source.is(ModTags.DamageTypes.MAGIC)) {
            return MAGIC;
        }
        if (source.is(ModTags.DamageTypes.PHYSICAL)) {
            return PHYSICAL;
        }
        return null;
    }
}
