package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestNetworkDataPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ServerboundRequestNetworkDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "request_network_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestNetworkDataPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerboundRequestNetworkDataPacket::pos,
            ServerboundRequestNetworkDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ServerboundRequestNetworkDataPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (player.level().getBlockEntity(pkt.pos()) instanceof com.varlike.spiritworld.block.SpiritCoreBlockEntity core) {
                com.varlike.spiritworld.block.SpiritCoreBlock.sendNetworkData(player, core);
            }
        });
    }
}