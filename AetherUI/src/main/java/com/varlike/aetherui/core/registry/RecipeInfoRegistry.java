package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.hud.RecipeInfoProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的配方信息提供者，按注册顺序渲染。 */
public final class RecipeInfoRegistry {

    private static final List<RecipeInfoProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private RecipeInfoRegistry() {}

    public static void register(RecipeInfoProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.id(), "provider id");
        for (RecipeInfoProvider existing : PROVIDERS) {
            if (existing.id().equals(provider.id())) {
                throw new IllegalArgumentException("RecipeInfoProvider already registered: " + provider.id());
            }
        }
        PROVIDERS.add(provider);
    }

    public static void unregister(RecipeInfoProvider provider) {
        if (provider != null) PROVIDERS.remove(provider);
    }

    public static void unregister(ResourceLocation id) {
        if (id == null) return;
        PROVIDERS.removeIf(p -> p.id().equals(id));
    }

    public static void clear() {
        PROVIDERS.clear();
    }

    public static List<RecipeInfoProvider> all() {
        return List.copyOf(PROVIDERS);
    }

    public static boolean isEmpty() {
        return PROVIDERS.isEmpty();
    }
}
