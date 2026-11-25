package com.varlike.spiritworld.world;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** 领地访问请求：持久化到 PocketDimensionData，60 秒过期，命令使用 UUID 标识。 */
public class VisitRequestManager {

    private static final long EXPIRE_TIME = 60 * 1000;

    private VisitRequestManager() {}

    private static Map<UUID, Map<UUID, Long>> requests(MinecraftServer server) {
        return PocketDimensionData.get(server).getVisitRequests();
    }

    public static void sendRequest(ServerPlayer sender, ServerPlayer target) {
        MinecraftServer server = target.server;
        cleanup(server);

        UUID senderId = sender.getUUID();
        UUID targetId = target.getUUID();
        Map<UUID, Map<UUID, Long>> all = requests(server);
        Map<UUID, Long> targetRequests = all.computeIfAbsent(targetId, k -> new HashMap<>());

        Long sentAt = targetRequests.get(senderId);
        if (sentAt != null && System.currentTimeMillis() - sentAt < EXPIRE_TIME) {
            sender.displayClientMessage(
                    Component.translatable("message.spiritworld.pocket.visit.already_sent").withStyle(ChatFormatting.RED), true);
            return;
        }

        targetRequests.put(senderId, System.currentTimeMillis());
        PocketDimensionData.get(server).setDirty();

        sender.displayClientMessage(
                Component.translatable("message.spiritworld.pocket.visit.sent", target.getName().getString())
                        .withStyle(ChatFormatting.GREEN), false);

        String senderIdText = senderId.toString();
        Component msg = Component.translatable("message.spiritworld.pocket.visit.request", sender.getName().getString())
                .withStyle(ChatFormatting.YELLOW)
                .append("\n")
                .append(Component.translatable("message.spiritworld.pocket.visit.accept_btn").withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/spiritworld accept " + senderIdText))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("message.spiritworld.pocket.visit.accept_hover")))))
                .append("   ")
                .append(Component.translatable("message.spiritworld.pocket.visit.deny_btn").withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/spiritworld deny " + senderIdText))));

        target.displayClientMessage(msg, false);
    }

    public static UUID acceptRequest(ServerPlayer target, UUID senderId) {
        MinecraftServer server = target.server;
        cleanup(server);

        Map<UUID, Long> reqs = requests(server).get(target.getUUID());
        if (reqs == null || !reqs.containsKey(senderId)) return null;

        reqs.remove(senderId);
        PocketDimensionData.get(server).setDirty();
        return senderId;
    }

    public static void denyRequest(ServerPlayer target, UUID senderId) {
        UUID removed = acceptRequest(target, senderId);
        if (removed == null) return;

        ServerPlayer sender = target.server.getPlayerList().getPlayer(removed);
        if (sender != null) {
            sender.displayClientMessage(
                    Component.translatable("message.spiritworld.pocket.visit.denied", target.getName().getString())
                            .withStyle(ChatFormatting.RED), false);
        }
    }

    /** 由服务端 tick 调用，统一清理所有目标玩家中的过期请求。 */
    public static void tickRequests(MinecraftServer server) {
        cleanup(server);
    }

    private static void cleanup(MinecraftServer server) {
        Map<UUID, Map<UUID, Long>> all = requests(server);
        long now = System.currentTimeMillis();
        boolean changed = false;

        Iterator<Map.Entry<UUID, Map<UUID, Long>>> iterator = all.entrySet().iterator();
        while (iterator.hasNext()) {
            Map<UUID, Long> senders = iterator.next().getValue();
            boolean removed = senders.entrySet().removeIf(entry -> now - entry.getValue() > EXPIRE_TIME);
            if (removed) changed = true;
            if (senders.isEmpty()) iterator.remove();
        }

        if (changed) PocketDimensionData.get(server).setDirty();
    }
}
