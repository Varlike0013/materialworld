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
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        // 开关瞬间 / 等级变化时上限缩小，clamp 精神力
        long effMax = data.getEffectiveMaxSpirit();
        if (data.getSpirit() > effMax) {
            data.setSpirit(effMax);
            data.markDirty();
        }
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
        handleSpiritSatiety(player, data);
        handleSpiritMaintenance(player, data);

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