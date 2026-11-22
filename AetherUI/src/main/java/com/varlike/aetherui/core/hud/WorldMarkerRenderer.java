package com.varlike.aetherui.core.hud;

import com.varlike.aetherui.AetherUIMod;
import com.varlike.aetherui.api.hud.WorldMarker;
import com.varlike.aetherui.api.hud.WorldMarkerProvider;
import com.varlike.aetherui.core.registry.WorldMarkerRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 3D 世界标记渲染器：把 {@link WorldMarkerProvider} 提供的标记投影到屏幕，
 * 绘制白色边框 + 黑色半透明背景 + 左侧 PNG 图标 + 右侧多行彩色文字。
 * 每个标记可独立决定是否穿透墙体（不可穿透时做 VISUAL 射线遮挡检测）。
 */
@EventBusSubscriber(modid = AetherUIMod.MOD_ID, value = Dist.CLIENT)
public final class WorldMarkerRenderer {

    private record Projected(WorldMarker marker, float x, float y, float scale) {}

    private static final List<Projected> MARKERS = new ArrayList<>();
    /** 记录每个标记 id 的首次出现时间，用于持续时间判断。 */
    private static final Map<String, Long> MARKER_BIRTH = new HashMap<>();
    /** 本帧仍存活的标记 id，用于清理已消失标记的计时记录。 */
    private static final Set<String> ALIVE = new HashSet<>();

    private static final float ICON_SIZE = 16f;
    private static final float LINE_H = 12f;
    private static final int PAD = 4;
    private static final int BORDER = 0x99D0D0D0; // 半透明灰白色边框
    private static final int BG = 0x70000000;     // 半透明黑底（约 44% 不透明）
    private static final float MAX_DISTANCE = 64f;
    private static final float BASE_SCALE = 1f;

    private WorldMarkerRenderer() {}

    @SubscribeEvent
    static void onRenderLevel(RenderLevelStageEvent event) {
        MARKERS.clear();
        ALIVE.clear();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        long now = System.currentTimeMillis();

        if (!WorldMarkerRegistry.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && mc.level != null) {
                collectMarkers(mc, player, now, event);
            }
        }

        // 清理已消失标记的计时记录（同一 id 再次出现时重新计时）
        MARKER_BIRTH.keySet().removeIf(key -> !ALIVE.contains(key));
    }

    private static void collectMarkers(Minecraft mc, LocalPlayer player, long now,
                                       RenderLevelStageEvent event) {
        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        int screenW = mc.getWindow().getWidth();
        int screenH = mc.getWindow().getHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        for (WorldMarkerProvider provider : WorldMarkerRegistry.all()) {
            List<WorldMarker> markers = provider.getMarkers(mc.level, player);
            if (markers == null) continue;

            String providerId = provider.id().toString();
            for (WorldMarker marker : markers) {
                // 生命周期：按 provider+marker id 跟踪首次出现时间
                String key = providerId + "/" + marker.id();
                ALIVE.add(key);
                long birth = MARKER_BIRTH.computeIfAbsent(key, k -> now);
                if (marker.durationMs() > 0 && now - birth >= marker.durationMs()) {
                    // 已超时，不再渲染；下一帧因不再 ALIVE 而被清理
                    continue;
                }

                Vec3 pos = marker.position();

                // 遮挡检测：不可穿透时，相机到标记点做 VISUAL 射线
                if (!marker.throughWall()) {
                    BlockHitResult hit = mc.level.clip(new ClipContext(
                            camPos, pos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
                    if (hit.getType() == HitResult.Type.BLOCK
                            && hit.getLocation().distanceTo(camPos) < pos.distanceTo(camPos) - 0.5f) {
                        continue;
                    }
                }

                double dist = pos.distanceTo(camPos);
                if (dist > MAX_DISTANCE || dist < 0.1) continue;

                Vector4f p = new Vector4f(
                        (float) (pos.x - camPos.x),
                        (float) (pos.y - camPos.y),
                        (float) (pos.z - camPos.z), 1f);
                p.mul(modelView).mul(projection);
                if (p.w() <= 0f) continue;

                float ndcX = p.x() / p.w();
                float ndcY = p.y() / p.w();
                if (ndcX < -1f || ndcX > 1f || ndcY < -1f || ndcY > 1f) continue;

                float sx = (ndcX + 1f) * 0.5f * screenW / guiScale;
                float sy = (1f - ndcY) * 0.5f * screenH / guiScale;
                float scale = BASE_SCALE;

                MARKERS.add(new Projected(marker, sx, sy, scale));
            }
        }
    }

    /** 由 ScreenRenderHandler 的 GUI layer 调用。 */
    public static void renderMarkers(GuiGraphics g) {
        if (MARKERS.isEmpty()) return;
        var font = Minecraft.getInstance().font;

        for (Projected proj : MARKERS) {
            WorldMarker marker = proj.marker();
            List<Component> lines = marker.lines();
            if (lines.isEmpty() && marker.icon() == null) continue;

            int maxLineW = 0;
            for (Component line : lines) maxLineW = Math.max(maxLineW, font.width(line));

            int iconW = marker.icon() != null ? Math.round(ICON_SIZE) : 0;
            int gap = (iconW > 0 && !lines.isEmpty()) ? PAD : 0;
            int panelW = iconW + gap + maxLineW + PAD * 2;
            int panelH = Math.max(iconW, lines.size() * Math.round(LINE_H)) + PAD * 2;
            if (panelW <= 0 || panelH <= 0) continue;

            int x = Math.round(proj.x()) - panelW / 2;
            int y = Math.round(proj.y()) - panelH / 2;

            drawPanel(g, x, y, panelW, panelH);

            int contentX = x + PAD;
            int contentY = y + PAD;

            // 左侧 PNG 图标
            if (marker.icon() != null) {
                drawIcon(g, marker.icon(), contentX, contentY, iconW);
                contentX += iconW + gap;
            }

            // 右侧多行彩色文字（Component 样式自带颜色）
            for (Component line : lines) {
                g.drawString(font, line, contentX, contentY, 0xFFFFFFFF, false);
                contentY += LINE_H;
            }
        }
    }

    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        // 半透明黑底
        g.fill(x, y, x + w, y + h, BG);
        // 半透明灰白色边框，四边等宽
        g.fill(x, y, x + w, y + 1, BORDER);
        g.fill(x, y + h - 1, x + w, y + h, BORDER);
        g.fill(x, y, x + 1, y + h, BORDER);
        g.fill(x + w - 1, y, x + w, y + h, BORDER);
    }

    private static void drawIcon(GuiGraphics g, ResourceLocation texture, int x, int y, int size) {
        // 图标灰白边框（略大于图标 1px）
        int bx = x - 1, by = y - 1, bw = size + 2, bh = size + 2;
        g.fill(bx, by, bx + bw, by + 1, BORDER);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, BORDER);
        g.fill(bx, by, bx + 1, by + bh, BORDER);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, BORDER);
        // 整张 PNG 缩放到 size×size；纹理通常为 16x16 物品图标
        g.blit(texture, x, y, size, size, 0f, 0f, size, size, size, size);
    }
}
