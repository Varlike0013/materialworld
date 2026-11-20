package com.varlike.aetherui.core.layout;

import net.minecraft.network.chat.Component;

public interface MeasureContext {
    float measureTextWidth(Component component, float scale);
    float textLineHeight(float scale);
}