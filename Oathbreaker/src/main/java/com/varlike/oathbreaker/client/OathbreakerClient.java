package com.varlike.oathbreaker.client;

import com.varlike.oathbreaker.Oathbreaker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * 客户端入口。
 *
 * <p>HUD 注册在这里；Screen、KeyMapping、客户端缓存等后续内容同样在此接入。
 * 使用 {@code dist = Dist.CLIENT} 保证专用服务器不会加载本类。</p>
 */
@Mod(value = Oathbreaker.MOD_ID, dist = Dist.CLIENT)
public final class OathbreakerClient {
    public OathbreakerClient(IEventBus modEventBus, ModContainer modContainer) {
        Oathbreaker.LOGGER.debug("Oathbreaker client initializing...");
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) ->
                OathbreakerHud.register(event));
    }
}
