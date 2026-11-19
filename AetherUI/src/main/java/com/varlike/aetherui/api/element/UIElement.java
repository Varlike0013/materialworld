package com.varlike.aetherui.api.element;

import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.core.layout.MeasureContext;

import java.util.List;

public abstract class UIElement {

    private Size widthSpec  = Size.WRAP;
    private Size heightSpec = Size.WRAP;

    protected float x, y;
    protected float measuredWidth, measuredHeight;

    @SuppressWarnings("unchecked")
    public final <T extends UIElement> T width(Size s)  { this.widthSpec = s;  return (T) this; }
    @SuppressWarnings("unchecked")
    public final <T extends UIElement> T height(Size s) { this.heightSpec = s; return (T) this; }

    public final float width()  { return measuredWidth; }
    public final float height() { return measuredHeight; }
    public final float x()      { return x; }
    public final float y()      { return y; }

    public final void measure(MeasureContext ctx, float parentW, float parentH) {
        float w = resolve(widthSpec,  parentW);
        float h = resolve(heightSpec, parentH);
        onMeasure(ctx, w, h);
    }

    private static float resolve(Size spec, float parent) {
        return switch (spec) {
            case Size.Fixed f    -> f.value();
            case Size.WrapContent w -> -1f;
            case Size.FillParent f  -> parent;
        };
    }

    public final void layout(float x, float y) {
        this.x = x;
        this.y = y;
        onLayout();
    }

    /** w/h 为 -1 表示该轴是 WrapContent，子类应测量内容后写入 measuredWidth/measuredHeight。 */
    protected abstract void onMeasure(MeasureContext ctx, float w, float h);
    protected abstract void onLayout();
    public abstract void collectSnapshots(List<Object> out);
}