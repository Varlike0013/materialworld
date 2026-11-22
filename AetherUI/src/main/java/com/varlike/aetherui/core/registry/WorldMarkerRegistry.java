package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.hud.WorldMarkerProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的 3D 世界标记提供者，按注册顺序渲染。 */
public final class WorldMarkerRegistry {

    private static final List<WorldMarkerProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private WorldMarkerRegistry() {}

    public static void register(WorldMarkerProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.id(), "provider id");
        for (WorldMarkerProvider existing : PROVIDERS) {
            if (existing.id().equals(provider.id())) {
                throw new IllegalArgumentException("WorldMarkerProvider already registered: " + provider.id());
            }
        }
        PROVIDERS.add(provider);
    }

    public static void unregister(WorldMarkerProvider provider) {
        if (provider != null) PROVIDERS.remove(provider);
    }

    public static void unregister(ResourceLocation id) {
        if (id == null) return;
        PROVIDERS.removeIf(p -> p.id().equals(id));
    }

    public static void clear() {
        PROVIDERS.clear();
    }

    public static List<WorldMarkerProvider> all() {
        return List.copyOf(PROVIDERS);
    }

    public static boolean isEmpty() {
        return PROVIDERS.isEmpty();
    }
}
