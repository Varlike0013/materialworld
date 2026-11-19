package com.varlike.aetherui.api;

import com.varlike.aetherui.api.element.Panel;
import com.varlike.aetherui.api.target.RenderTarget;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import com.varlike.aetherui.api.target.ScreenRenderTarget;

public final class AetherUI {

    private AetherUI() {}

    public static Panel panel() { return new Panel(); }

    public static UITree build(Panel root, RenderTarget target) {
        if (root == null || target == null) throw new IllegalArgumentException("root and target required");
        return new UITree(root, target);
    }

    // 后续阶段在此补 register / destroy；目前返回树，由调用者自行管理
    public static UITree register(UITree tree) {
        if (tree.target() instanceof WorldRenderTarget) {
            // Phase 4：WorldUIRegistry.add(tree)
        } else if (tree.target() instanceof ScreenRenderTarget) {
            // Phase 3：ScreenUIRegistry.add(tree)
        }
        return tree;
    }
}