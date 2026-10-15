package com.varlike.spiritworld.capability;

import com.varlike.materialworld.api.IPlayerEMC;
import com.varlike.materialworld.api.MaterialWorldAPI;
import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
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

        int interval = Math.max(2, Config.emcConversionBaseInterval - (skillLevel - 1) * 3);
        if (player.tickCount % interval != 0) return;

        long emcCost = Config.emcConversionEmcCost * skillLevel;

        IPlayerEMC emc = MaterialWorldAPI.getPlayerEMC(player);
        if (emc == null || emc.getBalance() < emcCost) {
            data.removePassive(ModSkills.EMC_CONVERSION_ID);
            NetworkHandler.syncTo(player);
            player.sendSystemMessage(Component.translatable(
                    "message.spiritworld.skill.emc_conversion.no_emc"));
            return;
        }

        emc.consumeBalance(emcCost);

        int visionLevel = data.getVisionLevel();
        double rate = Config.emcConversionBaseRate
                * (1 + Config.emcConversionVisionBonus * visionLevel)
                * (1 + Config.emcConversionSkillBonus * (skillLevel - 1));

        long gain = Math.max(1, (long) Math.ceil(emcCost * rate));
        data.addVisionPoints(gain);
        data.markDirty();

        if (player.tickCount % Config.syncInterval == 0) {
            NetworkHandler.syncTo(player);
            data.clearDirty();
        }
    }
}