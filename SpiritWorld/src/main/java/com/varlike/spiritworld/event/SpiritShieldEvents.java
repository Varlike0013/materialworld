package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 精神护盾：受到伤害时，将一定比例的伤害由精神力承担。
 * 使用 LivingDamageEvent.Pre，护甲/抗性结算之后、真正扣血之前介入。
 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public final class SpiritShieldEvents {

    private SpiritShieldEvents() {}

    /** 每级吸收比例 */
    public static final float RATIO_PER_LEVEL = SkillValues.SHIELD_RATIO_PER_LEVEL;
    /** 每点伤害消耗的精神力（1 精神力 = 1 伤害） */
    public static final float SPIRIT_PER_DAMAGE = SkillValues.SHIELD_SPIRIT_PER_DAMAGE;

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // /kill、虚空等不可减免伤害不吸收
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (data == null) return;

        int level = data.getSkillLevel(ModSkills.SPIRIT_SHIELD_ID);
        if (level <= 0) return;
        if (!data.isPassiveActive(ModSkills.SPIRIT_SHIELD_ID)) return;

        float damage = event.getNewDamage();
        if (damage <= 0.0f) return;

        long spirit = data.getSpirit();
        if (spirit <= 0L) return;

        // 计算要吸收的伤害和精神力
        float ratio = Math.min(1.0f, level * RATIO_PER_LEVEL);
        float toShield = damage * ratio;
        long spiritNeeded = (long) Math.ceil(toShield * SPIRIT_PER_DAMAGE);
        long actualSpent = Math.min(spirit, spiritNeeded);
        float actualShielded = actualSpent / SPIRIT_PER_DAMAGE;

        if (actualSpent <= 0L) return;

        // 扣精神力 + 减伤害
        data.consumeSpirit(actualSpent);
        event.setNewDamage(Math.max(0.0f, damage - actualShielded));

        // 同步到客户端（HUD 精神力条立即更新）
        NetworkHandler.syncTo(player);
    }
}