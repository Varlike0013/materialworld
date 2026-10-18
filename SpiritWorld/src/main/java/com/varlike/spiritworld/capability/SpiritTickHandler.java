package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
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
}