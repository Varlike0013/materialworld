package com.varlike.aetherui.core.render;

import com.varlike.aetherui.AetherUIMod;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.snapshot.ElementSnapshot;
import com.varlike.aetherui.api.target.Anchor;
import com.varlike.aetherui.api.target.Orientation;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import com.varlike.aetherui.core.layout.DefaultMeasureContext;
import com.varlike.aetherui.core.layout.LayoutEngine;
import com.varlike.aetherui.core.registry.WorldUIRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 世界空间 UI 渲染入口。
 *
 * <p>在 {@code AFTER_LEVEL} 阶段把每棵世界树按 {@link Anchor} 解析出世界坐标，
 * 投影到屏幕 GUI 坐标并按距离计算缩放，缓存为 {@link Projected}；随后由
 * {@link ScreenRenderHandler} 的 GUI layer 调用 {@link #renderAll(GuiGraphics)}，
 * 复用 {@link SnapshotRenderer} 把同一套 {@link ElementSnapshot} 画出来。
 *
 * <p>因此同一棵组件树（UIComponent）可同时构建为 ScreenRenderTarget 与 WorldRenderTarget，
 * 一次声明，两处绘制。当前世界 UI 以 billboard（始终面向摄像机）方式绘制；
 * {@link com.varlike.aetherui.api.target.Orientation} 预留给未来的真 3D 几何渲染。
 */
@EventBusSubscriber(modid = AetherUIMod.MOD_ID, value = Dist.CLIENT)
public final class WorldRenderHandler {

    /**
     * @param yawDeg   绕 Y 轴总旋转角（度，朝向策略 + 自旋），垂直轴翻转
     * @param pitchDeg 绕 X 轴总旋转角（度，朝向策略 + 自旋），水平轴翻转
     * @param rollDeg  绕 Z 轴总旋转角（度，屏幕平面转圈）
     * @param panelW   面板逻辑宽度（旋转中心用）
     * @param panelH   面板逻辑高度
     */
    private record Projected(List<ElementSnapshot> snapshots, float baseX, float baseY, float scale,
                             float yawDeg, float pitchDeg, float rollDeg, float panelW, float panelH) {}

    private static final List<Projected> PROJECTED = new ArrayList<>();

    private WorldRenderHandler() {}

    @SubscribeEvent
    static void onRenderLevel(RenderLevelStageEvent event) {
        PROJECTED.clear();
        if (!com.varlike.aetherui.Config.worldUIEnabled()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        if (WorldUIRegistry.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        int screenW = mc.getWindow().getWidth();
        int screenH = mc.getWindow().getHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        // 相机右/上方向（世界坐标），来自 view 矩阵的旋转部分
        Vector3f right = new Vector3f(modelView.m00(), modelView.m01(), modelView.m02()).normalize();
        Vector3f up    = new Vector3f(modelView.m10(), modelView.m11(), modelView.m12()).normalize();

        for (UITree tree : WorldUIRegistry.all()) {
            if (!tree.isVisible()) continue;
            if (!(tree.target() instanceof WorldRenderTarget target)) continue;
            if (tree.component() == null) continue;

            Vec3 worldPos = resolveAnchor(target.anchor(), mc.level, player, right, up);
            if (worldPos == null) continue;

            double dist = worldPos.distanceTo(camPos);
            if (dist > target.maxRenderDistance() || dist < 0.1) continue;

            if (!target.seeThrough() && isOccluded(mc.level, camPos, worldPos, player, dist)) continue;

            Vector4f p = project(worldPos, camPos, modelView, projection);
            if (p == null) continue;
            float sx = (p.x() / p.w() + 1f) * 0.5f * screenW / guiScale;
            float sy = (1f - p.y() / p.w()) * 0.5f * screenH / guiScale;

            // 屏幕上 1 个世界方块的长度（GUI 像素）→ 逻辑像素缩放
            Vec3 oneBlock = worldPos.add(right.x(), right.y(), right.z());
            Vector4f q = project(oneBlock, camPos, modelView, projection);
            float scale;
            if (q != null && q.w() > 0f) {
                float qx = (q.x() / q.w() + 1f) * 0.5f * screenW / guiScale;
                float qy = (1f - q.y() / q.w()) * 0.5f * screenH / guiScale;
                float blockPx = (float) Math.hypot(qx - sx, qy - sy);
                scale = blockPx / Math.max(1f, target.pixelsPerBlock());
            } else {
                scale = 10f / (float) dist; // 退化：按距离衰减
            }
            scale = Mth.clamp(scale, 0.1f, 4f);

            LayoutEngine.LayoutResult result =
                    LayoutEngine.layoutDetailed(tree.component(), DefaultMeasureContext.INSTANCE);
            if (result.snapshots().isEmpty()) continue;

            // 解析朝向策略：基准 yaw/pitch（相对相机），并做背面剔除
            Orientation orientation = resolveOrientation(target.orientation(), camera);
            float baseYawDeg = 0f, basePitchDeg = 0f;
            if (orientation instanceof Orientation.Fixed fixed) {
                // 固定世界朝向 → 相对相机的旋转角（始终面向 world 固定方向）
                baseYawDeg = fixed.yawDegrees() - camera.getYRot();
                basePitchDeg = fixed.pitchDegrees() - camera.getXRot();
                if (!target.doubleSided() && Math.abs(baseYawDeg) > 90f) continue;
            } else if (orientation instanceof Orientation.Vertical) {
                basePitchDeg = 0f; // 仅绕 Y 轴面向玩家，保持竖直
            } else if (orientation instanceof Orientation.Center) {
                baseYawDeg = 0f; basePitchDeg = 0f; // billboard
            }

            // 求值用户的三轴自旋角（每帧，支持表达式/参数绑定）
            Supplier<Float> yawSupplier = target.yawDegrees();
            Supplier<Float> pitchSupplier = target.pitchDegrees();
            Supplier<Float> rollSupplier = target.rollDegrees();
            Float spinYaw = yawSupplier != null ? yawSupplier.get() : null;
            Float spinPitch = pitchSupplier != null ? pitchSupplier.get() : null;
            Float spinRoll = rollSupplier != null ? rollSupplier.get() : null;

            float yawDeg = baseYawDeg + (spinYaw != null ? spinYaw : 0f);
            float pitchDeg = basePitchDeg + (spinPitch != null ? spinPitch : 0f);
            float rollDeg = spinRoll != null ? spinRoll : 0f;

            Alignment align = target.alignment();
            float panelW = result.rootWidth() * scale;
            float panelH = result.rootHeight() * scale;
            float baseX = sx - panelW * align.u();
            float baseY = sy - panelH * align.v();

            PROJECTED.add(new Projected(result.snapshots(), baseX, baseY, scale,
                    yawDeg, pitchDeg, rollDeg, panelW, panelH));
        }
    }

    /** 由 ScreenRenderHandler 的 GUI layer 调用，绘制本帧投影出的世界 UI。 */
    public static void renderAll(GuiGraphics g) {
        if (PROJECTED.isEmpty()) return;
        for (Projected p : PROJECTED) {
            boolean needsTransform = p.yawDeg() != 0f || p.pitchDeg() != 0f || p.rollDeg() != 0f;
            if (!needsTransform) {
                SnapshotRenderer.render(p.snapshots(), g, p.baseX(), p.baseY(), p.scale());
                continue;
            }

            // 旋转中心 = 面板中心（屏幕坐标）
            float cx = p.baseX() + p.panelW() * 0.5f;
            float cy = p.baseY() + p.panelH() * 0.5f;
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0f);

            // 三轴真 3D 旋转：绕 Y（垂直轴翻转）→ X（水平轴翻）→ Z（屏幕平面转圈）
            // GUI pose 是屏幕像素空间（x 右、y 下、z 朝观察者），用四元数组合避免万向锁
            Quaternionf rotation = new Quaternionf();
            if (p.yawDeg() != 0f) {
                rotation.rotateY(p.yawDeg() * ((float) Math.PI / 180f));
            }
            if (p.pitchDeg() != 0f) {
                rotation.rotateX(p.pitchDeg() * ((float) Math.PI / 180f));
            }
            if (p.rollDeg() != 0f) {
                rotation.rotateZ(p.rollDeg() * ((float) Math.PI / 180f));
            }
            g.pose().mulPose(rotation);

            // 平移回面板原始绘制坐标系
            g.pose().translate(-cx, -cy, 0f);
            SnapshotRenderer.render(p.snapshots(), g, p.baseX(), p.baseY(), p.scale());
            g.pose().popPose();
        }
    }

    /** 求值朝向：动态朝向每帧读取 Bindable，静态朝向直接返回。 */
    private static Orientation resolveOrientation(Orientation orientation, Camera camera) {
        if (orientation instanceof Orientation.Dynamic dynamic) {
            Orientation resolved = dynamic.supplier().get();
            return resolved != null ? resolved : Orientation.CENTER;
        }
        return orientation;
    }

    private static Vector4f project(Vec3 worldPos, Vec3 camPos, Matrix4f modelView, Matrix4f projection) {
        Vector4f v = new Vector4f(
                (float) (worldPos.x - camPos.x),
                (float) (worldPos.y - camPos.y),
                (float) (worldPos.z - camPos.z), 1f);
        v.mul(modelView).mul(projection);
        return v.w() <= 0f ? null : v;
    }

    private static Vec3 resolveAnchor(Anchor anchor, Level level, LocalPlayer player,
                                      Vector3f right, Vector3f up) {
        return switch (anchor) {
            case Anchor.Fixed f -> f.worldPos();
            case Anchor.Entity e -> {
                Entity entity = null;
                if (level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
                    for (Entity candidate : clientLevel.entitiesForRendering()) {
                        if (candidate.getUUID().equals(e.entityId())) {
                            entity = candidate;
                            break;
                        }
                    }
                }
                if (entity == null) yield null;
                Vec3 o = e.offset();
                // offset 相对实体脚底、实体局部坐标系；用实体朝向把局部偏移旋转到世界
                float yawRad = entity.getYRot() * ((float) Math.PI / 180f);
                double ox = o.x * Math.cos(yawRad) - o.z * Math.sin(yawRad);
                double oz = o.x * Math.sin(yawRad) + o.z * Math.cos(yawRad);
                yield new Vec3(entity.getX() + ox, entity.getY() + o.y, entity.getZ() + oz);
            }
            case Anchor.PlayerRelative pr -> {
                Vec3 o = pr.offset();
                Vec3 look = player.getLookAngle();
                Vec3 base = player.position().add(0, player.getEyeHeight(), 0);
                yield base.add(
                        look.x * o.z + right.x * o.x + up.x * o.y,
                        look.y * o.z + right.y * o.x + up.y * o.y,
                        look.z * o.z + right.z * o.x + up.z * o.y);
            }
            case Anchor.Dynamic dynamic -> dynamic.supplier().get();
        };
    }

    private static boolean isOccluded(Level level, Vec3 from, Vec3 to, LocalPlayer player, double dist) {
        BlockHitResult hit = level.clip(new ClipContext(
                from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK
                && hit.getLocation().distanceTo(from) < dist - 0.5f;
    }
}
