package com.varlike.oathbreaker.combat;

import com.varlike.oathbreaker.OathbreakerConfig;
import com.varlike.oathbreaker.core.attribute.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 服务端计算出的玩家战斗面板，用于 HUD、调试命令与网络同步。
 *
 * <p>数值尺度仍沿用 Minecraft（20 点生命），但字段与计算方式对齐 LoL：
 * AD/AP、护甲/魔抗、暴击、穿透、技能急速、吸血、生命/法力回复。</p>
 */
public record CombatStats(
        float health,
        float maxHealth,
        float mana,
        float maxMana,
        float attackDamage,
        float abilityPower,
        float armor,
        float magicResist,
        float attackSpeed,
        float abilityHaste,
        float critChance,
        float critDamage,
        float armorPenetrationFlat,
        float armorPenetrationPercent,
        float magicPenetrationFlat,
        float magicPenetrationPercent,
        float healthRegeneration,
        float manaRegeneration,
        float lifesteal,
        float omnivamp,
        int foodLevel
) {
    public static CombatStats capture(ServerPlayer player) {
        float maxMana = (float) player.getAttributeValue(ModAttributes.MAX_MANA);
        return new CombatStats(
                player.getHealth(),
                player.getMaxHealth(),
                maxMana, // P2 尚无消耗 Mana 的技能，当前按满蓝显示；P3 接入 RunState.currentMana
                maxMana,
                (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE),
                (float) player.getAttributeValue(ModAttributes.ABILITY_POWER),
                (float) (player.getAttributeValue(Attributes.ARMOR) + OathbreakerConfig.BASE_ARMOR.get()),
                (float) player.getAttributeValue(ModAttributes.MAGIC_RESIST),
                (float) player.getAttributeValue(Attributes.ATTACK_SPEED),
                (float) player.getAttributeValue(ModAttributes.ABILITY_HASTE),
                (float) player.getAttributeValue(ModAttributes.CRIT_CHANCE),
                (float) player.getAttributeValue(ModAttributes.CRIT_DAMAGE),
                (float) player.getAttributeValue(ModAttributes.ARMOR_PENETRATION),
                (float) player.getAttributeValue(ModAttributes.ARMOR_PENETRATION_PERCENT),
                (float) player.getAttributeValue(ModAttributes.MAGIC_PENETRATION),
                (float) player.getAttributeValue(ModAttributes.MAGIC_PENETRATION_PERCENT),
                (float) player.getAttributeValue(ModAttributes.HEALTH_REGENERATION),
                (float) player.getAttributeValue(ModAttributes.MANA_REGENERATION),
                (float) player.getAttributeValue(ModAttributes.LIFESTEAL),
                (float) player.getAttributeValue(ModAttributes.OMNIVAMP),
                player.getFoodData().getFoodLevel()
        );
    }
}
