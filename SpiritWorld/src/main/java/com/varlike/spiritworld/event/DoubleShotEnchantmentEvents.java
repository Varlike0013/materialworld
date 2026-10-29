package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

/**
 * 双发快射：
 * <ul>
 *   <li>固定射出 1 根小箭，附魔等级只提升小箭伤害</li>
 *   <li>Lv1 = 25%，Lv2 = 50%，Lv3 = 75%（上限 75%，避免超过主箭）</li>
 *   <li>小箭不可拾取，箭矢类型继承原弹药（普通/药水/光灵/魔力合金等）</li>
 * </ul>
 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public final class DoubleShotEnchantmentEvents {

    private DoubleShotEnchantmentEvents() {}

    /** 延迟 2 tick = 0.1 秒 */
    private static final int DELAY_TICKS = 2;
    /** 附魔最大等级 */
    private static final int MAX_LEVEL = 3;
    /** 每级小箭伤害比例：下标 0 占位，Lv1=0.25, Lv2=0.50, Lv3=0.75 */
    private static final double[] DAMAGE_RATIO_BY_LEVEL = { 0.0, 0.25, 0.50, 0.75 };

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (event.getLevel().isClientSide()) return;
        if (!event.hasAmmo()) return;

        ItemStack bow = event.getBow();
        int level = getDoubleShotLevel(serverPlayer, bow);
        if (level <= 0) return;

        int clampedLevel = Math.min(level, MAX_LEVEL);
        double damageRatio = DAMAGE_RATIO_BY_LEVEL[clampedLevel];
        if (damageRatio <= 0.0) return;

        ItemStack ammo = player.getProjectile(event.getBow()).copy();
        int charge = event.getCharge();
        float power = BowItem.getPowerForTime(charge);
        if (power < 0.1f) return;

        MinecraftServer server = serverPlayer.server;
        server.tell(new TickTask(server.getTickCount() + DELAY_TICKS, () -> {
            if (!serverPlayer.isAlive()) return;
            fireSecondaryArrow(serverPlayer, ammo, power, damageRatio);
        }));
    }

    private static int getDoubleShotLevel(ServerPlayer player, ItemStack bow) {
        var registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var holder = registry.getHolder(ModEnchantments.DOUBLE_SHOT).orElse(null);
        if (holder == null) return 0;

        return EnchantmentHelper.getItemEnchantmentLevel(holder, bow);
    }

    /** 射出 1 根小箭，伤害 = 主箭伤害 × damageRatio */
    private static void fireSecondaryArrow(ServerPlayer player, ItemStack ammo,
                                           float power, double damageRatio) {
        Level level = player.level();
        ItemStack weapon = player.getMainHandItem();

        AbstractArrow arrow = createArrowFromAmmo(level, ammo, player, weapon);
        if (arrow == null) return;

        // 伤害 = 基础伤害 × 比例
        arrow.setBaseDamage(arrow.getBaseDamage() * damageRatio);
        // 不可拾取
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        // 满弦暴击
        if (power == 1.0f) arrow.setCritArrow(true);

        // 和主箭完全相同方向（无偏移），保证"后发同轨"
        arrow.shootFromRotation(player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                power * 3.0F,
                1.0F);

        level.addFreshEntity(arrow);

        // 轻微音效，避免太吵
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                0.6f, 1.2f);
    }

    /**
     * 按原弹药类型创建箭实体：
     * <ul>
     *   <li>普通箭 → {@link Arrow}</li>
     *   <li>药水箭 → {@link Arrow}（携带药水效果）</li>
     *   <li>光灵箭 → {@link Arrow}（带发光效果）</li>
     *   <li>魔力合金箭 → {@link com.varlike.spiritworld.entity.ManaAlloyArrow}</li>
     *   <li>其他 mod 的 ArrowItem → 由该 mod 的 createArrow 决定</li>
     * </ul>
     */
    private static AbstractArrow createArrowFromAmmo(Level level, ItemStack ammo,
                                                     ServerPlayer player, ItemStack weapon) {
        if (ammo.getItem() instanceof ArrowItem arrowItem) {
            return arrowItem.createArrow(level, ammo, player, weapon);
        }
        LivingEntity shooter = player;
        return new Arrow(level, shooter);
    }
}