package com.varlike.spiritworld.network;

import com.varlike.spiritworld.client.ClientSpiritData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleSyncSpirit(ClientboundSyncSpiritPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> ClientSpiritData.update(
                pkt.spirit(),
                pkt.maxSpirit(),
                pkt.baseMaxSpirit(),
                pkt.availableVisionPoints(),
                pkt.totalSpentVisionPoints(),
                pkt.visionFragments(),
                pkt.learnedSkills(),
                pkt.loadout(),
                pkt.selectedSlot(),
                pkt.activePassives(),
                pkt.scanRange(),
                pkt.scanOres() ));
    }
}