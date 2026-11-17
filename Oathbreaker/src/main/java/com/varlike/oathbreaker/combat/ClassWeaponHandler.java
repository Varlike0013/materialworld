package com.varlike.oathbreaker.combat;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.OathbreakerConfig;
import com.varlike.oathbreaker.core.classification.ModTags;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import com.varlike.oathbreaker.core.weapon.WeaponPool;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 职业武器适配裁决。
 *
 * <p>1.21.1 已没有设计文档里写的 {@code LivingHurtEvent}：伤害事件拆成
 * {@code LivingIncomingDamageEvent}（护甲前）与 {@link LivingDamageEvent.Pre}（护甲后、吸收前）。
 * “最终伤害强制为 1”必须在护甲之后执行，因此监听 Pre 并使用 LOWEST 优先级，
 * 保证覆盖此前所有乘区；后续如果我们自己的易伤/暴击也要作用，需在更早的优先级完成。</p>
 *
 * <p>与设计文档的示例一致：只对 Monster 目标生效，玩家之间的 PvP 不受职业惩罚。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class ClassWeaponHandler {
    private ClassWeaponHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!event.getSource().is(ModTags.DamageTypes.PHYSICAL)) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        WeaponProfile profile = WeaponPool.resolveProfile(weapon);
        if (profile == null || profile.isUsableBy(player)) {
            return;
        }

        event.setNewDamage(1.0F);

        if (OathbreakerConfig.DEBUG_LOGGING.get()) {
            Oathbreaker.LOGGER.debug(
                    "Class weapon mismatch: player={} weapon={} category={} damage={} target={} -> 1.0",
                    player.getGameProfile().getName(),
                    BuiltInRegistries.ITEM.getKey(weapon.getItem()),
                    profile.category(),
                    profile.damage(),
                    BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()));
        }
    }
}
