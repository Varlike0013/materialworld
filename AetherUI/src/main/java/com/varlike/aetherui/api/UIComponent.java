package com.varlike.aetherui.api;

import com.varlike.aetherui.api.binding.Bindable;
import java.util.List;

import com.varlike.aetherui.api.layout.*;
import com.varlike.aetherui.api.paint.CanvasPainter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class UIComponent {

    public final ElementKind kind;

    public final float x, y;
    public final Size widthMode, heightMode;

    public final Bindable<Integer> backgroundColor;
    public final ResourceLocation backgroundTexture;
    public final boolean clipChildren;

    public final Bindable<Component> text;
    public final Bindable<Integer> textColor;
    public final float textScale;
    public final boolean textShadow;

    public final ResourceLocation iconTexture;
    public final FitMode fitMode;
    public final Bindable<Integer> iconTint;

    public final float spacing;
    public final EdgeInsets padding;
    public final int tableColumns;
    public final Alignment canvasAnchor;
    public final CanvasPainter painter;

    public final List<UIComponent> children;
    public final MainAxisAlignment mainAxisAlignment;
    public final CrossAxisAlignment crossAxisAlignment;

    UIComponent(UIBuilder b, List<UIComponent> children) {
        this.kind = b.kind;
        this.x = b.x;
        this.y = b.y;
        this.widthMode = b.widthMode;
        this.heightMode = b.heightMode;
        this.backgroundColor = b.backgroundColor;
        this.backgroundTexture = b.backgroundTexture;
        this.clipChildren = b.clipChildren;
        this.text = b.text;
        this.textColor = b.textColor;
        this.textScale = b.textScale;
        this.textShadow = b.textShadow;
        this.iconTexture = b.iconTexture;
        this.fitMode = b.fitMode;
        this.iconTint = b.iconTint;
        this.spacing = b.spacing;
        this.padding = b.padding;
        this.tableColumns = b.tableColumns;
        this.canvasAnchor = b.canvasAnchor;
        this.painter = b.painter;
        this.mainAxisAlignment = b.mainAxisAlignment;
        this.crossAxisAlignment = b.crossAxisAlignment;
        this.children = List.copyOf(children);
    }
}