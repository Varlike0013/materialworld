package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.skill.SkillType;
import net.minecraft.client.gui.GuiGraphics;

public final class SkillRenderer {

    private SkillRenderer() {}

    /** 绘制实心正方形 */
    public static void fillSquare(GuiGraphics g, int cx, int cy, int half, int color) {
        g.fill(cx - half, cy - half, cx + half, cy + half, color);
    }

    /** 绘制实心圆形 */
    public static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int y = -r; y <= r; y++) {
            int xw = (int) Math.sqrt((double) r * r - (double) y * y);
            g.fill(cx - xw, cy + y, cx + xw + 1, cy + y + 1, color);
        }
    }

    /** 绘制实心正六边形（顶点朝上） */
    public static void fillHexagon(GuiGraphics g, int cx, int cy, int r, int color) {
        int halfWidth = (int) (r * Math.sqrt(3) / 2);
        int shoulder = r / 2;
        for (int y = -r; y <= r; y++) {
            int xw;
            if (y <= -shoulder) {
                xw = (int) ((y + r) * (double) halfWidth / (r - shoulder));
            } else if (y <= shoulder) {
                xw = halfWidth;
            } else {
                xw = (int) ((r - y) * (double) halfWidth / (r - shoulder));
            }
            g.fill(cx - xw, cy + y, cx + xw + 1, cy + y + 1, color);
        }
    }

    /** 按形状绘制：外边框色 + 内填充色（模拟双线边框） */
    public static void fillShape(GuiGraphics g, SkillType type, int cx, int cy, int r,
                                 int borderColor, int fillColor) {
        switch (type) {
            case ACTIVE -> {
                fillSquare(g, cx, cy, r, borderColor);
                fillSquare(g, cx, cy, r - 2, fillColor);
            }
            case PASSIVE -> {
                fillCircle(g, cx, cy, r, borderColor);
                fillCircle(g, cx, cy, r - 2, fillColor);
            }
            case SPECIAL -> {
                fillHexagon(g, cx, cy, r, borderColor);
                fillHexagon(g, cx, cy, r - 2, fillColor);
            }
        }
    }

    /** 绘制外发光（呼吸光晕） */
    public static void fillGlow(GuiGraphics g, SkillType type, int cx, int cy, int r, int color) {
        switch (type) {
            case ACTIVE -> fillSquare(g, cx, cy, r, color);
            case PASSIVE -> fillCircle(g, cx, cy, r, color);
            case SPECIAL -> fillHexagon(g, cx, cy, r, color);
        }
    }
}