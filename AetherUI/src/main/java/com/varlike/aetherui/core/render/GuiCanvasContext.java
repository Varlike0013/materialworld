package com.varlike.aetherui.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.varlike.aetherui.api.paint.CanvasContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * CanvasContext 的 2D 后端。构造后调用 {@link #push()} 建立坐标系，
 * painter 执行完毕后调用 {@link #pop()} 恢复。
 *
 * <p>push 后 PoseStack 已平移到 Canvas 锚点并应用 guiScale，因此
 * {@link #guiGraphics()} 返回的对象可直接使用原版绘制方法，
 * 其坐标系与 Canvas 逻辑像素一致。
 */
public final class GuiCanvasContext implements CanvasContext {

    private static final int DEFAULT_TEXTURE_SIZE = 256;

    private final GuiGraphics graphics;
    private final float width, height;
    private final float originX, originY, scale;

    public GuiCanvasContext(GuiGraphics graphics, float width, float height,
                            float originX, float originY, float scale) {
        this.graphics = graphics;
        this.width = width;
        this.height = height;
        this.originX = originX;
        this.originY = originY;
        this.scale = scale;
    }

    /** 平移并缩放 PoseStack；必须与 {@link #pop()} 成对调用。 */
    public void push() {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(originX, originY, 0f);
        pose.scale(scale, scale, 1f);
    }

    public void pop() {
        graphics.pose().popPose();
    }

    @Override public float width()  { return width; }
    @Override public float height() { return height; }
    @Override public GuiGraphics guiGraphics() { return graphics; }

    @Override
    public void fill(float x, float y, float w, float h, int argb) {
        graphics.fill(Math.round(x), Math.round(y),
                Math.round(x + w), Math.round(y + h), argb);
    }

    @Override
    public void blit(ResourceLocation texture,
                     float x, float y, float w, float h,
                     float u, float v, float uw, float uh,
                     int tintArgb) {
        float a = ((tintArgb >>> 24) & 0xFF) / 255f;
        float r = ((tintArgb >>> 16) & 0xFF) / 255f;
        float g = ((tintArgb >>> 8) & 0xFF) / 255f;
        float b = (tintArgb & 0xFF) / 255f;
        graphics.setColor(r, g, b, a);
        graphics.blit(texture,
                Math.round(x), Math.round(y), Math.round(w), Math.round(h),
                u * DEFAULT_TEXTURE_SIZE, v * DEFAULT_TEXTURE_SIZE,
                Math.round(uw * DEFAULT_TEXTURE_SIZE), Math.round(uh * DEFAULT_TEXTURE_SIZE),
                DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void blitSprite(ResourceLocation sprite, float x, float y, float w, float h) {
        graphics.blitSprite(sprite, Math.round(x), Math.round(y), Math.round(w), Math.round(h));
    }

    @Override
    public void blitAtlasSprite(TextureAtlasSprite sprite, float x, float y, float w, float h) {
        graphics.blit(Math.round(x), Math.round(y), 0, Math.round(w), Math.round(h), sprite);
    }

    @Override
    public void drawString(Component text, float x, float y, int color, boolean shadow) {
        graphics.drawString(Minecraft.getInstance().font, text,
                Math.round(x), Math.round(y), color, shadow);
    }

    @Override
    public void renderItem(ItemStack stack, float x, float y) {
        if (stack == null || stack.isEmpty()) return;
        graphics.renderItem(stack, Math.round(x), Math.round(y));
    }
}
