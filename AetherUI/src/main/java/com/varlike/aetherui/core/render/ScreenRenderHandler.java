package com.varlike.aetherui.core.render;

import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.layout.DefaultMeasureContext;
import com.varlike.aetherui.core.layout.LayoutEngine;
import com.varlike.aetherui.core.registry.ScreenUIRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** 屏幕 UI 的渲染入口：注册一个 GUI layer，每帧遍历已注册的 UI 树。 */
public final class ScreenRenderHandler {

    public static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath("aetherui", "screen_ui");

    private ScreenRenderHandler() {}

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID,
                (graphics, deltaTracker) -> renderAll(graphics));
    }

    public static void renderAll(GuiGraphics graphics) {
        com.varlike.aetherui.core.hud.WorldHealthBarRenderer.renderBars(graphics);
        com.varlike.aetherui.core.hud.WorldMarkerRenderer.renderMarkers(graphics);
        WorldRenderHandler.renderAll(graphics);

        if (ScreenUIRegistry.isEmpty()) return;

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        for (UITree tree : ScreenUIRegistry.all()) {
            if (!tree.isVisible()) continue;
            if (!(tree.target() instanceof ScreenRenderTarget target)) continue;
            if (tree.component() == null) continue; // 旧 Panel API 创建的树暂不渲染

            LayoutEngine.LayoutResult result =
                    LayoutEngine.layoutDetailed(tree.component(), DefaultMeasureContext.INSTANCE);
            if (result.snapshots().isEmpty()) continue;

            Vec2 anchor = resolveAnchor(target.screenAnchor(), screenW, screenH);
            float scale = target.guiScale();
            Alignment align = target.alignment();
            float rootW = result.rootWidth() * scale;
            float rootH = result.rootHeight() * scale;
            float baseX = anchor.x - rootW * align.u();
            float baseY = anchor.y - rootH * align.v();

            SnapshotRenderer.render(result.snapshots(), graphics, baseX, baseY, scale);
        }
    }

    /** 把 ScreenAnchor 解析为屏幕 scaled 坐标系中的像素位置。 */
    private static Vec2 resolveAnchor(ScreenAnchor anchor, int screenW, int screenH) {
        return switch (anchor) {
            case ScreenAnchor.Corner c -> new Vec2(
                    screenW * c.corner().u() + c.offsetX(),
                    screenH * c.corner().v() + c.offsetY());
            case ScreenAnchor.EdgeMid e -> new Vec2(
                    screenW * e.edge().u(),
                    screenH * e.edge().v() + e.offset());
            case ScreenAnchor.Percent p -> new Vec2(screenW * p.xRatio(), screenH * p.yRatio());
            case ScreenAnchor.Center ignored -> new Vec2(screenW / 2f, screenH / 2f);
        };
    }
}
