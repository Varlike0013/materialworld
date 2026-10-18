package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.network.ServerboundSetLoadoutPacket;
import com.varlike.spiritworld.network.ServerboundSetSelectedSlotPacket;
import com.varlike.spiritworld.network.ServerboundTogglePassivePacket;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class SkillLoadoutScreen extends Screen {

    private static final int COLOR_BG_TOP = 0xFF1A0F2E;
    private static final int COLOR_BG_BOTTOM = 0xFF0D0618;
    private static final int COLOR_PANEL = 0xC0201030;
    private static final int COLOR_BORDER = 0xFF7E57C2;

    private static final int SLOT_RADIUS = 24;
    private static final int RING_RADIUS = 80;
    private static final int SLOT_COUNT = 6;

    // 技能列表布局常量（render 和 mouseClicked 共用，避免坐标漂移）
    private static final int LIST_BASE_OFFSET_X = 20;   // 相对 this.width / 2
    private static final int LIST_BASE_Y = 60;
    private static final int LIST_CELL = 40;
    private static final int LIST_PER_ROW = 6;
    private static final int LIST_ICON_HALF = 16;

    private int centerX, centerY;
    private int[] slotX = new int[SLOT_COUNT];
    private int[] slotY = new int[SLOT_COUNT];

    private SkillType filterType = null;
    private boolean onlyEquippable = false;

    private Button btnAll, btnEquippable, btnActive, btnPassive, btnSpecial, btnSwitchToTree;

    public SkillLoadoutScreen() {
        super(Component.translatable("screen.spiritworld.loadout.title"));
    }

    @Override
    protected void init() {
        super.init();

        centerX = this.width / 4;
        centerY = this.height / 2;

        for (int i = 0; i < SLOT_COUNT; i++) {
            double angle = -Math.PI / 2 + i * (Math.PI * 2 / SLOT_COUNT);
            slotX[i] = centerX + (int) (Math.cos(angle) * RING_RADIUS);
            slotY[i] = centerY + (int) (Math.sin(angle) * RING_RADIUS);
        }

        int bx = this.width - 20 - 60;
        int by = 30;

        btnAll = addRenderableWidget(Button.builder(
                        Component.translatable("screen.spiritworld.loadout.filter.all"),
                        b -> { filterType = null; onlyEquippable = false; })
                .bounds(bx, by, 60, 18).build());
        by += 22;
        btnEquippable = addRenderableWidget(Button.builder(
                        Component.translatable("screen.spiritworld.loadout.filter.equippable"),
                        b -> { filterType = null; onlyEquippable = true; })
                .bounds(bx, by, 60, 18).build());
        by += 22;
        btnActive = addRenderableWidget(Button.builder(
                        Component.translatable("skill.type.active"),
                        b -> { filterType = SkillType.ACTIVE; onlyEquippable = false; })
                .bounds(bx, by, 60, 18).build());
        by += 22;
        btnPassive = addRenderableWidget(Button.builder(
                        Component.translatable("skill.type.passive"),
                        b -> { filterType = SkillType.PASSIVE; onlyEquippable = false; })
                .bounds(bx, by, 60, 18).build());
        by += 22;
        btnSpecial = addRenderableWidget(Button.builder(
                        Component.translatable("skill.type.special"),
                        b -> { filterType = SkillType.SPECIAL; onlyEquippable = false; })
                .bounds(bx, by, 60, 18).build());

        // 页面切换：技能树
        by += 26;
        btnSwitchToTree = addRenderableWidget(Button.builder(
                        Component.translatable("screen.spiritworld.loadout.switch_to_tree"),
                        b -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new SkillTreeScreen());
                            }
                        })
                .bounds(bx, by, 60, 18).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFD54F);

        SkillRenderer.fillCircle(g, centerX, centerY, RING_RADIUS + SLOT_RADIUS + 8, COLOR_PANEL);

        for (int i = 0; i < SLOT_COUNT; i++) {
            ResourceLocation equipped = ClientSpiritData.getLoadoutSlot(i);
            boolean selected = i == ClientSpiritData.getSelectedSlot();

            int borderColor = selected ? 0xFFFFD54F : COLOR_BORDER;
            int fillColor = selected ? 0xA05D4037 : 0x80201030;

            SkillRenderer.fillCircle(g, slotX[i], slotY[i], SLOT_RADIUS, borderColor);
            SkillRenderer.fillCircle(g, slotX[i], slotY[i], SLOT_RADIUS - 2, fillColor);

            if (equipped != null) {
                SkillNode node = SkillTree.get(equipped);
                if (node != null) {
                    g.drawCenteredString(this.font, node.getIcon(), slotX[i], slotY[i] - 4, 0xFFFFFFFF);
                }
            } else {
                g.drawCenteredString(this.font, "空", slotX[i], slotY[i] - 4, 0xFF666666);
            }

            g.drawString(this.font, String.valueOf(i + 1),
                    slotX[i] - SLOT_RADIUS + 4, slotY[i] - SLOT_RADIUS + 4, 0xFFAAAAAA, false);
        }

        renderSkillList(g, mouseX, mouseY);
        renderPassiveToggles(g, mouseX, mouseY);

        g.drawCenteredString(this.font,
                Component.translatable("screen.spiritworld.loadout.hint"),
                this.width / 2, this.height - 20, 0xFFAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderSkillList(GuiGraphics g, int mouseX, int mouseY) {
        int listX = this.width / 2 + LIST_BASE_OFFSET_X;

        int i = 0;
        for (SkillNode node : SkillTree.getAll()) {
            if (ClientSpiritData.getSkillLevel(node.getId()) <= 0) continue;
            if (filterType != null && node.getType() != filterType) continue;
            if (onlyEquippable && !node.isEquippable()) continue;

            int col = i % LIST_PER_ROW;
            int row = i / LIST_PER_ROW;
            int cx = listX + col * LIST_CELL + LIST_CELL / 2;
            int cy = LIST_BASE_Y + row * LIST_CELL + LIST_CELL / 2;

            int borderColor = node.isEquippable() ? 0xFF4CAF50 : 0xFF666666;
            int fillColor = node.isEquippable() ? 0xFF2E1A4A : 0xFF1A1A1A;
            SkillRenderer.fillShape(g, node.getType(), cx, cy, LIST_ICON_HALF, borderColor, fillColor);

            g.drawCenteredString(this.font, node.getIcon(), cx, cy - 4, 0xFFFFFFFF);

            int lvl = ClientSpiritData.getSkillLevel(node.getId());
            g.drawCenteredString(this.font, String.valueOf(lvl), cx, cy + 6, 0xFFFFD54F);

            // 悬停高亮，方便确认命中区域
            if (Math.abs(mouseX - cx) <= LIST_ICON_HALF && Math.abs(mouseY - cy) <= LIST_ICON_HALF) {
                SkillRenderer.fillShape(g, node.getType(), cx, cy, LIST_ICON_HALF + 2, 0xFFFFFFFF, 0x00000000);
            }

            i++;
        }

        if (i == 0) {
            g.drawString(this.font,
                    Component.translatable("screen.spiritworld.loadout.empty"),
                    listX, LIST_BASE_Y, 0xFF888888, false);
        }
    }

    private void renderPassiveToggles(GuiGraphics g, int mouseX, int mouseY) {
        int listX = 20;
        int listY = this.height - 180;

        g.drawString(this.font,
                Component.translatable("screen.spiritworld.loadout.passives"),
                listX, listY - 12, 0xFFFFD54F, false);

        int y = listY;
        for (SkillNode node : SkillTree.getAll()) {
            if (node.getType() != SkillType.PASSIVE) continue;
            if (ClientSpiritData.getSkillLevel(node.getId()) <= 0) continue;

            String name = Component.translatable(node.getNameKey()).getString();
            boolean active = ClientSpiritData.isPassiveActive(node.getId());
            String status;
            int color;
            if (node.isAlwaysOn()) {
                status = "[" + Component.translatable("screen.spiritworld.loadout.passive.always").getString() + "]";
                color = 0xFF888888;
            } else if (!node.isToggleable()) {
                status = "[-]";
                color = 0xFF888888;
            } else {
                status = active
                        ? "[" + Component.translatable("screen.spiritworld.loadout.passive.on").getString() + "]"
                        : "[" + Component.translatable("screen.spiritworld.loadout.passive.off").getString() + "]";
                color = active ? 0xFF88FF88 : 0xFFFF8888;
            }

            g.drawString(this.font, "● " + name, listX, y, 0xFFFFFFFF, false);
            g.drawString(this.font, status, listX + 120, y, color, false);
            y += 12;
        }
    }

    // ==================== 命中判定 ====================

    /** 返回鼠标命中的技能节点，未命中返回 null。render 与 mouseClicked 共用同一套坐标逻辑。 */
    private SkillNode getSkillNodeAt(double mouseX, double mouseY) {
        int listX = this.width / 2 + LIST_BASE_OFFSET_X;

        int i = 0;
        for (SkillNode node : SkillTree.getAll()) {
            if (ClientSpiritData.getSkillLevel(node.getId()) <= 0) continue;
            if (filterType != null && node.getType() != filterType) continue;
            if (onlyEquippable && !node.isEquippable()) continue;

            int col = i % LIST_PER_ROW;
            int row = i / LIST_PER_ROW;
            int cx = listX + col * LIST_CELL + LIST_CELL / 2;
            int cy = LIST_BASE_Y + row * LIST_CELL + LIST_CELL / 2;

            if (Math.abs(mouseX - cx) <= LIST_ICON_HALF && Math.abs(mouseY - cy) <= LIST_ICON_HALF) {
                return node;
            }
            i++;
        }
        return null;
    }

    // ==================== 交互 ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ① Widget（分类按钮 + 切换按钮）最优先
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int mx = (int) mouseX, my = (int) mouseY;

        // ② 槽位（圆形判定）
        for (int i = 0; i < SLOT_COUNT; i++) {
            int dx = mx - slotX[i], dy = my - slotY[i];
            if (dx * dx + dy * dy <= SLOT_RADIUS * SLOT_RADIUS) {
                if (button == 0) {
                    PacketDistributor.sendToServer(new ServerboundSetSelectedSlotPacket(i));
                } else if (button == 1) {
                    PacketDistributor.sendToServer(new ServerboundSetLoadoutPacket(i, null));
                }
                return true;
            }
        }

        // ③ 技能列表：左键装备到当前选中槽位（← 关键修复）
        SkillNode hit = getSkillNodeAt(mouseX, mouseY);
        if (hit != null) {
            if (button == 0 && hit.isEquippable()) {
                int slot = ClientSpiritData.getSelectedSlot();
                PacketDistributor.sendToServer(new ServerboundSetLoadoutPacket(slot, hit.getId()));
            }
            return true;
        }

        // ④ 被动开关
        int listX = 20;
        int listY = this.height - 180;
        int y = listY;
        for (SkillNode node : SkillTree.getAll()) {
            if (node.getType() != SkillType.PASSIVE) continue;
            if (ClientSpiritData.getSkillLevel(node.getId()) <= 0) continue;
            if (!node.isToggleable() || node.isAlwaysOn()) { y += 12; continue; }

            if (my >= y && my < y + 12 && mx >= listX + 120 && mx < listX + 180) {
                boolean active = ClientSpiritData.isPassiveActive(node.getId());
                PacketDistributor.sendToServer(new ServerboundTogglePassivePacket(node.getId(), !active));
                return true;
            }
            y += 12;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 空实现：render 里已用 fillGradient 画了自定义背景，避免原版半透明黑 + 模糊覆盖
    }
}