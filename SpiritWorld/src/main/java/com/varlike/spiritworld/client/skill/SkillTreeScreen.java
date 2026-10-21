package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.network.ServerboundLearnSkillPacket;
import com.varlike.spiritworld.network.ServerboundRequestSyncPacket;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeScreen extends Screen {

    private static final int CELL = 48;
    private static final int NODE_RADIUS = 16;
    private static final int TOP_BAR_HEIGHT = 50;
    private static final int BOTTOM_BAR_HEIGHT = 30;

    private static final int COLOR_BG_TOP = 0xFF1A0F2E;
    private static final int COLOR_BG_BOTTOM = 0xFF0D0618;
    private static final int COLOR_BAR = 0xE02A1A4A;
    private static final int COLOR_BORDER = 0xFF7E57C2;
    private static final int COLOR_LINE_DONE    = 0xCC66BB6A;  // 绿色，前置满足
    private static final int COLOR_LINE_PARTIAL = 0xCCFFD54F;  // 金色，前置部分满足
    private static final int COLOR_LINE_LOCKED  = 0x80424242;  // 灰色，前置未学

    private static final int COLOR_BORDER_LEARNED = 0xFF4CAF50;
    private static final int COLOR_FILL_LEARNED = 0x991B5E20;
    private static final int COLOR_BORDER_LEARNABLE = 0xFFFFD54F;
    private static final int COLOR_FILL_LEARNABLE = 0x995D4037;
    private static final int COLOR_BORDER_LOCKED = 0xFF424242;
    private static final int COLOR_FILL_LOCKED = 0xB3212121;
    private static final int COLOR_BORDER_NOPOINTS = 0xFFC62828;
    private static final int COLOR_FILL_NOPOINTS = 0x994A148C;
    private static final int COLOR_BORDER_MAX = 0xFF42A5F5;
    private static final int COLOR_FILL_MAX = 0x990D47A1;


    private double offsetX = 0;
    private double offsetY = 0;
    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private double dragStartOffsetX, dragStartOffsetY;

    private int minGridX, maxGridX, minGridY, maxGridY;

    private int contentLeft, contentTop, contentRight, contentBottom;
    private Button btnSwitchToLoadout;

    /** 点击失败时的临时提示（底部居中显示约 2.5 秒） */
    private Component feedback = null;
    private long feedbackUntil = 0L;

    public SkillTreeScreen() {
        super(Component.translatable("screen.spiritworld.skill_tree.title"));
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new ServerboundRequestSyncPacket());

        minGridX = Integer.MAX_VALUE;
        maxGridX = Integer.MIN_VALUE;
        minGridY = Integer.MAX_VALUE;
        maxGridY = Integer.MIN_VALUE;
        for (SkillNode node : SkillTree.getAll()) {
            minGridX = Math.min(minGridX, node.getGridX());
            maxGridX = Math.max(maxGridX, node.getGridX());
            minGridY = Math.min(minGridY, node.getGridY());
            maxGridY = Math.max(maxGridY, node.getGridY());
        }

        contentLeft = 20;
        contentTop = TOP_BAR_HEIGHT + 10;
        contentRight = this.width - 20;
        contentBottom = this.height - BOTTOM_BAR_HEIGHT - 10;

        btnSwitchToLoadout = addRenderableWidget(Button.builder(
                        Component.translatable("screen.spiritworld.skill_tree.switch_to_loadout"),
                        b -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new SkillLoadoutScreen());
                            }
                        })
                .bounds(this.width - 100, 15, 80, 20).build());
        centerView();
    }

    private void centerView() {
        int gw = (maxGridX - minGridX + 1) * CELL;
        int gh = (maxGridY - minGridY + 1) * CELL;
        int vw = contentRight - contentLeft;
        int vh = contentBottom - contentTop;
        offsetX = -(gw - vw) / 2.0;
        offsetY = -(gh - vh) / 2.0;
        clampOffset();
    }

    private void clampOffset() {
        int gw = (maxGridX - minGridX + 1) * CELL;
        int gh = (maxGridY - minGridY + 1) * CELL;
        int vw = contentRight - contentLeft;
        int vh = contentBottom - contentTop;
        if (gw <= vw) offsetX = -(gw - vw) / 2.0;
        else offsetX = Math.max(vw - gw, Math.min(0, offsetX));
        if (gh <= vh) offsetY = -(gh - vh) / 2.0;
        else offsetY = Math.max(vh - gh, Math.min(0, offsetY));
    }

    private int gridToScreenX(int gx) {
        return (int) (contentLeft + (gx - minGridX) * CELL + offsetX);
    }

    private int gridToScreenY(int gy) {
        return (int) (contentTop + (gy - minGridY) * CELL + offsetY);
    }

    private int nodeCenterX(SkillNode n) { return gridToScreenX(n.getGridX()) + CELL / 2; }
    private int nodeCenterY(SkillNode n) { return gridToScreenY(n.getGridY()) + CELL / 2; }

    // ==================== 状态判定 ====================

    private enum NodeState { LEARNED, LEARNABLE, LOCKED, NO_POINTS, MAX }

    private NodeState getState(SkillNode node) {
        int cur = ClientSpiritData.getSkillLevel(node.getId());
        if (cur >= node.getMaxLevel()) return NodeState.MAX;

        // 前置
        for (SkillNode.Prerequisite p : node.getPrerequisites()) {
            if (ClientSpiritData.getSkillLevel(p.id()) < p.minLevel()) return NodeState.LOCKED;
        }
        // 灵视等级
        if (ClientSpiritData.getVisionLevel() < node.getRequiredVisionLevel()) return NodeState.LOCKED;

        // 灵视点
        if (ClientSpiritData.getAvailableVisionPoints() < node.getCostForNextLevel(cur)) return NodeState.NO_POINTS;

        // 已学过但未满
        if (cur > 0) return NodeState.LEARNABLE; // 可继续加

        return NodeState.LEARNABLE;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderGradientBackground(g);

        g.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
        renderConnections(g);
        renderNodes(g, mouseX, mouseY);
        g.disableScissor();

        renderTopBar(g);

        // 新增：画 Widget（切换按钮）
        super.render(g, mouseX, mouseY, partialTick);

        renderFeedback(g);

        SkillNode hovered = getHoveredNode(mouseX, mouseY);
        if (hovered != null) renderTooltip(g, hovered, mouseX, mouseY);
    }

    // 4) 重写 renderBackground 为空，避免 super.render 触发原版遮罩
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 空实现
    }

    private void renderGradientBackground(GuiGraphics g) {
        g.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);
        // 边框
        g.fill(0, 0, this.width, 1, COLOR_BORDER);
        g.fill(0, this.height - 1, this.width, this.height, COLOR_BORDER);
        g.fill(0, 0, 1, this.height, COLOR_BORDER);
        g.fill(this.width - 1, 0, this.width, this.height, COLOR_BORDER);
    }

    private void renderConnections(GuiGraphics g) {
        for (SkillNode node : SkillTree.getAll()) {
            int x1 = nodeCenterX(node);
            int y1 = nodeCenterY(node);

            for (SkillNode.Prerequisite pre : node.getPrerequisites()) {
                SkillNode preNode = SkillTree.get(pre.id());
                if (preNode == null) continue;

                int x2 = nodeCenterX(preNode);
                int y2 = nodeCenterY(preNode);

                // 前置满足条件：前置技能等级 >= 要求的最低等级
                boolean satisfied = ClientSpiritData.getSkillLevel(pre.id()) >= pre.minLevel();

                // 三种颜色区分更细致
                int color;
                if (satisfied) {
                    // 前置已满足：绿色
                    color = COLOR_LINE_DONE;
                } else if (ClientSpiritData.getSkillLevel(pre.id()) > 0) {
                    // 前置已学但等级不够：金色（提示差几级）
                    color = COLOR_LINE_PARTIAL;
                } else {
                    // 前置完全未学：灰色
                    color = COLOR_LINE_LOCKED;
                }

                drawLine(g, x1, y1, x2, y2, color);
            }
        }
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // 缩短两端，避免覆盖节点
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;

        int shrink = NODE_RADIUS + 4;
        int sx1 = (int) (x1 + dx / len * shrink);
        int sy1 = (int) (y1 + dy / len * shrink);
        int sx2 = (int) (x2 - dx / len * shrink);
        int sy2 = (int) (y2 - dy / len * shrink);

        int steps = Math.max(Math.abs(sx2 - sx1), Math.abs(sy2 - sy1));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = sx1 + (sx2 - sx1) * i / steps;
            int y = sy1 + (sy2 - sy1) * i / steps;
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    private void renderNodes(GuiGraphics g, int mouseX, int mouseY) {
        float pulse = (float) (Math.sin(Util.getMillis() / 1000.0 * Math.PI * 2) * 0.25 + 0.55);
        int glowAlpha = (int) (pulse * 200);

        for (SkillNode node : SkillTree.getAll()) {
            int cx = nodeCenterX(node);
            int cy = nodeCenterY(node);

            NodeState state = getState(node);

            // 呼吸光晕（可学习状态）
            if (state == NodeState.LEARNABLE) {
                int glowColor = (glowAlpha << 24) | 0x00FFD54F;
                SkillRenderer.fillGlow(g, node.getType(), cx, cy, NODE_RADIUS + 4, glowColor);
            }

            int borderColor, fillColor;
            switch (state) {
                case LEARNED -> { borderColor = COLOR_BORDER_LEARNED; fillColor = COLOR_FILL_LEARNED; }
                case LEARNABLE -> { borderColor = COLOR_BORDER_LEARNABLE; fillColor = COLOR_FILL_LEARNABLE; }
                case LOCKED -> { borderColor = COLOR_BORDER_LOCKED; fillColor = COLOR_FILL_LOCKED; }
                case NO_POINTS -> { borderColor = COLOR_BORDER_NOPOINTS; fillColor = COLOR_FILL_NOPOINTS; }
                case MAX -> { borderColor = COLOR_BORDER_MAX; fillColor = COLOR_FILL_MAX; }
                default -> { borderColor = COLOR_BORDER_LOCKED; fillColor = COLOR_FILL_LOCKED; }
            }

            SkillRenderer.fillShape(g, node.getType(), cx, cy, NODE_RADIUS, borderColor, fillColor);

            // 图标字符
            String icon = node.getIcon();
            int iconColor = state == NodeState.LOCKED ? 0xFF888888 : 0xFFFFFFFF;
            g.drawCenteredString(this.font, icon, cx, cy - 4, iconColor);

            // 等级显示
            int cur = ClientSpiritData.getSkillLevel(node.getId());
            String lvlText = cur + "/" + node.getMaxLevel();
            g.drawCenteredString(this.font, lvlText, cx, cy + 6, 0xFFFFD54F);

            // 悬停高亮
            if (isInside(mouseX, mouseY, cx, cy, NODE_RADIUS)) {
                SkillRenderer.fillShape(g, node.getType(), cx, cy, NODE_RADIUS, 0x60FFFFFF, 0x00000000);
            }
        }
    }

    private void renderTopBar(GuiGraphics g) {
        g.fill(0, 0, this.width, TOP_BAR_HEIGHT, COLOR_BAR);
        g.fill(0, TOP_BAR_HEIGHT - 1, this.width, TOP_BAR_HEIGHT, COLOR_BORDER);

        int level = ClientSpiritData.getVisionLevel();
        long points = ClientSpiritData.getAvailableVisionPoints();
        long spent = ClientSpiritData.getTotalSpentVisionPoints();

        long curThreshold = (long) level * (level + 1) / 2 * 10;
        long nextThreshold = (long) (level + 1) * (level + 2) / 2 * 10;
        long toNext = nextThreshold - spent;

        g.drawString(this.font,
                Component.translatable("screen.spiritworld.skill_tree.level", level),
                20, 10, 0xFFFFD54F, false);
        g.drawString(this.font,
                Component.translatable("screen.spiritworld.skill_tree.points", points),
                20, 28, 0xFFAAAAFF, false);

        // 进度条
        int barLeft = this.width / 2 - 60;
        int barTop = 20;
        int barWidth = 120;
        int barHeight = 8;
        g.fill(barLeft - 1, barTop - 1, barLeft + barWidth + 1, barTop + barHeight + 1, 0xFF000000);
        g.fill(barLeft, barTop, barLeft + barWidth, barTop + barHeight, 0xFF303030);
        long range = nextThreshold - curThreshold;
        if (range > 0) {
            float ratio = (float) (spent - curThreshold) / range;
            int filled = (int) (barWidth * Math.max(0, Math.min(1, ratio)));
            g.fill(barLeft, barTop, barLeft + filled, barTop + barHeight, 0xFF9C27B0);
        }
        g.drawString(this.font,
                Component.translatable("screen.spiritworld.skill_tree.progress",
                        spent, nextThreshold),
                barLeft, barTop + 12, 0xFFFFFFFF, false);
    }

    private void renderTooltip(GuiGraphics g, SkillNode node, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(node.getNameKey()).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("skill.type." + node.getType().name().toLowerCase())
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.empty());
        lines.add(Component.translatable(node.getDescKey()).withStyle(ChatFormatting.WHITE));
        lines.add(Component.empty());

        int cur = ClientSpiritData.getSkillLevel(node.getId());
        lines.add(Component.translatable("skill.tooltip.level", cur, node.getMaxLevel())
                .withStyle(ChatFormatting.AQUA));

        if (cur < node.getMaxLevel()) {
            long nextCost = node.getCostForNextLevel(cur);
            lines.add(Component.translatable("skill.tooltip.cost", nextCost)
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (node.getRequiredVisionLevel() > 0) {
            boolean ok = ClientSpiritData.getVisionLevel() >= node.getRequiredVisionLevel();
            lines.add(Component.translatable("skill.tooltip.required_vision",
                            node.getRequiredVisionLevel())
                    .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        if (!node.getPrerequisites().isEmpty()) {
            lines.add(Component.translatable("skill.tooltip.prerequisites").withStyle(ChatFormatting.GRAY));
            for (SkillNode.Prerequisite p : node.getPrerequisites()) {
                SkillNode pn = SkillTree.get(p.id());
                if (pn == null) continue;
                int curLvl = ClientSpiritData.getSkillLevel(p.id());
                boolean ok = curLvl >= p.minLevel();
                String suffix = p.minLevel() > 1 ? " (Lv." + p.minLevel() + ")" : "";
                lines.add(Component.literal((ok ? " ✓ " : " ✗ "))
                        .append(Component.translatable(pn.getNameKey()))
                        .append(Component.literal(suffix))
                        .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }

        List<Component> extras = node.getExtraTooltip();
        if (!extras.isEmpty()) {
            lines.add(Component.empty());
            lines.addAll(extras);
        }

        g.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
    }

    // ==================== 学习反馈 ====================

    /** 本地预检，失败时给出可见提示，避免“点了没反应” */
    private void tryLearn(SkillNode node) {
        int cur = ClientSpiritData.getSkillLevel(node.getId());

        if (cur >= node.getMaxLevel()) {
            showFeedback(Component.translatable("message.spiritworld.skill.max_level"));
            return;
        }

        for (SkillNode.Prerequisite p : node.getPrerequisites()) {
            if (ClientSpiritData.getSkillLevel(p.id()) < p.minLevel()) {
                SkillNode pre = SkillTree.get(p.id());
                showFeedback(pre != null
                        ? Component.translatable("message.spiritworld.skill.prereq",
                                Component.translatable(pre.getNameKey()))
                        : Component.translatable("message.spiritworld.skill.prereq_unknown"));
                return;
            }
        }

        if (ClientSpiritData.getVisionLevel() < node.getRequiredVisionLevel()) {
            showFeedback(Component.translatable("message.spiritworld.skill.no_vision",
                    node.getRequiredVisionLevel()));
            return;
        }

        long cost = node.getCostForNextLevel(cur);
        if (ClientSpiritData.getAvailableVisionPoints() < cost) {
            showFeedback(Component.translatable("message.spiritworld.skill.no_points", cost));
            return;
        }

        PacketDistributor.sendToServer(new ServerboundLearnSkillPacket(node.getId()));
    }

    private void showFeedback(Component text) {
        this.feedback = text;
        this.feedbackUntil = Util.getMillis() + 2500L;
    }

    private void renderFeedback(GuiGraphics g) {
        if (feedback == null) return;
        if (Util.getMillis() >= feedbackUntil) {
            feedback = null;
            return;
        }
        int w = this.font.width(feedback);
        int x = (this.width - w) / 2;
        int y = this.height - BOTTOM_BAR_HEIGHT - 16;
        g.fill(x - 4, y - 3, x + w + 4, y + 10, 0xB0000000);
        g.drawString(this.font, feedback, x, y, 0xFFFF8A80, false);
    }

    private boolean isInside(int mx, int my, int cx, int cy, int r) {
        return Math.abs(mx - cx) <= r && Math.abs(my - cy) <= r;
    }

    private SkillNode getHoveredNode(int mx, int my) {
        if (mx < contentLeft || mx > contentRight) return null;
        if (my < contentTop || my > contentBottom) return null;
        for (SkillNode node : SkillTree.getAll()) {
            if (isInside(mx, my, nodeCenterX(node), nodeCenterY(node), NODE_RADIUS)) {
                return node;
            }
        }
        return null;
    }

    // ==================== 交互 ====================

    // 5) mouseClicked 开头加 Widget 处理（放在 button 判定之前）
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Widget 优先（切换按钮）
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button != 0) return false;

        SkillNode node = getHoveredNode((int) mouseX, (int) mouseY);
        if (node != null) {
            tryLearn(node);
            return true;
        }

        dragging = true;
        dragStartX = mouseX;
        dragStartY = mouseY;
        dragStartOffsetX = offsetX;
        dragStartOffsetY = offsetY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            offsetX = dragStartOffsetX + (mouseX - dragStartX);
            offsetY = dragStartOffsetY + (mouseY - dragStartY);
            clampOffset();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (hasShiftDown()) {
            offsetX += scrollY * CELL;
        } else {
            offsetY += scrollY * CELL;
        }
        clampOffset();
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}