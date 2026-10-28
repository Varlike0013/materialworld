package com.varlike.spiritworld.client;

import com.varlike.spiritworld.ClientConfig;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.client.skill.SkillRenderer;
import com.varlike.spiritworld.skill.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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
    public static final ResourceLocation SPIRIT_INFO_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_info");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // 精神力条：在饥饿条之上渲染，Y 坐标由 renderSpiritBar 内部控制在饥饿条上方
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL, LAYER_ID,
                SpiritHudOverlay::renderSpiritBar
        );
        // 灵视信息：最顶层，确保不被其他 HUD 遮挡
        event.registerAboveAll(SPIRIT_INFO_LAYER_ID, SpiritHudOverlay::renderSpiritInfo);
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
        ratio = Math.clamp(ratio, 0.0f, 1.0f);
        int filled = Math.round(BAR_WIDTH * ratio);
        if (filled > 0) {
            // 三段渐变：左深蓝 → 中亮蓝 → 右淡蓝
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF0066CC);
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT / 2, 0xFF3399FF);
        }
        // ===== 被扣掉的上限（红色段） =====
        long baseMax = ClientSpiritData.getBaseMaxSpirit();
        if (baseMax > maxSpirit) {
            // 红色段的起点 = maxSpirit 在 bar 上的位置
            int redStart = Math.round(BAR_WIDTH * (float) maxSpirit / (float) baseMax);
            redStart = Math.clamp(redStart, 0, BAR_WIDTH);

            if (redStart < BAR_WIDTH) {
                // 深红底 + 亮红上半，和蓝色条风格一致
                graphics.fill(x + redStart, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF8B1A1A);
                graphics.fill(x + redStart, y, x + BAR_WIDTH, y + BAR_HEIGHT / 2, 0xFFCC4444);
            }
        }

        // ===== 文本 =====
        if (ClientConfig.showSpiritText) {
            Component text = Component.literal(spirit + " / " + maxSpirit);
            int textWidth = mc.font.width(text);
            int textX = x + (BAR_WIDTH - textWidth) / 2;
            // 阴影
            graphics.drawString(mc.font, text, textX + 1, y + 1, 0xFF000000, false);
            graphics.drawString(mc.font, text, textX, y, 0xFFFFFFFF, false);
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
        // ===== 飞行状态图标（新增）=====
        renderFlightIndicator(graphics, mc, x, y);
        // ===== 精神力条上方：已激活被动图标 =====
        renderActivePassives(graphics, mc, x, y);
    }
    private static void renderFlightIndicator(GuiGraphics g, Minecraft mc, int barX, int barY) {
        // 只在飞行被动激活时显示
        boolean flightActive = ClientSpiritData.isPassiveActive(ModSkills.SPIRIT_FLIGHT_ID);
        if (!flightActive) return;

        int lv = ClientSpiritData.getSkillLevel(ModSkills.SPIRIT_FLIGHT_ID);
        if (lv <= 0) return;

        // 放在精神力条左侧，和右侧技能图标对称
        int iconX = barX - 20;
        int iconY = barY - 4;

        // 状态色：正常飞行绿色，缓降黄色，精神力低红色
        long spirit = ClientSpiritData.getSpirit();
        double costPerSec = SkillValues.flightCostPerSecond(lv);
        int borderColor;
        if (spirit <= 0) {
            borderColor = 0xFFFF4444;          // 红：精神力耗尽
        } else if (spirit < costPerSec * SkillValues.FLIGHT_GLIDE_EXIT_SECONDS) {
            borderColor = 0xFFFFD54F;          // 黄：缓降阈值
        } else {
            borderColor = 0xFF4CAF50;          // 绿：正常飞行
        }

        // 外框 + 符号
        SkillRenderer.fillCircle(g, iconX + 8, iconY + 8, 8, borderColor);
        SkillRenderer.fillCircle(g, iconX + 8, iconY + 8, 6, 0x80201030);
        g.drawCenteredString(mc.font, "飞", iconX + 8, iconY + 4, 0xFFFFFFFF);
    }
    private static final int INFO_LINE_HEIGHT = 11;
    private static final int INFO_RIGHT_PADDING = 12;
    private static final int INFO_BG_PADDING_X = 4;
    private static final int INFO_BG_PADDING_Y = 3;

    private static void renderSpiritInfo(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (mc.level == null || mc.player == null) return;
        if (mc.hitResult == null) return;

        // 1. 从指向目标获取 SpiritInfoHolder
        SpiritInfoHolder holder = resolveTargetHolder(mc);
        if (holder == null) return;

        // 2. 取原始信息
        List<SpiritInfoLine> raw = holder.getSpiritInfo();
        if (raw == null || raw.isEmpty()) return;

        // 3. 按玩家灵视等级过滤
        int visionLevel = ClientSpiritData.getVisionLevel();
        List<Component> lines = new ArrayList<>();
        for (SpiritInfoLine line : raw) {
            if (line.isVisibleAt(visionLevel)) lines.add(line.text());
        }
        if (lines.isEmpty()) return;   // 灵视等级不足，全部过滤 → 不显示

        // 4. 半透明背景 + 右对齐 + 垂直居中
        drawInfoPanel(g, mc, lines);
    }

    /** 准星命中 BLOCK → 取 BE，命中 ENTITY → 取 Entity；都检查 SpiritInfoHolder */
    private static SpiritInfoHolder resolveTargetHolder(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be instanceof SpiritInfoHolder holder) return holder;
        } else if (hit instanceof EntityHitResult entityHit) {
            Entity e = entityHit.getEntity();
            if (e instanceof SpiritInfoHolder holder) return holder;
        }
        return null;
    }

    /** 画半透明背景板 + 右对齐 + 垂直居中 */
    private static void drawInfoPanel(GuiGraphics g, Minecraft mc, List<Component> lines) {
        // 计算最大行宽
        int maxWidth = 0;
        for (Component line : lines) {
            maxWidth = Math.max(maxWidth, mc.font.width(line));
        }

        int totalHeight = lines.size() * INFO_LINE_HEIGHT;
        int guiW = g.guiWidth();
        int guiH = g.guiHeight();

        int startY = (guiH - totalHeight) / 2;
        int rightX = guiW - INFO_RIGHT_PADDING;
        int textLeft = rightX - maxWidth;

        // 背景框：覆盖所有文字 + padding
        int bgLeft = textLeft - INFO_BG_PADDING_X;
        int bgTop = startY - INFO_BG_PADDING_Y;
        int bgRight = rightX + INFO_BG_PADDING_X;
        int bgBottom = startY + totalHeight + INFO_BG_PADDING_Y;
        g.fill(bgLeft, bgTop, bgRight, bgBottom, 0x80000000);   // 50% 黑底

        // 逐行渲染，右对齐
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            int textW = mc.font.width(line);
            int x = rightX - textW;
            int y = startY + i * INFO_LINE_HEIGHT;
            g.drawString(mc.font, line, x, y, 0xFFFFFFFF, true);
        }
    }

}