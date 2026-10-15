package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ClientboundSyncSpiritPacket(
        long spirit,
        long maxSpirit,
        long availableVisionPoints,
        long totalSpentVisionPoints,
        Map<ResourceLocation, Integer> learnedSkills,
        ResourceLocation[] loadout,
        int selectedSlot,
        Set<ResourceLocation> activePassives
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
        // 技能栏
        for (int i = 0; i < 6; i++) {
            ResourceLocation id = (loadout != null && i < loadout.length) ? loadout[i] : null;
            buf.writeBoolean(id != null);
            if (id != null) buf.writeResourceLocation(id);
        }
        buf.writeVarInt(selectedSlot);
        // 激活被动
        buf.writeVarInt(activePassives.size());
        for (ResourceLocation id : activePassives) {
            buf.writeResourceLocation(id);
        }
    }

    public ClientboundSyncSpiritPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readLong(), buf.readLong(), buf.readLong(), buf.readLong(),
                readMap(buf),
                readLoadout(buf),
                buf.readVarInt(),
                readPassives(buf));
    }

    private static Map<ResourceLocation, Integer> readMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) map.put(buf.readResourceLocation(), buf.readVarInt());
        return map;
    }

    private static ResourceLocation[] readLoadout(RegistryFriendlyByteBuf buf) {
        ResourceLocation[] arr = new ResourceLocation[6];
        for (int i = 0; i < 6; i++) {
            if (buf.readBoolean()) arr[i] = buf.readResourceLocation();
        }
        return arr;
    }

    private static Set<ResourceLocation> readPassives(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<ResourceLocation> set = new HashSet<>();
        for (int i = 0; i < size; i++) set.add(buf.readResourceLocation());
        return set;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}