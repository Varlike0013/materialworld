package com.varlike.aetherui.api.snapshot;

import com.varlike.aetherui.api.layout.ClipRect;
import com.varlike.aetherui.api.layout.ElementKind;
import com.varlike.aetherui.api.layout.FitMode;
import com.varlike.aetherui.api.paint.CanvasPainter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * 一个元素的最终渲染状态。所有坐标相对 UI 根节点的左上角，单位逻辑像素。
 * 列表本身按绘制顺序排列：父在子之前，子按声明顺序。
 */
public sealed interface ElementSnapshot {

    ElementKind kind();
    float x();
    float y();
    float width();
    float height();
    @Nullable ClipRect clipRect();

    record Panel(float x, float y, float width, float height,
                 @Nullable ClipRect clipRect,
                 int backgroundColor, @Nullable ResourceLocation backgroundTexture)
            implements ElementSnapshot {
        @Override public ElementKind kind() { return ElementKind.PANEL; }
    }

    record Canvas(float x, float y, float width, float height,
                  @Nullable ClipRect clipRect,
                  int backgroundColor, @Nullable ResourceLocation backgroundTexture,
                  float anchorX, float anchorY,
                  @Nullable CanvasPainter painter)
            implements ElementSnapshot {
        @Override public ElementKind kind() { return ElementKind.CANVAS; }
    }

    record Icon(float x, float y, float width, float height,
                @Nullable ClipRect clipRect,
                ResourceLocation texture, FitMode fitMode, int tint)
            implements ElementSnapshot {
        @Override public ElementKind kind() { return ElementKind.ICON; }
    }

    record Label(float x, float y, float width, float height,
                 @Nullable ClipRect clipRect,
                 Component text, int color, float scale, boolean shadow)
            implements ElementSnapshot {
        @Override public ElementKind kind() { return ElementKind.LABEL; }
    }

    // Row / Column / Stack / Table 自身没有视觉内容，不需要自己的 snapshot。
    // 它们的尺寸由布局引擎消费，只产出子元素的 snapshot。
}