package com.varlike.spiritworld.event;

import com.mojang.brigadier.CommandDispatcher;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.world.VisitRequestManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
                                    String senderName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "player");
                                    UUID senderId = VisitRequestManager.acceptRequest(target, senderName);
                                    if (senderId == null) {
                                        ctx.getSource().sendFailure(Component.translatable("message.spiritworld.pocket.visit.expired"));
                                        return 0;
                                    }

                                    ServerPlayer sender = target.server.getPlayerList().getPlayer(senderId);
                                    if (sender != null) {
                                        com.varlike.spiritworld.event.PocketDimensionHandler.teleportVisitor(target, sender);
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("deny")
                        .then(Commands.argument("player", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                                    String senderName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "player");
                                    VisitRequestManager.denyRequest(target, senderName);
                                    return 1;
                                })))
        );
    }
}