package com.varlike.aetherui.core.render;

import com.varlike.aetherui.api.paint.CanvasContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * CanvasContext 的偏移包装：把绘制坐标整体平移，使子区域拥有自己的局部坐标系。
 * 用于在同一张 Canvas 上绘制多个自定义条。
 *
 * <p>注意：{@link #guiGraphics()} 返回的是底层 GuiGraphics，其坐标系仍是整张 Canvas。
 * 自定义条应尽量使用本接口提供的统一原语；若直接使用 GuiGraphics，需自行加上偏移量。
 */
public final class OffsetCanvasContext implements CanvasContext {

    private final CanvasContext delegate;
    private final float dx, dy;
    private final float width, height;

    public OffsetCanvasContext(CanvasContext delegate, float dx, float dy, float width, float height) {
        this.delegate = delegate;
        this.dx = dx;
        this.dy = dy;
        this.width = width;
        this.height = height;
    }

    @Override public float width()  { return width; }
    @Override public float height() { return height; }

    @Override public GuiGraphics guiGraphics() { return delegate.guiGraphics(); }

    @Override
    public void fill(float x, float y, float w, float h, int argb) {
        delegate.fill(x + dx, y + dy, w, h, argb);
    }

    @Override
    public void blit(ResourceLocation texture,
                     float x, float y, float w, float h,
                     float u, float v, float uw, float uh,
                     int tintArgb) {
        delegate.blit(texture, x + dx, y + dy, w, h, u, v, uw, uh, tintArgb);
    }

    @Override
    public void blitSprite(ResourceLocation sprite, float x, float y, float w, float h) {
        delegate.blitSprite(sprite, x + dx, y + dy, w, h);
    }

    @Override
    public void blitAtlasSprite(TextureAtlasSprite sprite, float x, float y, float w, float h) {
        delegate.blitAtlasSprite(sprite, x + dx, y + dy, w, h);
    }

    @Override
    public void drawString(Component text, float x, float y, int color, boolean shadow) {
        delegate.drawString(text, x + dx, y + dy, color, shadow);
    }

    @Override
    public void renderItem(ItemStack stack, float x, float y) {
        delegate.renderItem(stack, x + dx, y + dy);
    }
}
