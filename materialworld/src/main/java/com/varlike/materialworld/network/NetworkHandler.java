package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.capability.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ServerboundTransmutePacket.class, id++)
                .encoder(ServerboundTransmutePacket::encode)
                .decoder(ServerboundTransmutePacket::decode)
                .consumerMainThread(ServerboundTransmutePacket::handle).add();
        CHANNEL.messageBuilder(ServerboundRequestSyncPacket.class, id++)
                .encoder(ServerboundRequestSyncPacket::encode)
                .decoder(ServerboundRequestSyncPacket::decode)
                .consumerMainThread(ServerboundRequestSyncPacket::handle).add();
        CHANNEL.messageBuilder(ClientboundSyncEMCPacket.class, id++)
                .encoder(ClientboundSyncEMCPacket::encode)
                .decoder(ClientboundSyncEMCPacket::decode)
                .consumerMainThread(ClientboundSyncEMCPacket::handle).add();
        CHANNEL.messageBuilder(ServerboundRecyclePacket.class, id++)
                .encoder(ServerboundRecyclePacket::encode)
                .decoder(ServerboundRecyclePacket::decode)
                .consumerMainThread(ServerboundRecyclePacket::handle).add();
        CHANNEL.messageBuilder(ServerboundEMCCraftPacket.class, id++)
                .encoder(ServerboundEMCCraftPacket::encode)
                .decoder(ServerboundEMCCraftPacket::decode)
                .consumerMainThread(ServerboundEMCCraftPacket::handle).add();
    }

    public static void syncTo(ServerPlayer player) {
        player.getCapability(ModCapabilities.PLAYER_EMC).ifPresent(data ->
                CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncEMCPacket(data.getEmcBalance(), data.getLearnedItems())
                ));
    }
}