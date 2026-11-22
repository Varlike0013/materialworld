package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.hud.BuffIcon;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的 buff 图标，按注册顺序排列。 */
public final class BuffIconRegistry {

    private static final List<BuffIcon> ICONS = new CopyOnWriteArrayList<>();

    private BuffIconRegistry() {}

    public static void register(BuffIcon icon) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(icon.id(), "icon id");
        for (BuffIcon existing : ICONS) {
            if (existing.id().equals(icon.id())) {
                throw new IllegalArgumentException("BuffIcon already registered: " + icon.id());
            }
        }
        ICONS.add(icon);
    }

    public static void unregister(BuffIcon icon) {
        if (icon != null) ICONS.remove(icon);
    }

    public static void unregister(ResourceLocation id) {
        if (id == null) return;
        ICONS.removeIf(icon -> icon.id().equals(id));
    }

    public static void clear() {
        ICONS.clear();
    }

    public static List<BuffIcon> all() {
        return List.copyOf(ICONS);
    }
}
