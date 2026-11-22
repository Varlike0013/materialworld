package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.hud.HudBar;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的自定义 HUD 条，按注册顺序渲染。 */
public final class HudBarRegistry {

    private static final List<HudBar> BARS = new CopyOnWriteArrayList<>();

    private HudBarRegistry() {}

    public static void register(HudBar bar) {
        Objects.requireNonNull(bar, "bar");
        Objects.requireNonNull(bar.id(), "bar id");
        for (HudBar existing : BARS) {
            if (existing.id().equals(bar.id())) {
                throw new IllegalArgumentException("HudBar already registered: " + bar.id());
            }
        }
        BARS.add(bar);
    }

    public static void unregister(HudBar bar) {
        if (bar != null) BARS.remove(bar);
    }

    public static void unregister(ResourceLocation id) {
        if (id == null) return;
        BARS.removeIf(bar -> bar.id().equals(id));
    }

    public static void clear() {
        BARS.clear();
    }

    public static List<HudBar> all() {
        return List.copyOf(BARS);
    }
}
