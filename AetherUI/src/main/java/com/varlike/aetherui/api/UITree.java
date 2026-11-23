package com.varlike.aetherui.api;

import com.varlike.aetherui.api.target.RenderTarget;

import java.util.function.BooleanSupplier;

/**
 * 一棵待渲染的 UI 树：组件根节点 + 渲染目标 + 可选可见性谓词。
 *
 * <p>根节点由 {@link UIBuilder} 构建为不可变的 {@link UIComponent}，
 * 经 {@link com.varlike.aetherui.core.layout.LayoutEngine} 布局后交给
 * {@link com.varlike.aetherui.core.render.SnapshotRenderer} 渲染。
 */
public final class UITree {

    private final UIComponent component;
    private final RenderTarget target;
    private final BooleanSupplier visible;

    public UITree(UIComponent component, RenderTarget target) {
        this(component, target, () -> true);
    }

    public UITree(UIComponent component, RenderTarget target, BooleanSupplier visible) {
        if (component == null || target == null) throw new IllegalArgumentException("component and target required");
        if (visible == null) throw new IllegalArgumentException("visible required");
        this.component = component;
        this.target = target;
        this.visible = visible;
    }

    public UIComponent component() { return component; }
    public RenderTarget target()  { return target; }
    public boolean isVisible()    { return visible.getAsBoolean(); }
}
