package com.varlike.spiritworld.client.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientScanData;
import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

/**
 * 扫描图标的世界空间渲染。
 * 穿墙可见，5~13 秒后由 ClientScanData 自动过期。
 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public final class SpiritScanRenderer {

    private SpiritScanRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        List<OreIcon> icons = ClientScanData.getActiveIcons();
        if (icons.isEmpty()) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        // 穿墙：关闭深度测试
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        try {
            for (OreIcon icon : icons) {
                double dx = icon.x() - camPos.x;
                double dy = icon.y() - camPos.y;
                double dz = icon.z() - camPos.z;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                pose.pushPose();
                pose.translate(dx, dy, dz);
                pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

                // 距离越远越小，0.012~0.028 之间
                float scale = (float) Math.max(0.012, Math.min(0.028, 2.5 / Math.max(1.0, dist)));
                pose.scale(-scale, -scale, scale);

                // ===== 图标（物品）=====
                pose.pushPose();
                pose.translate(-8f, -24f, 0f);
                pose.scale(16f, 16f, 16f);
                itemRenderer.renderStatic(
                        icon.iconItem().getDefaultInstance(),
                        ItemDisplayContext.GUI,
                        0xF000F0,
                        OverlayTexture.NO_OVERLAY,
                        pose,
                        buffer,
                        mc.level,
                        0);
                pose.popPose();

                // ===== 文字：名称 + 距离 + 数量 =====
                String label = icon.iconItem().getDescription().getString()
                        + " -" + (int) dist + "m";
                if (icon.count() > 1) label += " ×" + icon.count();
                Component text = Component.literal(label);

                float textX = -mc.font.width(text) / 2f;
                float textY = 0f;
                mc.font.drawInBatch(text, textX, textY, 0xFFFFFFFF, false,
                        pose.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH,
                        0x80000000, 0xF000F0);

                pose.popPose();
            }
        } finally {
            buffer.endBatch();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}