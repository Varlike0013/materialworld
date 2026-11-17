package com.varlike.oathbreaker.core.player;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.OathbreakerConfig;
import com.varlike.oathbreaker.core.attribute.AttributeModifiers;
import com.varlike.oathbreaker.core.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 玩家 LoL 风格基础面板。
 *
 * <p>登录、重生、跨维度、死亡克隆时用固定 ID 永久修饰符施加：
 * 最大生命 ×倍率、攻击力 ×倍率、魔抗 +30、最大法力 +300、法力回复 +15/5s、生命回复 +5/5s。
 * 护甲基础值不写成 armor 修饰符（原版 armor 上限 30，会被装备截断），
 * 而是在伤害公式与面板快照中按“原版护甲 + baseArmor”计算。
 * 数值来自 {@link OathbreakerConfig} 的 {@code base_stats} 分组。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class PlayerBaseStats {
    private static final ResourceLocation BASE_MAX_HEALTH_ID = AttributeModifiers.id("base/max_health_multiplier");
    private static final ResourceLocation BASE_ATTACK_ID = AttributeModifiers.id("base/attack_damage_multiplier");
    private static final ResourceLocation BASE_MAGIC_RESIST_ID = AttributeModifiers.id("base/magic_resist");
    private static final ResourceLocation BASE_MAX_MANA_ID = AttributeModifiers.id("base/max_mana");
    private static final ResourceLocation BASE_MANA_REGEN_ID = AttributeModifiers.id("base/mana_regeneration");
    private static final ResourceLocation BASE_HEALTH_REGEN_ID = AttributeModifiers.id("base/health_regeneration");

    private PlayerBaseStats() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity(), true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClone(PlayerEvent.Clone event) {
        apply(event.getEntity(), false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        apply(event.getEntity(), true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        apply(event.getEntity(), false);
    }

    private static void apply(Player player, boolean fillHealth) {
        applyTotalMultiplier(player, Attributes.MAX_HEALTH, BASE_MAX_HEALTH_ID,
                OathbreakerConfig.PLAYER_HEALTH_MULTIPLIER.get());
        applyTotalMultiplier(player, Attributes.ATTACK_DAMAGE, BASE_ATTACK_ID,
                OathbreakerConfig.PLAYER_ATTACK_MULTIPLIER.get());
        AttributeModifiers.applyPermanent(player, ModAttributes.MAGIC_RESIST, BASE_MAGIC_RESIST_ID,
                OathbreakerConfig.BASE_MAGIC_RESIST.get(), AttributeModifier.Operation.ADD_VALUE);
        AttributeModifiers.applyPermanent(player, ModAttributes.MAX_MANA, BASE_MAX_MANA_ID,
                OathbreakerConfig.BASE_MAX_MANA.get(), AttributeModifier.Operation.ADD_VALUE);
        AttributeModifiers.applyPermanent(player, ModAttributes.MANA_REGENERATION, BASE_MANA_REGEN_ID,
                OathbreakerConfig.BASE_MANA_REGENERATION.get(), AttributeModifier.Operation.ADD_VALUE);
        AttributeModifiers.applyPermanent(player, ModAttributes.HEALTH_REGENERATION, BASE_HEALTH_REGEN_ID,
                OathbreakerConfig.BASE_HEALTH_REGENERATION.get(), AttributeModifier.Operation.ADD_VALUE);

        if (fillHealth && player.getHealth() < player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyTotalMultiplier(Player player, Holder<Attribute> attribute,
                                             ResourceLocation id, double multiplier) {
        AttributeModifiers.applyPermanent(player, attribute, id, Math.max(0.0D, multiplier - 1.0D),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
