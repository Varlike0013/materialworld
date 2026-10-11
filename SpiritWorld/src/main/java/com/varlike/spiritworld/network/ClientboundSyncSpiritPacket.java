package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundSyncSpiritPacket(long spirit, long maxSpirit, int visionLevel)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundSyncSpiritPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "sync_spirit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncSpiritPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundSyncSpiritPacket::encode, ClientboundSyncSpiritPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(spirit);
        buf.writeLong(maxSpirit);
        buf.writeVarInt(visionLevel);
    }

    public ClientboundSyncSpiritPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readLong(), buf.readLong(), buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}