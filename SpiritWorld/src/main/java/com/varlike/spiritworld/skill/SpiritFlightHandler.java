package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 精神飞行状态机：
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 被动关闭 / 未学 / 创造 / 旁观 → 清理飞行状态，返回           │
 * └─────────────────────────────────────────────────────────────┘
 *              ↓ 被动激活且非创造/旁观
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 缓降中？                                                     │
 * │   是：spirit ≥ baseCost × GLIDE_EXIT_SECONDS → 退出缓降     │
 * │       否则继续 Slow Falling，return                          │
 * │   否：进入正常飞行消耗                                       │
 * └─────────────────────────────────────────────────────────────┘
 *              ↓ 正常飞行
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 按 rising（deltaMovement.y > 0.05）选消耗倍率                │
 * │ 累积 flightCostAccum，满 1 扣精神力                          │
 * │ 精神力耗尽 → 进入缓降（mayFly=false + Slow Falling + 提示）  │
 * │ 否则确保 mayFly=true + 节流同步                              │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 */
public final class SpiritFlightHandler {

    private SpiritFlightHandler() {}

    /** 每次 tick 调用；内部处理所有状态切换 */
    public static void tick(ServerPlayer player, PlayerSpirit data) {
        int lv = data.getSkillLevel(ModSkills.SPIRIT_FLIGHT_ID);
        boolean active = lv > 0 && data.isPassiveActive(ModSkills.SPIRIT_FLIGHT_ID);

        // ==== 1. 被动关闭：清理一切 ====
        if (!active) {
            if (!player.isCreative() && !player.isSpectator()) {
                SkillHelper.setFlightState(player, false, false);
                player.removeEffect(MobEffects.SLOW_FALLING);
            }
            data.setFlightGliding(false);
            data.setFlightCostAccum(0.0);
            return;
        }

        // ==== 2. 创造 / 旁观：不干预 ====
        if (player.isCreative() || player.isSpectator()) return;

        double baseCost = SkillValues.flightCostPerSecond(lv)
                * Config.spiritFlightCostMultiplier;
        if (baseCost <= 0.0) return;

        long spirit = data.getSpirit();

        // ==== 3. 缓降状态检查 ====
        if (data.isFlightGliding()) {
            if (spirit >= baseCost * SkillValues.FLIGHT_GLIDE_EXIT_SECONDS) {
                // 精神力恢复，退出缓降
                data.setFlightGliding(false);
                player.removeEffect(MobEffects.SLOW_FALLING);
                SkillHelper.setMayFly(player, true);
                player.onUpdateAbilities();
            } else {
                // 仍在缓降：禁止飞行 + 续 Slow Falling
                if (player.getAbilities().flying) {
                    SkillHelper.setFlying(player, false);
                    player.onUpdateAbilities();
                }
                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, 40, 0, true, false, true));
                return;
            }
        }

        // ==== 4. 正常飞行消耗 ====
        boolean rising = player.getAbilities().flying
                && player.getDeltaMovement().y > 0.05;
        double costPerSecond = baseCost
                * (rising ? SkillValues.FLIGHT_RISING_MULTIPLIER : 1.0);
        double costPerTick = costPerSecond / 20.0;

        data.setFlightCostAccum(data.getFlightCostAccum() + costPerTick);
        while (data.getFlightCostAccum() >= 1.0) {
            if (!data.consumeSpirit(1L)) {
                // 精神力耗尽 → 进入缓降
                data.setFlightCostAccum(0.0);
                data.setFlightGliding(true);
                SkillHelper.setFlightState(player, false, false);
                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, 200, 0, true, false, true));
                NetworkHandler.syncTo(player);
                player.displayClientMessage(
                        Component.translatable("message.spiritworld.spirit_flight.low_spirit")
                                .withStyle(ChatFormatting.RED), true);
                return;
            }
            data.setFlightCostAccum(data.getFlightCostAccum() - 1.0);
        }

        // ==== 5. 确保允许飞行 ====
        if (!SkillHelper.mayFly(player)) {
            SkillHelper.setMayFly(player, true);
            player.onUpdateAbilities();
        }

        // ==== 6. 节流同步 ====
        if (player.tickCount % Config.syncInterval == 0) {
            NetworkHandler.syncTo(player);
            data.markDirty();
        }
    }
}