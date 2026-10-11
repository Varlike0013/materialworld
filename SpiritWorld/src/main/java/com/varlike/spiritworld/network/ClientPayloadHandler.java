package com.varlike.spiritworld.network;

import com.varlike.spiritworld.client.ClientSpiritData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleSyncSpirit(ClientboundSyncSpiritPacket pkt, IPayloadContext context) {
        context.enqueueWork(() ->
                ClientSpiritData.update(pkt.spirit(), pkt.maxSpirit(), pkt.visionLevel()));
    }
}