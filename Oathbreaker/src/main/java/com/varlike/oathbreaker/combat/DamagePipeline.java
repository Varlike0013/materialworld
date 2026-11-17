package com.varlike.oathbreaker.combat;

import com.varlike.oathbreaker.core.attribute.ModAttributes;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import com.varlike.oathbreaker.core.weapon.WeaponPool;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 伤害管线骨架。
 *
 * <p>设计文档 4.3 把伤害分成两段：攻击方面板值、防御方修正。当前只落地前半程的入口
 * {@link #panelValue(Player, ItemStack)} 与“是否受职业限制”的判定；护甲穿透、护甲、魔抗、
 * 暴击、易伤等阶段在 P2 接入，且必须保持“UI 只读同一份面板值”的约束。</p>
 */
public final class DamagePipeline {
    private DamagePipeline() {}

    /**
     * 前半程：攻击方面板值。
     *
     * <p>对齐 LoL：物理武器显示 AD（原版 attack_damage），法术武器显示 AP。
     * 该值用于 HUD/Tooltip 显示，不包含目标护甲/魔抗。</p>
     */
    public static float panelValue(Player player, ItemStack weapon) {
        WeaponProfile profile = WeaponPool.resolveProfile(weapon);
        if (profile != null && profile.damage().primaryType() == DamageCategory.MAGIC) {
            return (float) player.getAttributeValue(ModAttributes.ABILITY_POWER);
        }
        return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    /** 无 weapon_profile 的武器视为不受职业限制。 */
    public static boolean isUsable(Player player, ItemStack weapon) {
        WeaponProfile profile = WeaponPool.resolveProfile(weapon);
        return profile == null || profile.isUsableBy(player);
    }
}
