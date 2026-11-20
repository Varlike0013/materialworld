package com.varlike.aetherui.core.layout;

import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.layout.*;
import com.varlike.aetherui.api.snapshot.ElementSnapshot;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class LayoutEngine {

    private LayoutEngine() {}

    public static List<ElementSnapshot> layout(UIComponent root, MeasureContext ctx) {
        LayoutNode rootNode = new LayoutNode(root);
        measure(rootNode, ctx, Float.MAX_VALUE, Float.MAX_VALUE);
        place(rootNode, 0f, 0f);
        List<ElementSnapshot> out = new ArrayList<>();
        emit(rootNode, 0f, 0f, null, out);
        return out;
    }

    // ==================== Measure ====================

    private static void measure(LayoutNode node, MeasureContext ctx,
                                float maxW, float maxH) {
        UIComponent c = node.element;

        // 先递归测量子元素。子元素的可用空间：
        //  - 父宽 Fixed  → 父宽 - padding
        //  - 父宽 FillParent → 父的 maxW - padding
        //  - 父宽 WrapContent → Float.MAX_VALUE（无限）
        if (c.kind.isContainer() && !c.children.isEmpty()) {
            float childMaxW = switch (c.widthMode) {
                case Size.Fixed f -> Math.max(0, f.value() - c.padding.horizontal());
                case Size.FillParent fp -> Math.max(0, maxW - c.padding.horizontal());
                case Size.WrapContent wc -> Float.MAX_VALUE;
            };
            float childMaxH = switch (c.heightMode) {
                case Size.Fixed f -> Math.max(0, f.value() - c.padding.vertical());
                case Size.FillParent fp -> Math.max(0, maxH - c.padding.vertical());
                case Size.WrapContent wc -> Float.MAX_VALUE;
            };
            node.children = new ArrayList<>(c.children.size());
            for (UIComponent child : c.children) {
                LayoutNode cn = new LayoutNode(child);
                measure(cn, ctx, childMaxW, childMaxH);
                node.children.add(cn);
            }
        }

        // 再根据元素类型计算自身尺寸
        switch (c.kind) {
            case PANEL  -> measureFreeform(node, maxW, maxH);
            case CANVAS -> measureCanvas(node, maxW, maxH);
            case ROW    -> measureRow(node, maxW, maxH, true);
            case COLUMN -> measureRow(node, maxW, maxH, false);
            case STACK  -> measureStack(node, maxW, maxH);
            case ICON   -> measureIcon(node, maxW, maxH);
            case LABEL  -> measureLabel(node, ctx, maxW, maxH);
            case TABLE  -> measureStack(node, maxW, maxH); // 第一版退化为 Stack
        }
    }

    private static void measureFreeform(LayoutNode node, float maxW, float maxH) {
        UIComponent c = node.element;
        node.w = resolveWidth(c.widthMode, maxW);
        node.h = resolveHeight(c.heightMode, maxH);
        if (node.w < 0) { // WrapContent
            float mw = 0, mh = 0;
            for (LayoutNode cn : node.children) {
                mw = Math.max(mw, cn.element.x + cn.w);
                mh = Math.max(mh, cn.element.y + cn.h);
            }
            node.w = mw + c.padding.horizontal();
            node.h = mh + c.padding.vertical();
        }
    }

    private static void measureCanvas(LayoutNode node, float maxW, float maxH) {
        UIComponent c = node.element;
        float fixedW = resolveWidth(c.widthMode, maxW);
        float fixedH = resolveHeight(c.heightMode, maxH);

        // 计算子元素相对锚点的包络
        float minX = 0, minY = 0, maxX = 0, maxY = 0;
        boolean hasChild = false;
        for (LayoutNode cn : node.children) {
            hasChild = true;
            minX = Math.min(minX, cn.element.x);
            minY = Math.min(minY, cn.element.y);
            maxX = Math.max(maxX, cn.element.x + cn.w);
            maxY = Math.max(maxY, cn.element.y + cn.h);
        }

        if (fixedW < 0 || fixedH < 0) {
            // WrapContent：紧贴内容，锚点唯一解（内容左上角对齐 Canvas 原点）
            node.w = hasChild ? (maxX - minX) : 0;
            node.h = hasChild ? (maxY - minY) : 0;
            node.anchorX = -minX;
            node.anchorY = -minY;
        } else {
            node.w = fixedW;
            node.h = fixedH;
            node.anchorX = fixedW * c.canvasAnchor.u();
            node.anchorY = fixedH * c.canvasAnchor.v();
        }
    }

    private static void measureRow(LayoutNode node, float maxW, float maxH, boolean horizontal) {
        UIComponent c = node.element;
        float fixedW = resolveWidth(c.widthMode, maxW);
        float fixedH = resolveHeight(c.heightMode, maxH);

        float mainSum = 0, crossMax = 0;
        for (int i = 0; i < node.children.size(); i++) {
            LayoutNode cn = node.children.get(i);
            if (horizontal) {
                mainSum += cn.w;
                crossMax = Math.max(crossMax, cn.h);
            } else {
                mainSum += cn.h;
                crossMax = Math.max(crossMax, cn.w);
            }
        }
        int n = node.children.size();
        if (n > 1) mainSum += c.spacing * (n - 1);

        float padH = c.padding.horizontal(), padV = c.padding.vertical();
        node.w = fixedW >= 0 ? fixedW : (horizontal ? mainSum + padH : crossMax + padH);
        node.h = fixedH >= 0 ? fixedH : (horizontal ? crossMax + padV : mainSum + padV);
    }

    private static void measureStack(LayoutNode node, float maxW, float maxH) {
        UIComponent c = node.element;
        float fixedW = resolveWidth(c.widthMode, maxW);
        float fixedH = resolveHeight(c.heightMode, maxH);
        float mw = 0, mh = 0;
        for (LayoutNode cn : node.children) {
            mw = Math.max(mw, cn.w);
            mh = Math.max(mh, cn.h);
        }
        node.w = fixedW >= 0 ? fixedW : mw + c.padding.horizontal();
        node.h = fixedH >= 0 ? fixedH : mh + c.padding.vertical();
    }

    private static void measureIcon(LayoutNode node, float maxW, float maxH) {
        UIComponent c = node.element;
        // Icon 默认 WrapContent 时 = 纹理的“逻辑尺寸” = 100×100 这类声明值（构建时已固定）
        node.w = resolveWidth(c.widthMode, maxW);
        node.h = resolveHeight(c.heightMode, maxH);
        if (node.w < 0) node.w = 16; // 兜底：无纹理尺寸信息时按 16×16 逻辑像素
        if (node.h < 0) node.h = 16;
    }

    private static void measureLabel(LayoutNode node, MeasureContext ctx, float maxW, float maxH) {
        UIComponent c = node.element;
        Component text = c.text.get();
        float scale = c.textScale;
        float tw = ctx.measureTextWidth(text, scale);
        float th = ctx.textLineHeight(scale);
        float fixedW = resolveWidth(c.widthMode, maxW);
        float fixedH = resolveHeight(c.heightMode, maxH);
        node.w = fixedW >= 0 ? fixedW : tw;
        node.h = fixedH >= 0 ? fixedH : th;
    }

    private static float resolveWidth(Size s, float maxW) {
        return switch (s) {
            case Size.Fixed f -> f.value();
            case Size.FillParent fp -> (maxW == Float.MAX_VALUE ? 0 : maxW);
            case Size.WrapContent wc -> -1;
        };
    }

    private static float resolveHeight(Size s, float maxH) {
        return switch (s) {
            case Size.Fixed f -> f.value();
            case Size.FillParent fp -> (maxH == Float.MAX_VALUE ? 0 : maxH);
            case Size.WrapContent wc -> -1;
        };
    }

    // ==================== Layout ====================

    private static void place(LayoutNode node, float x, float y) {
        node.x = x;
        node.y = y;
        UIComponent c = node.element;
        float padL = c.padding.left(), padT = c.padding.top();

        switch (c.kind) {
            case PANEL -> {
                for (LayoutNode cn : node.children)
                    place(cn, x + padL + cn.element.x, y + padT + cn.element.y);
            }
            case CANVAS -> {
                for (LayoutNode cn : node.children)
                    place(cn, x + node.anchorX + cn.element.x, y + node.anchorY + cn.element.y);
            }
            case ROW    -> placeRow(node, x + padL, y + padT, true);
            case COLUMN -> placeRow(node, x + padL, y + padT, false);
            case STACK, TABLE -> {
                for (LayoutNode cn : node.children)
                    place(cn, x + padL, y + padT);
            }
            case ICON, LABEL -> { /* 叶子，无子元素 */ }
        }
    }

    private static void placeRow(LayoutNode node, float startX, float startY, boolean horizontal) {
        UIComponent c = node.element;
        int n = node.children.size();
        if (n == 0) return;

        float mainSize = horizontal ? node.w : node.h;
        float padMain = horizontal ? c.padding.horizontal() : c.padding.vertical();
        float crossSize = horizontal ? node.h : node.w;
        float padCross = horizontal ? c.padding.vertical() : c.padding.horizontal();
        float crossStart = horizontal ? startY : startX;

        float content = 0;
        for (LayoutNode cn : node.children) content += horizontal ? cn.w : cn.h;
        content += c.spacing * (n - 1);
        float free = Math.max(0, mainSize - padMain - content);

        float cursor = horizontal ? startX : startY;
        float gap = c.spacing;
        float lead = 0;
        switch (c.mainAxisAlignment) {
            case START -> { }
            case CENTER -> lead = free / 2f;
            case END -> lead = free;
            case SPACE_BETWEEN -> { if (n > 1) gap = c.spacing + free / (n - 1); }
            case SPACE_AROUND -> {
                float each = free / n;
                lead = each / 2f;
                gap = c.spacing + each;
            }
        }
        cursor += lead;

        for (LayoutNode cn : node.children) {
            float childMain = horizontal ? cn.w : cn.h;
            float childCross = horizontal ? cn.h : cn.w;
            float crossPos = crossStart;
            switch (c.crossAxisAlignment) {
                case START -> { }
                case CENTER -> crossPos += (crossSize - padCross - childCross) / 2f;
                case END -> crossPos += crossSize - padCross - childCross;
                case STRETCH -> { /* 第一版不实现 STRETCH */ }
            }
            if (horizontal) place(cn, cursor, crossPos);
            else             place(cn, crossPos, cursor);
            cursor += childMain + gap;
        }
    }

    // ==================== Emit ====================

    private static void emit(LayoutNode node, float parentX, float parentY,
                             ClipRect parentClip, List<ElementSnapshot> out) {
        float absX = parentX + node.x;
        float absY = parentY + node.y;
        UIComponent c = node.element;

        ClipRect myClip = parentClip;
        if (c.kind == ElementKind.PANEL && c.clipChildren) {
            ClipRect own = new ClipRect(absX, absY, node.w, node.h);
            myClip = own.intersect(parentClip);
        }

        switch (c.kind) {
            case PANEL -> out.add(new ElementSnapshot.Panel(
                    absX, absY, node.w, node.h, myClip,
                    c.backgroundColor.get(),
                    c.backgroundTexture));
            case CANVAS -> out.add(new ElementSnapshot.Canvas(
                    absX, absY, node.w, node.h, myClip,
                    c.backgroundColor.get(),
                    c.backgroundTexture,
                    node.anchorX, node.anchorY,
                    c.painter));
            case ICON -> out.add(new ElementSnapshot.Icon(
                    absX, absY, node.w, node.h, myClip,
                    c.iconTexture, c.fitMode, c.iconTint.get()));
            case LABEL -> out.add(new ElementSnapshot.Label(
                    absX, absY, node.w, node.h, myClip,
                    c.text.get(), c.textColor.get(), c.textScale, c.textShadow));
            case ROW, COLUMN, STACK, TABLE -> { /* 容器自身无可见内容 */ }
        }

        for (LayoutNode cn : node.children) {
            emit(cn, absX, absY, myClip, out);
        }
    }

    // ==================== 内部状态 ====================

    private static final class LayoutNode {
        final UIComponent element;
        List<LayoutNode> children = new ArrayList<>();
        float x, y;          // 相对父节点左上角
        float w, h;          // 解析后的尺寸
        float anchorX, anchorY;  // Canvas 专用

        LayoutNode(UIComponent element) { this.element = element; }
    }
}