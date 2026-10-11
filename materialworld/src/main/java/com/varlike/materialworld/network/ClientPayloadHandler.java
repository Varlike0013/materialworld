package com.varlike.materialworld.network;

import com.varlike.materialworld.client.ClientEMCData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientPayloadHandler {

    public static void handleSyncEMC(ClientboundSyncEMCPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> ClientEMCData.update(pkt.balance(), pkt.learned()));
    }
}