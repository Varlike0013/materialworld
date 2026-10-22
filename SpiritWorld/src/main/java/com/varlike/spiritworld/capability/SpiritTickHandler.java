package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        // 1. 基础恢复
        if (Config.spiritRegenInterval > 0
                && player.tickCount % Config.spiritRegenInterval == 0) {
            if (data.getSpirit() < data.getEffectiveMaxSpirit()) {
                double base = Config.spiritRegenAmount + data.getSkillRegenFlatBonus();
                double mult = 1.0 + data.getSkillRegenPercentBonus();
                data.addSpiritWithFraction(base * mult);
            }
        }

        // 2. 精神洪流：每秒回复上限百分比
        double flowRegen = data.getSpiritFlowRegenPerSecond();
        if (flowRegen > 0 && player.tickCount % 20 == 0) {
            if (data.getSpirit() < data.getEffectiveMaxSpirit()) {
                double amount = data.getEffectiveMaxSpirit() * flowRegen / 100.0;
                data.addSpiritWithFraction(amount);
            }
        }
        handleNightVision(player, data);

        // 3. 节流同步
        if (Config.syncInterval > 0
                && player.tickCount % Config.syncInterval == 0
                && data.isDirty()) {
            NetworkHandler.syncTo(player);
            data.clearDirty();
        }
        // 4. 定期把最新进度写进玩家 NBT（落盘由自动存档 / 登出 / 关服完成），
        //    避免崩溃时把最近一分钟的游玩进度整个丢掉；
        //    必须等登录流程读过存档再写，否则会拿默认值覆盖真实进度
        if (data.isPersistenceReady() && player.tickCount % 1200 == 0) {
            SpiritPersistence.save(player, data);
        }

    }
    private static void handleNightVision(ServerPlayer player, PlayerSpirit data) {
        int level = data.getSkillLevel(ModSkills.SPIRIT_NIGHT_VISION_ID);
        boolean active = level > 0 && data.isPassiveActive(ModSkills.SPIRIT_NIGHT_VISION_ID);

        if (!active) return;

        // 每 X 秒（100 tick）检查一次
        if (player.tickCount % SkillValues.NIGHT_VISION_CHECK_INTERVAL != 0) return;
        // 如果已有充足的夜视（>= 10 秒），被动让位，不消耗精神力
        MobEffectInstance existing = player.getEffect(MobEffects.NIGHT_VISION);
        if (existing != null && existing.getDuration() >= SkillValues.NIGHT_VISION_REFRESH_TICKS) return;
        // 计算 10 秒所需精神力：每秒消耗 base/level，10 秒 = base/level*10，向上取整
        double base = Config.nightVisionBaseCostPerSecond > 0
                ? Config.nightVisionBaseCostPerSecond
                : SkillValues.NIGHT_VISION_BASE_COST_PER_SECOND_DEFAULT;
        long cost = Math.max(1L, (long) Math.ceil(base / level * 10.0));

        if (!data.consumeSpirit(cost)) {
            // 精神力不足：关闭被动、移除效果、提示
            data.removePassive(ModSkills.SPIRIT_NIGHT_VISION_ID);
            player.removeEffect(MobEffects.NIGHT_VISION);
            NetworkHandler.syncTo(player);
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_night_vision.no_spirit")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 给 10 秒夜视（220 tick 含 1 秒缓冲，避免边界闪烁）
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
                SkillValues.NIGHT_VISION_REFRESH_TICKS + 20, 0, true, false, true));

        // 同步精神力到客户端（HUD 立即更新）
        NetworkHandler.syncTo(player);
    }
}