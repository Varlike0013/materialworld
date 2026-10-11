package com.varlike.spiritworld.network;

import com.varlike.spiritworld.client.ClientSpiritData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientPayloadHandler {

    public static void handleSyncSpirit(ClientboundSyncSpiritPacket pkt, IPayloadContext context) {
        context.enqueueWork(() ->
                ClientSpiritData.update(pkt.spirit(), pkt.maxSpirit(), pkt.visionLevel()));
    }
}