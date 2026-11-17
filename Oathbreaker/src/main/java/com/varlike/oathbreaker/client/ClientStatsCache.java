package com.varlike.oathbreaker.client;

import com.varlike.oathbreaker.core.network.PlayerStatsPayload;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端只读面板缓存。只由 {@code PlayerStatsPayload} 更新，不自行计算属性。
 */
public final class ClientStatsCache {
    @Nullable
    private static PlayerStatsPayload stats;

    private ClientStatsCache() {}

    public static void update(PlayerStatsPayload payload) {
        stats = payload;
    }

    @Nullable
    public static PlayerStatsPayload get() {
        return stats;
    }

    public static void clear() {
        stats = null;
    }
}
