package com.varlike.spiritworld.capability;

import com.varlike.materialworld.api.IPlayerEMC;
import com.varlike.materialworld.api.MaterialWorldAPI;
import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SkillTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        if (data.isPassiveActive(ModSkills.EMC_CONVERSION_ID)) {
            processEmcConversion(player, data);
        }
    }

    private static void processEmcConversion(ServerPlayer player, PlayerSpirit data) {
        int skillLevel = data.getSkillLevel(ModSkills.EMC_CONVERSION_ID);
        if (skillLevel <= 0) {
            data.removePassive(ModSkills.EMC_CONVERSION_ID);
            return;
        }

        // 间隔固定，不随等级变化
        int interval = Math.max(2, Config.emcConversionBaseInterval);
        if (player.tickCount % interval != 0) return;

        // 每碎片价格（含技能等级 + 视觉等级折扣）
        int visionLevel = data.getVisionLevel();
        long perFragment = SkillValues.emcPerFragment(skillLevel, visionLevel);

        // 本次想消耗的 EMC（基础值 × EMC 亲和折扣）
        long targetCost = Math.max(perFragment,
                (long) Math.ceil(Config.emcConversionEmcCost * data.getEmcCostMultiplier()))*skillLevel;

        // 产出碎片数 = 消耗 / 每碎片价格，至少 1
        long gain = Math.max(1L, targetCost / perFragment);

        // 实际消耗对齐到 perFragment 的整数倍，避免「白嫖」或丢精度
        long emcCost = gain * perFragment;

        IPlayerEMC emc = MaterialWorldAPI.getPlayerEMC(player);
        if (emc == null || emc.getBalance() < emcCost) {
            data.removePassive(ModSkills.EMC_CONVERSION_ID);
            NetworkHandler.syncTo(player);
            player.sendSystemMessage(Component.translatable(
                    "message.spiritworld.skill.emc_conversion.no_emc"));
            return;
        }

        emc.consumeBalance(emcCost);
        data.addVisionFragments(gain);
        data.markDirty();

        if (player.tickCount % Config.syncInterval == 0) {
            NetworkHandler.syncTo(player);
            data.clearDirty();
        }
    }
}