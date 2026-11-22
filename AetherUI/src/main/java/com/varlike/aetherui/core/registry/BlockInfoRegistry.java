package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.hud.BlockInfoProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的方块信息提供者，按注册顺序查询。 */
public final class BlockInfoRegistry {

    private static final List<BlockInfoProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private BlockInfoRegistry() {}

    public static void register(BlockInfoProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.id(), "provider id");
        for (BlockInfoProvider existing : PROVIDERS) {
            if (existing.id().equals(provider.id())) {
                throw new IllegalArgumentException("BlockInfoProvider already registered: " + provider.id());
            }
        }
        PROVIDERS.add(provider);
    }

    public static void unregister(BlockInfoProvider provider) {
        if (provider != null) PROVIDERS.remove(provider);
    }

    public static void unregister(ResourceLocation id) {
        if (id == null) return;
        PROVIDERS.removeIf(p -> p.id().equals(id));
    }

    public static void clear() {
        PROVIDERS.clear();
    }

    public static List<BlockInfoProvider> all() {
        return List.copyOf(PROVIDERS);
    }
}
