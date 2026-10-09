package com.varlike.materialworld.network;

import com.varlike.materialworld.client.ClientEMCData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ClientboundSyncEMCPacket {
    private final long balance;
    private final Set<ResourceLocation> learned;

    public ClientboundSyncEMCPacket(long balance, Set<ResourceLocation> learned) {
        this.balance = balance;
        this.learned = new HashSet<>(learned);
    }

    public static void encode(ClientboundSyncEMCPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.balance);
        buf.writeVarInt(pkt.learned.size());
        for (ResourceLocation rl : pkt.learned) buf.writeResourceLocation(rl);
    }

    public static ClientboundSyncEMCPacket decode(FriendlyByteBuf buf) {
        long balance = buf.readLong();
        int size = buf.readVarInt();
        Set<ResourceLocation> learned = new HashSet<>();
        for (int i = 0; i < size; i++) learned.add(buf.readResourceLocation());
        return new ClientboundSyncEMCPacket(balance, learned);
    }

    public static void handle(ClientboundSyncEMCPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientEMCData.update(pkt.balance, pkt.learned)));
        ctx.setPacketHandled(true);
    }
}