package com.varlike.aetherui.api;

import com.varlike.aetherui.api.binding.Bindable;
import java.util.ArrayList;
import java.util.List;

import com.varlike.aetherui.api.layout.*;
import com.varlike.aetherui.api.paint.CanvasPainter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public final class UIBuilder {

    // ---------------- 静态工厂 ----------------

    public static UIBuilder panel()                { return new UIBuilder(ElementKind.PANEL); }
    public static UIBuilder panel(Size w, Size h)  { return panel().size(w, h); }
    public static UIBuilder panel(Vec2 v)          { return panel().size(v); }

    public static UIBuilder row()                  { return new UIBuilder(ElementKind.ROW); }
    public static UIBuilder column()               { return new UIBuilder(ElementKind.COLUMN); }

    public static UIBuilder table(int columns) {
        if (columns <= 0) throw new IllegalArgumentException("table columns must be > 0");
        UIBuilder b = new UIBuilder(ElementKind.TABLE);
        b.tableColumns = columns;
        return b;
    }

    public static UIBuilder canvas()                { return new UIBuilder(ElementKind.CANVAS); }
    public static UIBuilder canvas(Size w, Size h)  { return canvas().size(w, h); }

    public UIBuilder canvasAnchor(Alignment a) {
        checkMutable();
        if (a == null) throw new IllegalArgumentException("canvasAnchor must not be null");
        this.canvasAnchor = a;
        return this;
    }

    public UIBuilder painter(CanvasPainter p) {
        checkMutable();
        this.painter = p;
        return this;
    }

    public static UIBuilder label(Component text)              { return label(Bindable.of(text)); }
    public static UIBuilder label(Bindable<Component> text)    { return new UIBuilder(ElementKind.LABEL).text(text); }

    public static UIBuilder icon(ResourceLocation tex, float w, float h) {
        return new UIBuilder(ElementKind.ICON)
                .icon(tex)
                .size(Size.fixed(w), Size.fixed(h));
    }

    /** 带半透明白灰边框的 PNG 图标，外框尺寸为 size×size，边框宽 1px。 */
    public static UIBuilder framedIcon(ResourceLocation tex, float size) {
        return framedIcon(tex, size, 1f);
    }

    /**
     * 带半透明白灰边框的 PNG 图标，可指定边框宽度。
     * 内部使用 {@link com.varlike.aetherui.api.paint.CanvasStyles#framedImage} 绘制，
     * 因此图片会缩放填满边框内侧。位置由所在 UI 树的锚点与对齐自动决定。
     */
    public static UIBuilder framedIcon(ResourceLocation tex, float size, float borderWidth) {
        if (tex == null) throw new IllegalArgumentException("framedIcon texture required");
        if (size <= 0f) throw new IllegalArgumentException("framedIcon size must be > 0");
        return canvas(Size.fixed(size), Size.fixed(size))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(ctx -> com.varlike.aetherui.api.paint.CanvasStyles
                        .framedImage(ctx, tex, 0f, 0f, size, borderWidth));
    }

    // ---------------- 内部状态 ----------------

    final ElementKind kind;

    float x = 0, y = 0;
    Size widthMode  = Size.WRAP;
    Size heightMode = Size.WRAP;

    Bindable<Integer> backgroundColor = Bindable.of(0x00000000);
    ResourceLocation backgroundTexture = null;
    boolean clipChildren = false;

    Bindable<Component> text   = Bindable.of(Component.empty());
    Bindable<Integer> textColor = Bindable.of(0xFFFFFFFF);
    float textScale   = 1f;
    boolean textShadow = false;

    ResourceLocation iconTexture = null;
    FitMode fitMode = FitMode.STRETCH;
    Bindable<Integer> iconTint = Bindable.of(0xFFFFFFFF);

    float spacing = 0f;
    EdgeInsets padding = EdgeInsets.ZERO;
    int tableColumns = 1;

    private final List<UIBuilder> children = new ArrayList<>();
    private boolean frozen = false;
    Alignment canvasAnchor = Alignment.CENTER;
    CanvasPainter painter = null;
    MainAxisAlignment mainAxisAlignment = MainAxisAlignment.START;
    CrossAxisAlignment crossAxisAlignment = CrossAxisAlignment.START;

    private UIBuilder(ElementKind kind) { this.kind = kind; }

    // ---------------- 通用属性 ----------------

    public UIBuilder size(Size w, Size h) { checkMutable(); this.widthMode = w; this.heightMode = h; return this; }
    public UIBuilder size(Vec2 v)         { return size(Size.fixed(v.x), Size.fixed(v.y)); }

    public UIBuilder x(float v)           { checkMutable(); this.x = v; return this; }
    public UIBuilder y(float v)           { checkMutable(); this.y = v; return this; }

    // ---------------- Panel ----------------

    public UIBuilder background(int argb)                    { return background(Bindable.of(argb)); }
    public UIBuilder background(Bindable<Integer> argb)      { checkMutable(); this.backgroundColor = argb; return this; }
    public UIBuilder backgroundTexture(ResourceLocation tex) { checkMutable(); this.backgroundTexture = tex; return this; }
    public UIBuilder clipChildren(boolean v)                 { checkMutable(); this.clipChildren = v; return this; }

    // ---------------- Label ----------------

    public UIBuilder text(Component c)                  { return text(Bindable.of(c)); }
    public UIBuilder text(Bindable<Component> t)        { checkMutable(); this.text = t; return this; }
    public UIBuilder textColor(int argb)                { return textColor(Bindable.of(argb)); }
    public UIBuilder textColor(Bindable<Integer> argb)  { checkMutable(); this.textColor = argb; return this; }
    public UIBuilder textScale(float s)                 { checkMutable(); this.textScale = s; return this; }
    public UIBuilder textShadow(boolean s)              { checkMutable(); this.textShadow = s; return this; }

    // ---------------- Icon ----------------

    public UIBuilder icon(ResourceLocation tex)         { checkMutable(); this.iconTexture = tex; return this; }
    public UIBuilder fitMode(FitMode m)                 { checkMutable(); this.fitMode = m; return this; }
    public UIBuilder iconTint(int argb)                 { return iconTint(Bindable.of(argb)); }
    public UIBuilder iconTint(Bindable<Integer> argb)   { checkMutable(); this.iconTint = argb; return this; }

    // ---------------- 自动布局容器 ----------------

    public UIBuilder spacing(float v)                   { checkMutable(); this.spacing = v; return this; }
    public UIBuilder padding(EdgeInsets e)              { checkMutable(); this.padding = e; return this; }
    public UIBuilder padding(float all)                 { return padding(EdgeInsets.all(all)); }
    public UIBuilder padding(float h, float v)          { return padding(EdgeInsets.symmetric(h, v)); }

    public UIBuilder mainAxisAlignment(MainAxisAlignment a) {
        checkMutable();
        if (a == null) throw new IllegalArgumentException("mainAxisAlignment must not be null");
        this.mainAxisAlignment = a;
        return this;
    }

    public UIBuilder crossAxisAlignment(CrossAxisAlignment a) {
        checkMutable();
        if (a == null) throw new IllegalArgumentException("crossAxisAlignment must not be null");
        this.crossAxisAlignment = a;
        return this;
    }

    // ---------------- 子节点 ----------------

    public UIBuilder add(UIBuilder child) {
        checkMutable();
        if (!kind.isContainer())
            throw new IllegalStateException(kind + " is not a container; cannot add children");
        if (child == null)
            throw new IllegalArgumentException("child must not be null");
        if (child == this)
            throw new IllegalArgumentException("a builder cannot be added to itself");
        if (child.frozen)
            throw new IllegalStateException("child builder already built; builders are single-use");
        children.add(child);
        return this;
    }

    // ---------------- 构建 ----------------

    public UIComponent build() {
        checkMutable();
        frozen = true;

        if (kind == ElementKind.ICON && iconTexture == null)
            throw new IllegalStateException("ICON builder requires an icon texture");

        List<UIComponent> built = new ArrayList<>(children.size());
        for (UIBuilder c : children) built.add(c.build());
        return new UIComponent(this, built);
    }

    private void checkMutable() {
        if (frozen) throw new IllegalStateException("builder already built; builders are single-use");
    }
}