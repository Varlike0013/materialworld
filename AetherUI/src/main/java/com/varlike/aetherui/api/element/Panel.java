package com.varlike.aetherui.api.element;

import com.varlike.aetherui.api.binding.Bindable;
import com.varlike.aetherui.core.layout.MeasureContext;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Panel extends UIElement {

    private final List<UIElement> children = new ArrayList<>();

    private Bindable<Integer> backgroundColor = Bindable.of(0x00000000);
    private ResourceLocation backgroundTexture;
    private boolean clipChildren = false;

    public Panel background(Bindable<Integer> argb)   { this.backgroundColor = argb; return this; }
    public Panel background(int argb)                  { return background(Bindable.of(argb)); }
    public Panel backgroundTexture(ResourceLocation t) { this.backgroundTexture = t; return this; }
    public Panel clipChildren(boolean v)               { this.clipChildren = v; return this; }

    public <T extends UIElement> T add(T child) {
        children.add(child);
        return child;
    }

    public List<UIElement> children() { return Collections.unmodifiableList(children); }
    public Bindable<Integer> backgroundColor() { return backgroundColor; }
    public ResourceLocation backgroundTexture() { return backgroundTexture; }
    public boolean clipChildren() { return clipChildren; }

    @Override
    protected void onMeasure(MeasureContext ctx, float w, float h) {
        // Phase 2：固定尺寸直接写；WrapContent 时取子元素最大包络（自由定位语义）
        this.measuredWidth  = w >= 0 ? w : 0;
        this.measuredHeight = h >= 0 ? h : 0;
    }

    @Override
    protected void onLayout() {
        // 自由定位容器：子元素按自己的声明坐标摆放（Phase 2 完整实现）
    }

    @Override
    public void collectSnapshots(List<Object> out) {
        // Phase 2：先写自身快照，再递归子元素
        for (UIElement c : children) c.collectSnapshots(out);
    }
}