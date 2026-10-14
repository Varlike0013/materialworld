package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record ClientboundSyncSpiritPacket(
        long spirit,
        long maxSpirit,
        long availableVisionPoints,
        long totalSpentVisionPoints,
        Map<ResourceLocation, Integer> learnedSkills
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundSyncSpiritPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "sync_spirit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncSpiritPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundSyncSpiritPacket::encode, ClientboundSyncSpiritPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(spirit);
        buf.writeLong(maxSpirit);
        buf.writeLong(availableVisionPoints);
        buf.writeLong(totalSpentVisionPoints);
        buf.writeVarInt(learnedSkills.size());
        for (var e : learnedSkills.entrySet()) {
            buf.writeResourceLocation(e.getKey());
            buf.writeVarInt(e.getValue());
        }
    }

    public ClientboundSyncSpiritPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readLong(), buf.readLong(), buf.readLong(), buf.readLong(), readMap(buf));
    }

    private static Map<ResourceLocation, Integer> readMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readResourceLocation(), buf.readVarInt());
        }
        return map;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}