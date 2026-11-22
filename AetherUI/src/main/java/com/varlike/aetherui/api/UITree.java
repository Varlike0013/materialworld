package com.varlike.aetherui.api;

import com.varlike.aetherui.api.element.Panel;
import com.varlike.aetherui.api.target.RenderTarget;

import java.util.function.BooleanSupplier;

/**
 * 一棵待渲染的 UI 树：组件根节点 + 渲染目标 + 可选可见性谓词。
 *
 * <p>目前存在两套组件体系：旧的 {@link Panel}/{@code UIElement} 和新的
 * {@link UIComponent}/{@code UIBuilder}。渲染管线基于后者；旧的 Panel 构造器
 * 保留以兼容已有调用方，但不会参与渲染。
 */
public final class UITree {

    private final Panel root;
    private final UIComponent component;
    private final RenderTarget target;
    private final BooleanSupplier visible;

    /** 旧 API 构造器，仅保留兼容；由此创建的树没有可渲染的组件。 */
    public UITree(Panel root, RenderTarget target) {
        this(root, target, () -> true);
    }

    private UITree(Panel root, RenderTarget target, BooleanSupplier visible) {
        if (root == null || target == null) throw new IllegalArgumentException("root and target required");
        this.root = root;
        this.component = null;
        this.target = target;
        this.visible = visible;
    }

    public UITree(UIComponent component, RenderTarget target) {
        this(component, target, () -> true);
    }

    public UITree(UIComponent component, RenderTarget target, BooleanSupplier visible) {
        if (component == null || target == null) throw new IllegalArgumentException("component and target required");
        if (visible == null) throw new IllegalArgumentException("visible required");
        this.root = null;
        this.component = component;
        this.target = target;
        this.visible = visible;
    }

    public Panel root()           { return root; }
    public UIComponent component() { return component; }
    public RenderTarget target()  { return target; }
    public boolean isVisible()    { return visible.getAsBoolean(); }
}
