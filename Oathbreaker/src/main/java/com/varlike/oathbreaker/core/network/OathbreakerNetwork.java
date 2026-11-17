package com.varlike.oathbreaker.core.network;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.client.ClientStatsCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络注册入口。
 *
 * <p>P2 注册 {@link PlayerStatsPayload}（S→C），HUD 只读客户端缓存。
 * 其余 Payload 按设计 5.4 在后续阶段加入。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class OathbreakerNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private OathbreakerNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(PlayerStatsPayload.TYPE, PlayerStatsPayload.STREAM_CODEC,
                OathbreakerNetwork::handlePlayerStats);
    }

    private static void handlePlayerStats(PlayerStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientStatsCache.update(payload);
            }
        });
    }
}
