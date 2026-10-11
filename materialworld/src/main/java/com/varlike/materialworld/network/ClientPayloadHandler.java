package com.varlike.materialworld.network;

import com.varlike.materialworld.client.ClientEMCData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 客户端网络包处理器。
 * 此类仅作为静态方法容器，不应注册到事件总线。
 */
public class ClientPayloadHandler {

    public static void handleSyncEMC(ClientboundSyncEMCPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> ClientEMCData.update(pkt.balance(), pkt.learned()));
    }
}