package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.network.ServerboundSetSelectedSlotPacket;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class RadialMenuScreen extends Screen {

    private static final int SLOT_COUNT = 6;
    private static final int INNER_RADIUS = 30;
    private static final int OUTER_RADIUS = 100;

    private static final int COLOR_RING = 0xC01A0F2E;
    private static final int COLOR_BORDER = 0xFF7E57C2;
    private static final int COLOR_HIGHLIGHT = 0xA0FFD54F;
    private static final int COLOR_SLOT = 0xA0201030;

    private int centerX, centerY;
    private int hoveredSlot = -1;
    private boolean closing = false;

    public RadialMenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        centerX = this.width / 2;
        centerY = this.height / 2;
        updateHoveredSlot();
    }

    private void updateHoveredSlot() {
        double dx = this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth()
                / this.minecraft.getWindow().getScreenWidth() - centerX;
        double dy = this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight()
                / this.minecraft.getWindow().getScreenHeight() - centerY;

        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < INNER_RADIUS || dist > OUTER_RADIUS * 1.5) {
            hoveredSlot = -1;
            return;
        }

        double angle = Math.atan2(dy, dx);
        // 归一化到 [0, 2π)，并将 -90° 作为扇区 0 中心
        angle = angle + Math.PI / 2;
        if (angle < 0) angle += Math.PI * 2;

        double sectorSize = Math.PI * 2 / SLOT_COUNT;
        // 偏移半个扇区，让 0 号槽位在正上方居中
        angle += sectorSize / 2;
        if (angle >= Math.PI * 2) angle -= Math.PI * 2;

        hoveredSlot = (int) (angle / sectorSize);
        if (hoveredSlot < 0) hoveredSlot = 0;
        if (hoveredSlot >= SLOT_COUNT) hoveredSlot = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        updateHoveredSlot();

        // ===== 不加全屏遮罩，只画圆环 =====

        // 环背景
        SkillRenderer.fillCircle(g, centerX, centerY, OUTER_RADIUS, COLOR_BORDER);
        SkillRenderer.fillCircle(g, centerX, centerY, OUTER_RADIUS - 2, COLOR_RING);
        // 中心镂空（用背景色清空）
        SkillRenderer.fillCircle(g, centerX, centerY, INNER_RADIUS, 0x00000000);

        // 扇区分隔和槽位图标
        for (int i = 0; i < SLOT_COUNT; i++) {
            double angle = -Math.PI / 2 + i * (Math.PI * 2 / SLOT_COUNT);
            double midR = (INNER_RADIUS + OUTER_RADIUS) / 2.0;
            int cx = centerX + (int) (Math.cos(angle) * midR);
            int cy = centerY + (int) (Math.sin(angle) * midR);

            boolean hovered = i == hoveredSlot;

            if (hovered) {
                SkillRenderer.fillCircle(g, cx, cy, 24, COLOR_HIGHLIGHT);
            } else {
                SkillRenderer.fillCircle(g, cx, cy, 24, COLOR_SLOT);
            }

            ResourceLocation id = ClientSpiritData.getLoadoutSlot(i);
            if (id != null) {
                SkillNode node = SkillTree.get(id);
                if (node != null) {
                    g.drawCenteredString(this.font, node.getIcon(), cx, cy - 4,
                            hovered ? 0xFFFFD54F : 0xFFFFFFFF);
                }
            } else {
                g.drawCenteredString(this.font, "空", cx, cy - 4, 0xFF666666);
            }

            // 槽位编号
            g.drawString(this.font, String.valueOf(i + 1),
                    cx - 20, cy - 20, 0xFFAAAAAA, false);
        }

        // 中心文字
        if (hoveredSlot >= 0) {
            ResourceLocation id = ClientSpiritData.getLoadoutSlot(hoveredSlot);
            if (id != null) {
                SkillNode node = SkillTree.get(id);
                if (node != null) {
                    g.drawCenteredString(this.font,
                            Component.translatable(node.getNameKey()),
                            centerX, centerY - 4, 0xFFFFD54F);
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && !closing) {
            closing = true;
            if (hoveredSlot >= 0) {
                PacketDistributor.sendToServer(new ServerboundSetSelectedSlotPacket(hoveredSlot));
            }
            this.minecraft.setScreen(null);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // 松开 G 关闭选盘
        if (com.varlike.spiritworld.client.SpiritKeyBindings.OPEN_RADIAL.matches(keyCode, scanCode)) {
            if (hoveredSlot >= 0) {
                PacketDistributor.sendToServer(new ServerboundSetSelectedSlotPacket(hoveredSlot));
            }
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}