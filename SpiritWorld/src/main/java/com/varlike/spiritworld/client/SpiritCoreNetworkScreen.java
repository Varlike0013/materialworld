package com.varlike.spiritworld.client;

import com.varlike.spiritworld.network.ClientboundNetworkTreePacket;
import com.varlike.spiritworld.network.OfflineCollectorData;
import com.varlike.spiritworld.network.ServerboundRequestNetworkDataPacket;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 魔力网络拓扑与诊断界面：展示段效率、断链原因和离线影子收益。 */
public class SpiritCoreNetworkScreen extends Screen {

    private record Row(ClientboundNetworkTreePacket.NodeData node, int y, int depth) {}

    private final Screen parent;
    private final BlockPos corePos;

    private List<ClientboundNetworkTreePacket.NodeData> nodes = new ArrayList<>();
    private List<ClientboundNetworkTreePacket.PullDetail> details = new ArrayList<>();
    private ClientboundNetworkTreePacket.NodeData selectedNode = null;
    private final Set<BlockPos> collapsedNodes = new HashSet<>();
    private final List<Row> visibleRows = new ArrayList<>();
    private int scrollOffset;
    private int maxScroll;
    private int refreshTimer;

    public SpiritCoreNetworkScreen(Screen parent, BlockPos pos) {
        super(Component.translatable("gui.spiritworld.core.network_title"));
        this.parent = parent;
        this.corePos = pos;
    }

    /** 由 ClientboundNetworkTreePacket 的 handle 调用更新数据。 */
    public void updateData(List<ClientboundNetworkTreePacket.NodeData> newNodes,
                           List<ClientboundNetworkTreePacket.PullDetail> newDetails) {
        BlockPos selectedPos = selectedNode == null ? null : selectedNode.pos();
        this.nodes = newNodes;
        this.details = newDetails;
        this.selectedNode = null;

        if (selectedPos != null) {
            this.selectedNode = nodes.stream()
                    .filter(node -> node.pos().equals(selectedPos))
                    .findFirst().orElse(null);
        }
        if (this.selectedNode == null && !nodes.isEmpty()) {
            this.selectedNode = nodes.get(0);
        }
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new ServerboundRequestNetworkDataPacket(this.corePos));
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.spiritworld.core.back_to_craft"),
                        b -> Minecraft.getInstance().setScreen(this.parent))
                .bounds(this.width - 100, 20, 80, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        if (++refreshTimer >= 100) {
            refreshTimer = 0;
            PacketDistributor.sendToServer(new ServerboundRequestNetworkDataPacket(this.corePos));
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        renderTree(g, 20, 50);
        renderDetailPanel(g, this.width / 2 + 20, 50);
    }

    private void renderTree(GuiGraphics g, int x, int y) {
        visibleRows.clear();
        int rowHeight = 12;
        int viewBottom = this.height - 24;
        int contentHeight = nodes.size() * rowHeight;
        maxScroll = Math.max(0, contentHeight - (viewBottom - y));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int currentY = y - scrollOffset;
        for (ClientboundNetworkTreePacket.NodeData node : nodes) {
            if (isHiddenByCollapse(node)) {
                currentY += rowHeight;
                continue;
            }

            int depth = depthOf(node);
            if (currentY + rowHeight >= y && currentY <= viewBottom) {
                visibleRows.add(new Row(node, currentY, depth));
                drawNodeRow(g, x, currentY, depth, node);
            }
            currentY += rowHeight;
        }
    }

    private void drawNodeRow(GuiGraphics g, int x, int y, int depth, ClientboundNetworkTreePacket.NodeData node) {
        String indent = "  ".repeat(Math.min(depth, 8));
        boolean collapsed = collapsedNodes.contains(node.pos());
        Component name = Component.literal(indent + (collapsed ? "▶ " : "▼ ") + typeLabel(node).getString()
                + " " + node.pos().toShortString());

        boolean disconnected = isDisconnected(node);
        int color = node == selectedNode ? 0xFFFFD54F : (disconnected ? 0xFFFF8A80 : 0xFFFFFFFF);

        if (node == selectedNode) {
            g.fill(x - 3, y - 1, x + 190, y + 11, 0x40FFFFFF);
        }
        g.drawString(this.font, name, x + depth * 3, y, color, false);

        if (depth > 0) {
            Component detail = Component.literal("  S" + percent(node.segmentEfficiency())
                    + " / T" + percent(node.efficiency()));
            g.drawString(this.font, detail, x + 120, y, disconnected ? 0xFFFF8A80 : 0x8ED7FF, false);
        }
    }

    private void renderDetailPanel(GuiGraphics g, int x, int y) {
        if (selectedNode == null) return;

        g.fill(x - 5, y - 5, x + 190, y + 176, 0x90000000);
        int currentY = y;
        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.node_detail"), 0xFFFFD54F);
        currentY += 14;

        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.pos", selectedNode.pos().toShortString()), 0xFFFFFF);
        currentY += 12;

        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.parent",
                selectedNode.parentPos() == null
                        ? Component.translatable("gui.spiritworld.network.parent_none").getString()
                        : selectedNode.parentPos().toShortString()), 0xFFFFFF);
        currentY += 12;

        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.children", selectedNode.children()), 0xFFFFFF);
        currentY += 12;

        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.segment_efficiency",
                percent(selectedNode.segmentEfficiency())), 0x8ED7FF);
        currentY += 12;

        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.efficiency",
                percent(selectedNode.efficiency())), 0x33CCFF);
        currentY += 12;

        boolean disconnected = isDisconnected(selectedNode);
        Component status = disconnected
                ? Component.translatable("gui.spiritworld.network.status_disconnected")
                        .append(" - ").append(reasonText(selectedNode.linkReason()))
                : Component.translatable("gui.spiritworld.network.status_connected");
        drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.link_reason", status),
                disconnected ? 0xFFFF8A80 : 0x99FF99);
        currentY += 16;

        if (selectedNode.type().equals("CORE")) {
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.total_mana",
                    selectedNode.mana(), selectedNode.capacity()), 0x33CCFF);
            currentY += 14;
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.pull_details"), 0xFFFFD54F);
            currentY += 12;
            for (ClientboundNetworkTreePacket.PullDetail detail : details) {
                drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.pull_line",
                        detail.amount(), detail.type().id(), detail.collectorPos().toShortString()), 0xAAAAAA);
                currentY += 10;
            }
        } else if (selectedNode.type().equals("COLLECTOR")) {
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.stock",
                    selectedNode.mana(), selectedNode.capacity()), 0x33CCFF);
            currentY += 12;
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.type",
                    selectedNode.manaType().id()), 0x33CCFF);
            currentY += 12;
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.offline_gain",
                    selectedNode.offlineGain()), 0xFFD54F);
            currentY += 14;
            drawLine(g, x, currentY, Component.translatable("gui.spiritworld.network.offline_hint",
                    OfflineCollectorData.FULL_RATE_SECONDS / 60,
                    OfflineCollectorData.MAX_OFFLINE_SECONDS / 3600,
                    (int) (OfflineCollectorData.MIN_OFFLINE_FACTOR * 100)), 0xAAAAAA);
        }
    }

    private void drawLine(GuiGraphics g, int x, int y, Component text, int color) {
        g.drawString(this.font, text, x, y, color, false);
    }

    private boolean isDisconnected(ClientboundNetworkTreePacket.NodeData node) {
        return node.parentPos() == null || !"connected".equals(node.linkReason());
    }

    private ClientboundNetworkTreePacket.NodeData findNode(BlockPos pos) {
        for (ClientboundNetworkTreePacket.NodeData node : nodes) {
            if (node.pos().equals(pos)) return node;
        }
        return null;
    }

    private int depthOf(ClientboundNetworkTreePacket.NodeData node) {
        int depth = 0;
        Set<BlockPos> seen = new HashSet<>();
        BlockPos current = node.parentPos();
        while (current != null && depth < 12 && seen.add(current)) {
            ClientboundNetworkTreePacket.NodeData parent = findNode(current);
            if (parent == null) break;
            depth++;
            current = parent.parentPos();
        }
        return depth;
    }

    private boolean isHiddenByCollapse(ClientboundNetworkTreePacket.NodeData node) {
        Set<BlockPos> seen = new HashSet<>();
        BlockPos current = node.parentPos();
        while (current != null && seen.add(current)) {
            if (collapsedNodes.contains(current)) return true;
            ClientboundNetworkTreePacket.NodeData parent = findNode(current);
            if (parent == null) break;
            current = parent.parentPos();
        }
        return false;
    }

    private Component typeLabel(ClientboundNetworkTreePacket.NodeData node) {
        if (node.type().equals("CORE")) {
            return Component.translatable("gui.spiritworld.network.core_node");
        }
        if (node.type().startsWith("RELAY")) {
            int tier = node.type().endsWith("3") ? 3 : (node.type().endsWith("2") ? 2 : 1);
            return Component.translatable("gui.spiritworld.network.relay_node", tier);
        }
        return Component.translatable("gui.spiritworld.network.collector_node");
    }

    private Component reasonText(String reason) {
        String key = "gui.spiritworld.network.reason." + (reason == null ? "no_parent" : reason);
        return Component.translatable(key);
    }

    private static String percent(double value) {
        if (value < 0) return "--";
        return String.format("%.1f%%", value * 100.0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Row row : visibleRows) {
            if (mouseX >= 20 && mouseX <= 210 && mouseY >= row.y && mouseY <= row.y + 12) {
                if (button == 1 && row.node().children() > 0) {
                    if (!collapsedNodes.add(row.node().pos())) collapsedNodes.remove(row.node().pos());
                } else {
                    selectedNode = row.node();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) Math.signum(verticalAmount) * 24;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0xC0100810, 0xE0050208);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
