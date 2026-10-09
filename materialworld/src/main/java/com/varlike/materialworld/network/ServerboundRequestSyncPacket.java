package com.varlike.materialworld.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundRequestSyncPacket {
    public ServerboundRequestSyncPacket() {}

    public static void encode(ServerboundRequestSyncPacket pkt, FriendlyByteBuf buf) {}

    public static ServerboundRequestSyncPacket decode(FriendlyByteBuf buf) {
        return new ServerboundRequestSyncPacket();
    }

    public static void handle(ServerboundRequestSyncPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) NetworkHandler.syncTo(player);
        });
        ctx.setPacketHandled(true);
    }
}