package com.varlike.aetherui.core.tick;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UITree;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 客户端 tick：驱动带持续时间 UI 的自动注销，避免到期后常驻内存。 */
public final class ClientTickHandler {

    /** 到期时间(ms) → 待销毁的树。静态，随 mod 生命周期。 */
    private static final List<ScheduledDestroy> SCHEDULED = new ArrayList<>();

    private record ScheduledDestroy(UITree tree, long deadlineMs) {}

    private ClientTickHandler() {}

    /** 安排一棵树在 durationMs 后自动注销。 */
    public static void scheduleDestroy(UITree tree, long durationMs) {
        if (tree == null || durationMs < 0L) return;
        SCHEDULED.add(new ScheduledDestroy(tree, System.currentTimeMillis() + durationMs));
    }

    /** 取消尚未到期的自动注销（手动 destroy 时调用，避免队列继续持有引用）。 */
    public static void cancelDestroy(UITree tree) {
        if (tree == null) return;
        SCHEDULED.removeIf(entry -> entry.tree() == tree);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) return;

        long now = System.currentTimeMillis();
        Iterator<ScheduledDestroy> it = SCHEDULED.iterator();
        while (it.hasNext()) {
            ScheduledDestroy entry = it.next();
            if (now >= entry.deadlineMs()) {
                AetherUI.destroy(entry.tree()); // 从注册表移除，断开强引用
                it.remove();
            }
        }
    }
}
