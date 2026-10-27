package com.varlike.spiritworld.capability;

import com.varlike.materialworld.api.IPlayerEMC;
import com.varlike.materialworld.api.MaterialWorldAPI;
import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import com.varlike.spiritworld.skill.SpiritFlightHandler;
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
public class SkillTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (data == null) return;

        // 每个技能一个门控
        if (data.isPassiveActive(ModSkills.EMC_CONVERSION_ID)) {
            processEmcConversion(player, data);
        }
        if (data.isPassiveActive(ModSkills.SPIRIT_NIGHT_VISION_ID)) {
            handleNightVision(player, data);
        }
        if (data.isPassiveActive(ModSkills.SPIRIT_SATIETY_ID)) {
            handleSpiritSatiety(player, data);
        }
        if (data.isPassiveActive(ModSkills.SPIRIT_MAINTENANCE_ID)) {
            handleSpiritMaintenance(player, data);
        }
        SpiritFlightHandler.tick(player, data);   // 内部已判 active
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
    private static void handleNightVision(ServerPlayer player, PlayerSpirit data) {
        int level = data.getSkillLevel(ModSkills.SPIRIT_NIGHT_VISION_ID);
        boolean active = level > 0 && data.isPassiveActive(ModSkills.SPIRIT_NIGHT_VISION_ID);

        if (!active) return;   // 关闭时不清效果，让 11 秒夜视自然过期

        // 每 5 秒检查一次
        if (player.tickCount % SkillValues.NIGHT_VISION_CHECK_INTERVAL != 0) return;

        // 剩余 >= 10 秒则不重复给药
        MobEffectInstance existing = player.getEffect(MobEffects.NIGHT_VISION);
        if (existing != null && existing.getDuration() >= SkillValues.NIGHT_VISION_REFRESH_TICKS) {
            return;
        }

        // 续夜视（220 tick = 11 秒，留 1 秒缓冲）
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                SkillValues.NIGHT_VISION_REFRESH_TICKS + 20,
                0, true, false, true));
    }
    private static void handleSpiritSatiety(ServerPlayer player, PlayerSpirit data) {
        int level = data.getSkillLevel(ModSkills.SPIRIT_SATIETY_ID);
        boolean active = level > 0 && data.isPassiveActive(ModSkills.SPIRIT_SATIETY_ID);
        if (!active) return;

        FoodData food = player.getFoodData();

        // 只在不足时设置，避免每 tick 无谓写入
        if (food.getFoodLevel() < SkillValues.SATIETY_FULL_FOOD) {
            food.setFoodLevel(SkillValues.SATIETY_FULL_FOOD);
        }
        if (food.getSaturationLevel() < SkillValues.SATIETY_FULL_SATURATION) {
            food.setSaturation(SkillValues.SATIETY_FULL_SATURATION);
        }
        // 清消耗累积，防止 exhaustion 触发饱食度下降
        if (food.getExhaustionLevel() > 0.0f) {
            food.setExhaustion(0.0f);
        }
    }
    private static void handleSpiritMaintenance(ServerPlayer player, PlayerSpirit data) {
        int lv = data.getSkillLevel(ModSkills.SPIRIT_MAINTENANCE_ID);
        boolean active = lv > 0 && data.isPassiveActive(ModSkills.SPIRIT_MAINTENANCE_ID);
        if (!active) return;

        // 每 20 tick（1 秒）触发一次
        if (player.tickCount % SkillValues.MAINTENANCE_INTERVAL != 0) return;

        int restore = SkillValues.maintenanceRestorePerSecond(lv);
        if (restore <= 0) return;

        // 遍历主背包 + 热键栏 + 装备槽 + 副手（getContainerSize 覆盖全部）
        List<ItemStack> damaged = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isDamageableItem() && stack.isDamaged()) damaged.add(stack);
        }
        if (damaged.isEmpty()) return;

        ItemStack target = damaged.get(player.getRandom().nextInt(damaged.size()));
        int current = target.getDamageValue();
        target.setDamageValue(Math.max(0, current - restore));
        data.markDirty();
    }
}