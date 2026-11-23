package com.varlike.aetherui.core.registry;

import com.varlike.aetherui.api.UITree;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** 已注册的世界空间 UI 树（{@code WorldRenderTarget}）。渲染线程只读遍历。 */
public final class WorldUIRegistry {

    private static final List<UITree> TREES = new CopyOnWriteArrayList<>();

    private WorldUIRegistry() {}

    public static void add(UITree tree) {
        Objects.requireNonNull(tree, "tree");
        if (!TREES.contains(tree)) TREES.add(tree);
    }

    public static void remove(UITree tree) {
        TREES.remove(tree);
    }

    public static void clear() {
        TREES.clear();
    }

    public static List<UITree> all() {
        return List.copyOf(TREES);
    }

    public static boolean isEmpty() {
        return TREES.isEmpty();
    }
}
