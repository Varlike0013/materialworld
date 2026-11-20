package com.varlike.aetherui.api.layout;

import net.minecraft.network.chat.Component;

public interface MeasureContext {

    /** 计算 Component 在给定缩放下的逻辑像素宽度。 */
    float measureTextWidth(Component text, float scale);

    /** 单行文字的逻辑像素高度。 */
    float textLineHeight(float scale);
}