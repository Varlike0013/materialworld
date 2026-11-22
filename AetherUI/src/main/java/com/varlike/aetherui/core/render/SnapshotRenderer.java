package com.varlike.aetherui.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.varlike.aetherui.api.layout.ClipRect;
import com.varlike.aetherui.api.layout.FitMode;
import com.varlike.aetherui.api.paint.CanvasPainter;
import com.varlike.aetherui.api.snapshot.ElementSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 将布局引擎产出的快照列表绘制到 GuiGraphics。
 * 输入坐标是相对 UI 根节点左上角的逻辑像素，通过 base + scale 变换到屏幕坐标。
 */
public final class SnapshotRenderer {

    private static final int TEXTURE_SIZE = 256;

    private SnapshotRenderer() {}

    public static void render(List<ElementSnapshot> snapshots, GuiGraphics g,
                              float baseX, float baseY, float scale) {
        for (ElementSnapshot e : snapshots) {
            float x = baseX + e.x() * scale;
            float y = baseY + e.y() * scale;
            float w = e.width() * scale;
            float h = e.height() * scale;

            ClipRect clip = e.clipRect();
            boolean scissored = clip != null;
            if (scissored) {
                g.enableScissor(
                        Math.round(baseX + clip.x() * scale),
                        Math.round(baseY + clip.y() * scale),
                        Math.round(baseX + (clip.x() + clip.width()) * scale),
                        Math.round(baseY + (clip.y() + clip.height()) * scale));
            }

            try {
                switch (e) {
                    case ElementSnapshot.Panel p -> drawBackground(g, x, y, w, h,
                            p.backgroundColor(), p.backgroundTexture());
                    case ElementSnapshot.Canvas c -> drawCanvas(c, g, x, y, w, h, scale);
                    case ElementSnapshot.Icon i -> drawIcon(i, g, x, y, w, h);
                    case ElementSnapshot.Label l -> drawLabel(l, g, x, y);
                }
            } finally {
                if (scissored) g.disableScissor();
            }
        }
    }

    private static void drawBackground(GuiGraphics g, float x, float y, float w, float h,
                                       int argb, ResourceLocation texture) {
        int xi = Math.round(x), yi = Math.round(y);
        int wi = Math.round(w), hi = Math.round(h);
        if (argb != 0) g.fill(xi, yi, xi + wi, yi + hi, argb);
        if (texture != null) drawTexture(g, texture, xi, yi, wi, hi, 0xFFFFFFFF);
    }

    private static void drawCanvas(ElementSnapshot.Canvas c, GuiGraphics g,
                                   float x, float y, float w, float h, float scale) {
        drawBackground(g, x, y, w, h, c.backgroundColor(), c.backgroundTexture());

        CanvasPainter painter = c.painter();
        if (painter == null) return;

        // anchorX/anchorY 是 Canvas 内的逻辑像素偏移（相对 Canvas 左上角）
        float anchorScreenX = x + c.anchorX() * scale;
        float anchorScreenY = y + c.anchorY() * scale;

        GuiCanvasContext ctx = new GuiCanvasContext(g, c.width(), c.height(),
                anchorScreenX, anchorScreenY, scale);
        ctx.push();
        try {
            painter.paint(ctx);
        } finally {
            ctx.pop();
        }
    }

    private static void drawIcon(ElementSnapshot.Icon i, GuiGraphics g,
                                 float x, float y, float w, float h) {
        int xi = Math.round(x), yi = Math.round(y);
        int wi = Math.round(w), hi = Math.round(h);
        if (wi <= 0 || hi <= 0) return;

        switch (i.fitMode()) {
            case STRETCH -> drawTexture(g, i.texture(), xi, yi, wi, hi, i.tint());
            case FIT -> {
                int side = Math.min(wi, hi);
                drawTexture(g, i.texture(),
                        xi + (wi - side) / 2, yi + (hi - side) / 2,
                        side, side, i.tint());
            }
            case TILE -> {
                int tile = Math.min(16, Math.min(wi, hi));
                for (int ty = 0; ty < hi; ty += tile) {
                    for (int tx = 0; tx < wi; tx += tile) {
                        drawTexture(g, i.texture(), xi + tx, yi + ty,
                                Math.min(tile, wi - tx), Math.min(tile, hi - ty), i.tint());
                    }
                }
            }
        }
    }

    private static void drawLabel(ElementSnapshot.Label l, GuiGraphics g, float x, float y) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0f);
        if (l.scale() != 1f) pose.scale(l.scale(), l.scale(), 1f);
        g.drawString(Minecraft.getInstance().font, l.text(), 0, 0, l.color(), l.shadow());
        pose.popPose();
    }

    private static void drawTexture(GuiGraphics g, ResourceLocation texture,
                                    int x, int y, int w, int h, int tint) {
        float a = ((tint >>> 24) & 0xFF) / 255f;
        float r = ((tint >>> 16) & 0xFF) / 255f;
        float gg = ((tint >>> 8) & 0xFF) / 255f;
        float b = (tint & 0xFF) / 255f;
        g.setColor(r, gg, b, a);
        g.blit(texture, x, y, w, h, 0f, 0f, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        g.setColor(1f, 1f, 1f, 1f);
    }
}
