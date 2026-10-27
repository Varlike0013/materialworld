package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillHelper;
import com.varlike.spiritworld.skill.SkillValues;
import com.varlike.spiritworld.skill.SpiritFlightHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (data == null) return;

        // 1. clamp
        long effMax = data.getEffectiveMaxSpirit();
        if (data.getSpirit() > effMax) {
            data.setSpirit(effMax);
            data.markDirty();
        }

        // 2. 基础恢复
        if (Config.spiritRegenInterval > 0 && player.tickCount % Config.spiritRegenInterval == 0) {
            if (data.getSpirit() < data.getEffectiveMaxSpirit()) {
                double base = Config.spiritRegenAmount + data.getSkillRegenFlatBonus();
                double mult = 1.0 + data.getSkillRegenPercentBonus();
                data.addSpiritWithFraction(base * mult);
            }
        }

        // 3. 精神洪流
        double flowRegen = data.getSpiritFlowRegenPerSecond();
        if (flowRegen > 0 && player.tickCount % 20 == 0) {
            if (data.getSpirit() < data.getEffectiveMaxSpirit()) {
                data.addSpiritWithFraction(data.getEffectiveMaxSpirit() * flowRegen);
            }
        }

        // 4. 节流同步
        if (Config.syncInterval > 0 && player.tickCount % Config.syncInterval == 0 && data.isDirty()) {
            NetworkHandler.syncTo(player);
            data.clearDirty();
        }

        // 5. 每 1200 tick 落盘
        if (data.isPersistenceReady() && player.tickCount % 1200 == 0) {
            SpiritPersistence.save(player, data);
        }
    }
}