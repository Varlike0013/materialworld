package com.varlike.spiritworld.client.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientScanData;
import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public final class SpiritScanRenderer {

    private SpiritScanRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 1. 使用 AFTER_LEVEL 保证在所有方块和实体之后渲染，避免被地平线或水切掉
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        List<OreIcon> icons = ClientScanData.getActiveIcons();
        if (icons.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        // 初始化 2D 绘图工具
        GuiGraphics guiGraphics = new GuiGraphics(mc, bufferSource);

        //  核心穿透逻辑：彻底关闭深度测试，确保 UI 无视任何方块遮挡
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        try {
            for (OreIcon icon : icons) {
                double dx = icon.x() - camPos.x;
                double dy = icon.y() - camPos.y;
                double dz = icon.z() - camPos.z;

                // 计算真实距离
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // 超过 XXX 米的图标不渲染，防止太乱
                if (dist > 256) continue;

                poseStack.pushPose();
                // 平移到世界坐标
                poseStack.translate(dx, dy, dz);
                // 面向相机 (Billboard 广告牌效果)
                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

                // 动态缩放：距离越远，图标和文字越小
                float scale = (float) Math.clamp(2.5 / Math.max(1.0, dist), 0.02, 0.05);
                poseStack.scale(-scale, -scale, scale);

                // ===== 1. 渲染物品图标 =====
                poseStack.pushPose();
                poseStack.translate(-8f, -22f, 0f); // 向上偏移，给下方文字留空间
                poseStack.scale(16f, 16f, 16f);     // 放大到正常 16x16 大小
                itemRenderer.renderStatic(
                        icon.iconItem().getDefaultInstance(),
                        ItemDisplayContext.GUI,
                        LightTexture.FULL_BRIGHT, // 确保图标在最暗的地方也能亮
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        mc.level,
                        0
                );
                poseStack.popPose();

                // ===== 2. 渲染黑灰色半透明背景和距离文字 =====
                String label = String.format("%.1f米", dist);
                int textWidth = mc.font.width(label);
                int bgWidth = Math.max(24, textWidth + 8);
                int bgHeight = 13;
                int bgX = -bgWidth / 2;
                int bgY = 2;

                // 魔法操作：将当前的 3D 矩阵覆盖给 guiGraphics 的 2D 矩阵
                guiGraphics.pose().pushPose();
                guiGraphics.pose().last().pose().set(poseStack.last().pose());

                // 绘制黑灰色半透明背景 (0xAA1E1E1E：A是透明度，后面是RGB)
                guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0xAA1E1E1E);

                // 绘制白色距离文字
                guiGraphics.drawString(mc.font, label, -textWidth / 2, bgY + 2, 0xFFFFFFFF, true);

                guiGraphics.pose().popPose();

                poseStack.popPose();
            }
        } finally {
            // 3. 恢复渲染状态，防止污染游戏后续的渲染
            bufferSource.endBatch();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}