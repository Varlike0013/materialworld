package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record ClientboundSyncEMCPacket(long balance, Set<ResourceLocation> learned)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundSyncEMCPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "sync_emc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncEMCPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundSyncEMCPacket::encode, ClientboundSyncEMCPacket::new);

    public ClientboundSyncEMCPacket(long balance, Set<ResourceLocation> learned) {
        this.balance = balance;
        this.learned = new HashSet<>(learned);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(balance);
        buf.writeVarInt(learned.size());
        for (ResourceLocation rl : learned) buf.writeResourceLocation(rl);
    }

    public ClientboundSyncEMCPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readLong(), readLearned(buf));
    }

    private static Set<ResourceLocation> readLearned(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<ResourceLocation> set = new HashSet<>();
        for (int i = 0; i < size; i++) set.add(buf.readResourceLocation());
        return set;
    }

    // 原来的 handle 方法改为：
    public static void handle(ClientboundSyncEMCPacket pkt, IPayloadContext context) {
        // 客户端处理已移至 ClientPayloadHandler
        // 此处仅在服务端被调用时执行（实际上不会）
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}