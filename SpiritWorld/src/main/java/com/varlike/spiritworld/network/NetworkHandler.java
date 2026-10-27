package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                ClientboundSyncSpiritPacket.TYPE,
                ClientboundSyncSpiritPacket.STREAM_CODEC, ClientboundSyncSpiritPacket::handle);
        registrar.playToServer(ServerboundRequestSyncPacket.TYPE,
                ServerboundRequestSyncPacket.STREAM_CODEC, ServerboundRequestSyncPacket::handle);
        registrar.playToServer(ServerboundLearnSkillPacket.TYPE,
                ServerboundLearnSkillPacket.STREAM_CODEC, ServerboundLearnSkillPacket::handle);
        registrar.playToServer(ServerboundSetLoadoutPacket.TYPE,
                ServerboundSetLoadoutPacket.STREAM_CODEC, ServerboundSetLoadoutPacket::handle);
        registrar.playToServer(ServerboundSetSelectedSlotPacket.TYPE,
                ServerboundSetSelectedSlotPacket.STREAM_CODEC, ServerboundSetSelectedSlotPacket::handle);
        registrar.playToServer(ServerboundTogglePassivePacket.TYPE,
                ServerboundTogglePassivePacket.STREAM_CODEC, ServerboundTogglePassivePacket::handle);
        registrar.playToServer(ServerboundUseSkillPacket.TYPE,
                ServerboundUseSkillPacket.STREAM_CODEC, ServerboundUseSkillPacket::handle);
        registrar.playToServer(ServerboundSetScanConfigPacket.TYPE,
                ServerboundSetScanConfigPacket.STREAM_CODEC,ServerboundSetScanConfigPacket::handle
        );
        registrar.playToClient(ClientboundScanResultPacket.TYPE,
                ClientboundScanResultPacket.STREAM_CODEC, ClientboundScanResultPacket::handle
        );
    }

    public static void syncTo(ServerPlayer player) {
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;
        PacketDistributor.sendToPlayer(player, new ClientboundSyncSpiritPacket(
                data.getSpirit(),
                data.getEffectiveMaxSpirit(),
                data.getBaseMaxSpirit(),
                data.getAvailableVisionPoints(),
                data.getTotalSpentVisionPoints(),
                data.getVisionFragments(),
                data.getLearnedSkills(),
                data.getLoadout(),
                data.getSelectedSlot(),
                data.getActivePassives(),
                data.getScanRange(),
                data.getScanOres()
        ));
    }

}