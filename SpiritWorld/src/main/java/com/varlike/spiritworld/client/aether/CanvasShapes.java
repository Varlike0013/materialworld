package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.spiritworld.skill.SkillType;

/** 把 SkillRenderer 的 GUI 图元移植到 AetherUI 的 CanvasContext 上。 */
final class CanvasShapes {

    private CanvasShapes() {}

    static void fillSquare(CanvasContext ctx, float cx, float cy, float half, int color) {
        ctx.fill(cx - half, cy - half, half * 2f, half * 2f, color);
    }

    static void fillCircle(CanvasContext ctx, float cx, float cy, float r, int color) {
        int ri = Math.max(1, (int) r);
        for (int y = -ri; y <= ri; y++) {
            float xw = (float) Math.sqrt(Math.max(0.0, (double) r * r - (double) y * y));
            ctx.fill(cx - xw, cy + y, xw * 2f, 1f, color);
        }
    }

    static void fillHexagon(CanvasContext ctx, float cx, float cy, float r, int color) {
        float halfWidth = r * (float) Math.sqrt(3) / 2f;
        float shoulder = r / 2f;
        for (int y = -(int) r; y <= (int) r; y++) {
            float xw;
            if (y <= -shoulder) {
                xw = (y + r) * halfWidth / (r - shoulder);
            } else if (y <= shoulder) {
                xw = halfWidth;
            } else {
                xw = (r - y) * halfWidth / (r - shoulder);
            }
            ctx.fill(cx - xw, cy + y, xw * 2f, 1f, color);
        }
    }

    static void fillShape(CanvasContext ctx, SkillType type, float cx, float cy, float r,
                          int borderColor, int fillColor) {
        switch (type) {
            case ACTIVE -> {
                fillSquare(ctx, cx, cy, r, borderColor);
                fillSquare(ctx, cx, cy, r - 2f, fillColor);
            }
            case PASSIVE -> {
                fillCircle(ctx, cx, cy, r, borderColor);
                fillCircle(ctx, cx, cy, r - 2f, fillColor);
            }
            case SPECIAL -> {
                fillHexagon(ctx, cx, cy, r, borderColor);
                fillHexagon(ctx, cx, cy, r - 2f, fillColor);
            }
        }
    }
}
