package com.varlike.spiritworld.event;

import com.mojang.brigadier.CommandDispatcher;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.INetworkNode;
import com.varlike.spiritworld.network.NetworkManager;
import com.varlike.spiritworld.network.NodeType;
import com.varlike.spiritworld.world.VisitRequestManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class PocketCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("spiritworld")
                .then(Commands.literal("visit")
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player");
                                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                    if (sender == target) {
                                        ctx.getSource().sendFailure(Component.translatable("message.spiritworld.pocket.visit.self"));
                                        return 0;
                                    }
                                    VisitRequestManager.sendRequest(sender, target);
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                                    UUID senderId = parseUuid(ctx);
                                    if (senderId == null) {
                                        ctx.getSource().sendFailure(Component.translatable("message.spiritworld.pocket.visit.invalid_uuid"));
                                        return 0;
                                    }
                                    UUID acceptedId = VisitRequestManager.acceptRequest(target, senderId);
                                    if (acceptedId == null) {
                                        ctx.getSource().sendFailure(Component.translatable("message.spiritworld.pocket.visit.expired"));
                                        return 0;
                                    }

                                    ServerPlayer sender = target.server.getPlayerList().getPlayer(acceptedId);
                                    if (sender != null) {
                                        com.varlike.spiritworld.event.PocketDimensionHandler.teleportVisitor(target, sender);
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("deny")
                        .then(Commands.argument("player", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                                    UUID senderId = parseUuid(ctx);
                                    if (senderId == null) {
                                        ctx.getSource().sendFailure(Component.translatable("message.spiritworld.pocket.visit.invalid_uuid"));
                                        return 0;
                                    }
                                    VisitRequestManager.denyRequest(target, senderId);
                                    return 1;
                                })))
                .then(Commands.literal("network")
                        .then(Commands.literal("diagnose")
                                .executes(ctx -> diagnoseNetwork(ctx.getSource()))))
        );
    }

    private static int diagnoseNetwork(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int total = 0;
        int disconnected = 0;
        int low = 0;
        int mid = 0;
        int high = 0;

        for (ServerLevel level : server.getAllLevels()) {
            for (Map.Entry<BlockPos, NodeType> entry : NetworkManager.getNodesInLevel(level).entrySet()) {
                total++;
                BlockEntity be = level.getBlockEntity(entry.getKey());
                if (!(be instanceof INetworkNode node)) {
                    disconnected++;
                    continue;
                }
                double efficiency = node.getTotalEfficiency();
                if (node.getParentPos() == null || efficiency <= 0.0) {
                    disconnected++;
                    continue;
                }
                if (efficiency < 0.5) low++;
                else if (efficiency < 0.8) mid++;
                else high++;
            }
        }

        if (total == 0) {
            source.sendFailure(Component.translatable("message.spiritworld.network.diagnose.not_found"));
            return 0;
        }

        Component summary = Component.translatable("message.spiritworld.network.diagnose.summary",
                total, disconnected, low, mid, high);
        source.sendSuccess(() -> Component.translatable("message.spiritworld.network.diagnose.header")
                .append("\n").append(summary), false);
        SpiritWorld.LOGGER.info("[SpiritWorld] Network diagnose: total={}, disconnected={}, <50%={}, 50-80%={}, >80%={}",
                total, disconnected, low, mid, high);
        return 1;
    }

    private static UUID parseUuid(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        String raw = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "player");
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}