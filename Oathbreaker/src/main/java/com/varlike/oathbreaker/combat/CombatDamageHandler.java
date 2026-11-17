package com.varlike.oathbreaker.combat;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.OathbreakerConfig;
import com.varlike.oathbreaker.core.attribute.ModAttributes;
import com.varlike.oathbreaker.core.classification.ModTags;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import com.varlike.oathbreaker.core.weapon.WeaponPool;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * LoL 风格伤害结算。
 *
 * <p>用 {@link LivingDamageEvent.Pre#getOriginalDamage()}（护甲前原始伤害）重新计算最终伤害，
 * 因此不再叠加原版护甲/保护附魔的减伤。流程：</p>
 * <ol>
 *     <li>玩家造成伤害：读取 {@link DamageProfile}，按物理/魔法/真实拆成多段分别减伤。</li>
 *     <li>物理段可暴击；三段共用同一次暴击判定。</li>
 *     <li>穿透先百分比后固定，抗性最低压到 0。</li>
 *     <li>玩家受到伤害：按伤害类型 Tag 分类，套用同一减伤公式。</li>
 *     <li>伤害后处理：生命偷取按物理占比，全能吸血作用于全部；随后触发命中效果。</li>
 * </ol>
 *
 * <p>职业武器 1 点伤害的裁决在 {@link ClassWeaponHandler} 的 LOWEST 优先级执行，
 * 会覆盖本类算出的最终值。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class CombatDamageHandler {
    private CombatDamageHandler() {}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer attacker) {
            applyOutgoing(event, attacker);
        } else if (event.getEntity() instanceof ServerPlayer victim) {
            applyIncoming(event, victim);
        }
    }

    private static void applyOutgoing(LivingDamageEvent.Pre event, ServerPlayer attacker) {
        if (attacker == event.getEntity()) {
            return;
        }
        // 只处理战斗伤害；thorns、爆炸、环境伤害等继续走原版逻辑。
        if (!event.getSource().is(ModTags.DamageTypes.PHYSICAL)
                && !event.getSource().is(ModTags.DamageTypes.MAGIC)
                && !event.getSource().is(ModTags.DamageTypes.TRUE)) {
            return;
        }
        float raw = event.getOriginalDamage();
        if (raw <= 0.0F) {
            return;
        }

        DamageProfile damageProfile = resolveDamageProfile(attacker, event.getSource());

        boolean critical = false;
        if (damageProfile.ratioOf(DamageCategory.PHYSICAL) > 0.0D) {
            double critChance = attacker.getAttributeValue(ModAttributes.CRIT_CHANCE);
            critical = critChance > 0.0D && attacker.getRandom().nextFloat() < critChance;
        }
        double critMultiplier = critical ? attacker.getAttributeValue(ModAttributes.CRIT_DAMAGE) : 1.0D;

        float finalDamage = 0.0F;
        for (DamageComponent component : damageProfile.components()) {
            float componentDamage = raw * (float) component.ratio();
            if (component.type() == DamageCategory.PHYSICAL && critical) {
                componentDamage *= (float) critMultiplier;
            }
            double resistance = switch (component.type()) {
                case PHYSICAL -> armorOf(event.getEntity());
                case MAGIC -> event.getEntity().getAttributeValue(ModAttributes.MAGIC_RESIST);
                case TRUE -> 0.0D;
            };
            double percentPenetration = switch (component.type()) {
                case PHYSICAL -> attacker.getAttributeValue(ModAttributes.ARMOR_PENETRATION_PERCENT);
                case MAGIC -> attacker.getAttributeValue(ModAttributes.MAGIC_PENETRATION_PERCENT);
                case TRUE -> 0.0D;
            };
            double flatPenetration = switch (component.type()) {
                case PHYSICAL -> attacker.getAttributeValue(ModAttributes.ARMOR_PENETRATION);
                case MAGIC -> attacker.getAttributeValue(ModAttributes.MAGIC_PENETRATION);
                case TRUE -> 0.0D;
            };
            double effectiveResistance = CombatMath.effectiveResistance(
                    resistance, percentPenetration, flatPenetration);
            finalDamage += CombatMath.mitigate(componentDamage, effectiveResistance);
        }

        event.setNewDamage(finalDamage);
        debug("outgoing", attacker, event.getEntity().getName().getString(),
                damageProfile, critical, raw, finalDamage);
    }

    private static void applyIncoming(LivingDamageEvent.Pre event, ServerPlayer victim) {
        DamageCategory category = DamageCategory.fromSource(event.getSource());
        if (category == null) {
            return;
        }
        float raw = event.getOriginalDamage();
        if (raw <= 0.0F) {
            return;
        }

        double resistance = switch (category) {
            case PHYSICAL -> armorOf(victim);
            case MAGIC -> victim.getAttributeValue(ModAttributes.MAGIC_RESIST);
            case TRUE -> 0.0D;
        };
        float finalDamage = CombatMath.mitigate(raw, resistance);
        event.setNewDamage(finalDamage);

        debug("incoming", victim, victim.getName().getString(),
                new DamageProfile(List.of(new DamageComponent(category, 1.0D))),
                false, raw, finalDamage);
    }

    /** 伤害应用后的吸血与命中效果。 */
    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (attacker == event.getEntity()) {
            return;
        }
        if (!event.getSource().is(ModTags.DamageTypes.PHYSICAL)
                && !event.getSource().is(ModTags.DamageTypes.MAGIC)
                && !event.getSource().is(ModTags.DamageTypes.TRUE)) {
            return;
        }
        float dealt = event.getNewDamage();
        if (dealt <= 0.0F) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        DamageProfile damageProfile = resolveDamageProfile(attacker, event.getSource());

        double lifesteal = attacker.getAttributeValue(ModAttributes.LIFESTEAL)
                * damageProfile.ratioOf(DamageCategory.PHYSICAL);
        double omnivamp = attacker.getAttributeValue(ModAttributes.OMNIVAMP);
        double heal = dealt * (lifesteal + omnivamp);
        if (heal > 0.0D) {
            attacker.heal((float) heal);
        }

        // 武器命中效果只由物理普攻/射击触发；魔法/真实技能走技能自己的命中接口。
        if (event.getSource().is(ModTags.DamageTypes.PHYSICAL)) {
            WeaponPool.fireHit(attacker, event.getEntity(), weapon, damageProfile, dealt, event.getSource());
        }
    }

    /** 击杀时给命中效果一个可选的 onKill 回调。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (attacker == event.getEntity()) {
            return;
        }
        if (!event.getSource().is(ModTags.DamageTypes.PHYSICAL)) {
            return;
        }
        ItemStack weapon = attacker.getMainHandItem();
        DamageProfile damageProfile = resolveDamageProfile(attacker, event.getSource());
        WeaponPool.fireKill(attacker, event.getEntity(), weapon, damageProfile, event.getSource());
    }

    /**
     * 玩家造成伤害的伤害构成：
     * 物理来源（普攻/箭/三叉戟）读武器 profile；魔法/真实来源（技能）按来源 Tag 单独成段。
     */
    private static DamageProfile resolveDamageProfile(ServerPlayer attacker, DamageSource source) {
        WeaponProfile weaponProfile = WeaponPool.resolveProfile(attacker.getMainHandItem());
        if (weaponProfile != null && source.is(ModTags.DamageTypes.PHYSICAL)) {
            return weaponProfile.damage();
        }
        DamageCategory category = DamageCategory.fromSource(source);
        if (category != null) {
            return new DamageProfile(List.of(new DamageComponent(category, 1.0D)));
        }
        return weaponProfile != null ? weaponProfile.damage() : DamageProfile.PHYSICAL;
    }

    private static void debug(String direction, Player attacker, String targetName,
                              DamageProfile profile, boolean critical, float raw, float finalDamage) {
        if (OathbreakerConfig.DEBUG_LOGGING.get()) {
            Oathbreaker.LOGGER.debug(
                    "LoL damage {}: attacker={} target={} profile={} crit={} raw={} final={}",
                    direction, attacker.getGameProfile().getName(), targetName, profile, critical, raw, finalDamage);
        }
    }

    /** 原版 armor 上限 30，基础值直接在公式里加，避免装备被 30 上限截断。 */
    private static double armorOf(LivingEntity entity) {
        double armor = entity.getAttributeValue(Attributes.ARMOR);
        return entity instanceof Player ? armor + OathbreakerConfig.BASE_ARMOR.get() : armor;
    }
}
