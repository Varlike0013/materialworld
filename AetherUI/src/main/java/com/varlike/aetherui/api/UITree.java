package com.varlike.aetherui.api;

import com.varlike.aetherui.api.element.Panel;
import com.varlike.aetherui.api.target.RenderTarget;

public final class UITree {

    private final Panel root;
    private final RenderTarget target;

    public UITree(Panel root, RenderTarget target) {
        this.root = root;
        this.target = target;
    }

    public Panel root()           { return root; }
    public RenderTarget target()  { return target; }
}