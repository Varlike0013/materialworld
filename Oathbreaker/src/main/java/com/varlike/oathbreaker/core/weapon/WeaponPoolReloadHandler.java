package com.varlike.oathbreaker.core.weapon;

import com.varlike.oathbreaker.Oathbreaker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 把武器池 JSON 加载器挂到服务端资源 reload（/reload 也会重新读取）。
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class WeaponPoolReloadHandler {
    private WeaponPoolReloadHandler() {}

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeaponPoolReloadListener());
    }
}
