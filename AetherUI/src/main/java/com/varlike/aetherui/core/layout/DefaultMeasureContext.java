package com.varlike.aetherui.core.layout;

import com.varlike.aetherui.api.layout.MeasureContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class DefaultMeasureContext implements MeasureContext {

    public static final DefaultMeasureContext INSTANCE = new DefaultMeasureContext();

    private DefaultMeasureContext() {}

    @Override
    public float measureTextWidth(Component text, float scale) {
        return Minecraft.getInstance().font.width(text) * scale;
    }

    @Override
    public float textLineHeight(float scale) {
        return Minecraft.getInstance().font.lineHeight * scale;
    }
}