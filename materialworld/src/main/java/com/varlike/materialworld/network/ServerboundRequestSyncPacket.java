package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestSyncPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundRequestSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "request_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundRequestSyncPacket::encode, ServerboundRequestSyncPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        // 无数据
    }

    public ServerboundRequestSyncPacket(RegistryFriendlyByteBuf buf) {
        this();
    }

    public static void handle(ServerboundRequestSyncPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                NetworkHandler.syncTo(player);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}