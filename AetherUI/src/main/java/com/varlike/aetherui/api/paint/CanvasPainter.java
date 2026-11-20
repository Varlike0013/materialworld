package com.varlike.aetherui.api.paint;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface CanvasPainter {
    void paint(CanvasContext ctx);
}