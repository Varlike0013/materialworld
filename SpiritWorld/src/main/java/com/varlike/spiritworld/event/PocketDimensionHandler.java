package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import com.varlike.spiritworld.world.PocketChunkLoader;
import com.varlike.spiritworld.world.PocketDimensionManager;
import com.varlike.spiritworld.world.PocketGenerationTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class PocketDimensionHandler {

    public static void toggleDimension(ServerPlayer player, PlayerSpirit data) {
        if (player.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            teleportOut(player, data);
        } else {
            teleportIn(player, data);
        }
    }

    private static void teleportIn(ServerPlayer player, PlayerSpirit data) {
        BlockPos center = PocketDimensionManager.getOrAssignCenter(player.getUUID(), player.server);
        if (center == null) {
            player.displayClientMessage(Component.translatable("message.spiritworld.pocket_dimension.full").withStyle(net.minecraft.ChatFormatting.RED), true);
            return;
        }

        // 记录主世界返回点
        data.setReturnPoint(player.getX(), player.getY(), player.getZ(), player.level().dimension().location().toString());

        int skillLevel = data.getSkillLevel(ModSkills.POCKET_DIMENSION_ID);
        int generatedLevel = PocketDimensionManager.getDomainLevel(player.getUUID());
        // 检查活跃名额
        if (!PocketChunkLoader.isActive(player.getUUID()) && !PocketChunkLoader.canJoinActiveList()) {
            player.displayClientMessage(Component.literal("当前同时运行的领域已达上限，请稍后再试").withStyle(net.minecraft.ChatFormatting.RED), true);
            return;
        }
        // 强制加载区块并占用名额
        PocketChunkLoader.updateChunks(player, center, skillLevel);

        ServerLevel pocketLevel = player.server.getLevel(PocketDimensionManager.POCKET_DIMENSION);
        if (pocketLevel == null) return;

        // 快速生成脚下 9x9 安全站台（瞬间完成，防止掉虚空）
        if (generatedLevel == 0) {
            int safeRadius = 4;
            for (int dx = -safeRadius; dx <= safeRadius; dx++) {
                for (int dz = -safeRadius; dz <= safeRadius; dz++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        BlockPos dp = center.offset(dx, dy, dz);
                        if (pocketLevel.getBlockState(dp).isAir()) {
                            pocketLevel.setBlockAndUpdate(dp, Blocks.DIRT.defaultBlockState());
                        }
                    }
                    BlockPos grassPos = center.offset(dx, 4, dz);
                    if (pocketLevel.getBlockState(grassPos).isAir()) {
                        pocketLevel.setBlockAndUpdate(grassPos, Blocks.GRASS_BLOCK.defaultBlockState());
                    }
                    BlockPos barrier = center.offset(dx, -1, dz);
                    if (pocketLevel.getBlockState(barrier).isAir()) {
                        pocketLevel.setBlockAndUpdate(barrier, Blocks.BARRIER.defaultBlockState());
                    }
                }
            }
        }

        // 提交异步生成任务（生成其余部分）
        if (skillLevel > generatedLevel) {
            PocketDimensionManager.queueTask(new PocketGenerationTask(pocketLevel, center, generatedLevel, skillLevel));
            PocketDimensionManager.setDomainLevel(player.getUUID(), skillLevel);
        }

        // 传送：如果是第一次进入用中心点，否则用记忆位置
        double x = data.getPocketX();
        double y = data.getPocketY();
        double z = data.getPocketZ();

        // 如果是首次进入，默认坐标是 CENTE + 0.5, 5.0, 0.5
        if (generatedLevel == 0 || data.getPocketY() < center.getY()) {
            x = center.getX() + 0.5;
            y = center.getY() + 5.0;
            z = center.getZ() + 0.5;
        }

        // 确保玩家绝不会卡在 Y=4 及以下的方块里
        if (y <= center.getY() + 4.5) {  y = center.getY() + 5.0;}

        if (y < -10) { x = center.getX() + 0.5; y = 5.0; z = center.getZ() + 0.5; }

        player.teleportTo(pocketLevel, x, y, z, data.getPocketYaw(), data.getPocketPitch());
        player.displayClientMessage(Component.translatable("message.spiritworld.pocket_dimension.entered"), true);
    }

    private static void teleportOut(ServerPlayer player, PlayerSpirit data) {
        // 记录领域内离开位置
        data.setPocketPosition(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        if (!data.hasReturnPoint()) {
            // 无记录：送回重生点或主世界中心
            ServerLevel overworld = player.server.overworld();
            BlockPos respawn = player.getRespawnPosition() != null ? player.getRespawnPosition() : overworld.getSharedSpawnPos();
            player.teleportTo(overworld, respawn.getX() + 0.5, respawn.getY() + 1, respawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        } else {
            // 有记录：返回原位
            ResourceLocation dimKey = ResourceLocation.parse(data.getReturnDim());
            ServerLevel targetLevel = player.server.getLevel(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION, dimKey));
            if (targetLevel == null) targetLevel = player.server.overworld();

            player.teleportTo(targetLevel, data.getReturnX(), data.getReturnY(), data.getReturnZ(), player.getYRot(), player.getXRot());
            data.clearReturnPoint();
        }
        player.displayClientMessage(Component.translatable("message.spiritworld.pocket_dimension.returned"), true);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
                event.setCanceled(true);
                PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
                if (data != null) {
                    BlockPos center = PocketDimensionManager.getOrAssignCenter(player.getUUID(), player.server);
                    // 如果掉得比领地基底还要低 10 格，判定为掉入虚空
                    if (center != null && player.getY() < center.getY() - 10) {
                        data.setPocketPosition(0.5, 5.0, 0.5, 0, 0); // 重置，下次会传送到中心
                    } else {
                        data.setPocketPosition(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    }
                }
                teleportOut(player, data);
                player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PocketDimensionManager.tickTasks();
    }

    /**
     * 处理访客传送 (被访者 target 同意了访客 sender 的请求)
     */
    public static void teleportVisitor(ServerPlayer target, ServerPlayer sender) {
        BlockPos center = PocketDimensionManager.getOrAssignCenter(target.getUUID(), target.server);
        if (center == null) {
            target.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.no_domain").withStyle(net.minecraft.ChatFormatting.RED), true);
            return;
        }

        ServerLevel pocketLevel = target.server.getLevel(PocketDimensionManager.POCKET_DIMENSION);
        if (pocketLevel == null) return;

        PlayerSpirit targetData = target.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        int skillLevel = targetData != null ? targetData.getSkillLevel(ModSkills.POCKET_DIMENSION_ID) : 1;
        PocketChunkLoader.updateChunks(target, center, skillLevel);

        PlayerSpirit senderData = sender.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (senderData != null) {
            senderData.setReturnPoint(sender.getX(), sender.getY(), sender.getZ(), sender.level().dimension().location().toString());
            senderData.setPocketPosition(0.5, 5.0, 0.5, 0, 0);
        }

        sender.teleportTo(pocketLevel, center.getX() + 0.5, 5.0, center.getZ() + 0.5, sender.getYRot(), sender.getXRot());

        sender.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.entered", target.getName().getString()).withStyle(net.minecraft.ChatFormatting.GREEN), true);
        target.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.guest_entered", sender.getName().getString()).withStyle(net.minecraft.ChatFormatting.GREEN), false);
    }
}