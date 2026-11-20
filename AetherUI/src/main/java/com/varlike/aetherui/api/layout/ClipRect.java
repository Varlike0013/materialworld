package com.varlike.aetherui.api.layout;

/** 相对于 UI 根节点左上角的裁剪矩形，单位逻辑像素。 */
public record ClipRect(float x, float y, float width, float height) {

    public ClipRect intersect(ClipRect other) {
        if (other == null) return this;
        float nx = Math.max(x, other.x);
        float ny = Math.max(y, other.y);
        float nx2 = Math.min(x + width, other.x + other.width);
        float ny2 = Math.min(y + height, other.y + other.height);
        return new ClipRect(nx, ny, Math.max(0, nx2 - nx), Math.max(0, ny2 - ny));
    }
}