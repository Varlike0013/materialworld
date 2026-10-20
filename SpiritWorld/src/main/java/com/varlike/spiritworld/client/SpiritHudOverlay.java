package com.varlike.spiritworld.client;

import com.varlike.spiritworld.ClientConfig;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.skill.SkillRenderer;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class SpiritHudOverlay {

    /** 与饥饿条一致的宽度：9 个图标 × 9 像素 */
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;

    /** 饥饿条顶部距屏幕底部的距离（原版约 39） */
    private static final int FOOD_BAR_BOTTOM_OFFSET = 39;
    /** 精神力条与饥饿条之间的间距 */
    private static final int GAP = 2;

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_bar");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // 在饥饿条之上渲染（视觉上会叠加，Y 坐标由我们自己控制到饥饿条上方）
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                LAYER_ID,
                SpiritHudOverlay::renderSpiritBar
        );
    }
    // ===== 被动图标渲染方法 =====
    private static void renderActivePassives(GuiGraphics graphics, Minecraft mc, int barX, int barY) {
        int maxRows = ClientConfig.hudPassiveMaxRows;
        int perRow = ClientConfig.hudPassivePerRow;
        int iconSize = ClientConfig.hudPassiveIconSize;
        int capacity = maxRows * perRow;

        List<ResourceLocation> active = new ArrayList<>();
        for (SkillNode node : SkillTree.getHud()) {
            if (node.getType() != SkillType.PASSIVE) continue;
            if (ClientSpiritData.isPassiveActive(node.getId())) {
                active.add(node.getId());
            }
        }
        if (active.isEmpty()) return;

        int total = Math.min(active.size(), capacity);
        int rows = (total + perRow - 1) / perRow;

        // 每行从左到右排列，多行向上叠加
        int startX = barX;
        int bottomY = barY - 4;

        for (int i = 0; i < total; i++) {
            int row = i / perRow;
            int col = i % perRow;
            int ix = startX + col * (iconSize + 2);
            int iy = bottomY - (row + 1) * (iconSize + 2);

            ResourceLocation id = active.get(i);
            SkillNode node = SkillTree.get(id);
            if (node == null) continue;

            SkillRenderer.fillCircle(graphics, ix + iconSize / 2, iy + iconSize / 2, iconSize / 2,
                    node.isAlwaysOn() ? 0xFF4CAF50 : 0xFFFFD54F);
            SkillRenderer.fillCircle(graphics, ix + iconSize / 2, iy + iconSize / 2, iconSize / 2 - 1,
                    0xC0201030);
            graphics.drawCenteredString(mc.font, node.getIcon(),
                    ix + iconSize / 2, iy + iconSize / 2 - 4, 0xFFFFFFFF);
        }
    }
    private static void renderSpiritBar(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ClientConfig.showSpiritBar) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;
        // 创造 / 旁观模式不显示
        if (mc.player.isCreative() || mc.player.isSpectator()) return;

        long spirit = ClientSpiritData.getSpirit();
        long maxSpirit = ClientSpiritData.getMaxSpirit();
        if (maxSpirit <= 0) return;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // 与饥饿条右对齐
        int x = screenWidth / 2 + 91 - BAR_WIDTH;
        // 饥饿条顶部 = screenHeight - 39；精神条在其上方，再加用户偏移
        int y = screenHeight - FOOD_BAR_BOTTOM_OFFSET - BAR_HEIGHT - GAP
                - ClientConfig.spiritBarYOffset;

        // ===== 背景 =====
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        // ===== 空槽 =====
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF303030);

        // ===== 填充 =====
        float ratio = (float) spirit / (float) maxSpirit;
        int filled = Math.round(BAR_WIDTH * ratio);
        if (filled > 0) {
            // 三段渐变：左深蓝 → 中亮蓝 → 右淡蓝
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF0066CC);
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT / 2, 0xFF3399FF);
        }

        // ===== 文本 =====
        if (ClientConfig.showSpiritText) {
            Component text = Component.literal(spirit + " / " + maxSpirit);
            int textWidth = mc.font.width(text);
            int textX = x + (BAR_WIDTH - textWidth) / 2;
            int textY = y - 10;

            // 阴影
            graphics.drawString(mc.font, text, textX + 1, textY + 1, 0xFF000000, false);
            graphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF, false);
        }
        // ===== 精神力条右侧：当前技能图标 =====
        ResourceLocation equippedId = ClientSpiritData.getEquippedSkill();
        if (equippedId != null) {
            SkillNode node = SkillTree.get(equippedId);
            if (node != null) {
                int iconX = x + BAR_WIDTH + 4;
                int iconY = y - 4;

                boolean isActive = ClientSpiritData.isPassiveActive(equippedId)
                        || node.isAlwaysOn();

                int borderColor = isActive ? 0xFF4CAF50 : 0xFFFFD54F;
                SkillRenderer.fillShape(graphics, node.getType(), iconX + 8, iconY + 8, 9,
                        borderColor, 0x80201030);
                graphics.drawCenteredString(mc.font, node.getIcon(), iconX + 8, iconY + 4, 0xFFFFFFFF);

                // 槽位编号
                int slot = ClientSpiritData.getSelectedSlot();
                graphics.drawString(mc.font, String.valueOf(slot + 1),
                        iconX, iconY + 20, 0xFFAAAAAA, false);
            }
        }

        // ===== 精神力条上方：已激活被动图标 =====
        renderActivePassives(graphics, mc, x, y);
    }
}