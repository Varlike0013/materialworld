package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.capability.PlayerEMC;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                ServerboundTransmutePacket.TYPE,
                ServerboundTransmutePacket.STREAM_CODEC,
                ServerboundTransmutePacket::handle);

        registrar.playToServer(
                ServerboundRequestSyncPacket.TYPE,
                ServerboundRequestSyncPacket.STREAM_CODEC,
                ServerboundRequestSyncPacket::handle);

        registrar.playBidirectional(
                ClientboundSyncEMCPacket.TYPE,
                ClientboundSyncEMCPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleSyncEMC,
                        ClientboundSyncEMCPacket::handle
                ));

        registrar.playToServer(
                ServerboundRecyclePacket.TYPE,
                ServerboundRecyclePacket.STREAM_CODEC,
                ServerboundRecyclePacket::handle);

        registrar.playToServer(
                ServerboundEMCCraftPacket.TYPE,
                ServerboundEMCCraftPacket.STREAM_CODEC,
                ServerboundEMCCraftPacket::handle);
    }

    public static void syncTo(ServerPlayer player) {
        PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
        if (data == null) return;
        PacketDistributor.sendToPlayer(
                player,
                new ClientboundSyncEMCPacket(data.getEmcBalance(), data.getLearnedItems())
        );
    }
}