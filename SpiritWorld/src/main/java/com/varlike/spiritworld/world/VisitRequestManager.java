package com.varlike.spiritworld.world;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class VisitRequestManager {

    private static final Map<UUID, Map<UUID, Long>> REQUESTS = new HashMap<>();
    private static final long EXPIRE_TIME = 60 * 1000;

    public static void sendRequest(ServerPlayer sender, ServerPlayer target) {
        UUID senderId = sender.getUUID();
        UUID targetId = target.getUUID();

        cleanup(targetId);

        Map<UUID, Long> targetReqs = REQUESTS.computeIfAbsent(targetId, k -> new HashMap<>());
        if (targetReqs.containsKey(senderId) && System.currentTimeMillis() - targetReqs.get(senderId) < 10000) {
            sender.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.already_sent").withStyle(ChatFormatting.RED), true);
            return;
        }

        targetReqs.put(senderId, System.currentTimeMillis());

        sender.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.sent", target.getName().getString()).withStyle(ChatFormatting.GREEN), false);

        Component msg = Component.translatable("message.spiritworld.pocket.visit.request", sender.getName().getString())
                .withStyle(ChatFormatting.YELLOW)
                .append("\n")
                .append(Component.translatable("message.spiritworld.pocket.visit.accept_btn").withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spiritworld accept " + sender.getName().getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.spiritworld.pocket.visit.accept_hover")))))
                .append("   ")
                .append(Component.translatable("message.spiritworld.pocket.visit.deny_btn").withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spiritworld deny " + sender.getName().getString()))));

        target.displayClientMessage(msg, false);
    }

    public static UUID acceptRequest(ServerPlayer target, String senderName) {
        UUID targetId = target.getUUID();
        cleanup(targetId);

        Map<UUID, Long> reqs = REQUESTS.get(targetId);
        if (reqs == null) return null;

        for (UUID senderId : reqs.keySet()) {
            ServerPlayer sender = target.server.getPlayerList().getPlayer(senderId);
            if (sender != null && sender.getName().getString().equals(senderName)) {
                reqs.remove(senderId);
                return senderId;
            }
        }
        return null;
    }

    public static void denyRequest(ServerPlayer target, String senderName) {
        UUID removed = acceptRequest(target, senderName);
        if (removed != null) {
            ServerPlayer sender = target.server.getPlayerList().getPlayer(removed);
            if (sender != null) {
                sender.displayClientMessage(Component.translatable("message.spiritworld.pocket.visit.denied", target.getName().getString()).withStyle(ChatFormatting.RED), false);
            }
        }
    }

    private static void cleanup(UUID targetId) {
        Map<UUID, Long> reqs = REQUESTS.get(targetId);
        if (reqs != null) {
            long now = System.currentTimeMillis();
            reqs.entrySet().removeIf(e -> now - e.getValue() > EXPIRE_TIME);
        }
    }
}