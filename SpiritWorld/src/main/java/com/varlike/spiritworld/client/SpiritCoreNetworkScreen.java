package com.varlike.spiritworld.client;

import com.varlike.spiritworld.network.ClientboundNetworkTreePacket;
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

public class SpiritCoreNetworkScreen extends Screen {
    private final Screen parent;
    private final BlockPos corePos;

    private List<ClientboundNetworkTreePacket.NodeData> nodes = new ArrayList<>();
    private List<ClientboundNetworkTreePacket.PullDetail> details = new ArrayList<>();
    private ClientboundNetworkTreePacket.NodeData selectedNode = null;
    private final Set<BlockPos> collapsedNodes = new HashSet<>();
    private int refreshTimer = 0;

    public SpiritCoreNetworkScreen(Screen parent, BlockPos pos) {
        super(Component.translatable("gui.spiritworld.core.network_title"));
        this.parent = parent;
        this.corePos = pos;
    }

    /** 由 ClientboundNetworkTreePacket 的 handle 调用更新数据 */
    public void updateData(List<ClientboundNetworkTreePacket.NodeData> newNodes, List<ClientboundNetworkTreePacket.PullDetail> newDetails) {
        this.nodes = newNodes;
        this.details = newDetails;
        if (selectedNode == null && !nodes.isEmpty()) {
            selectedNode = nodes.get(0); // 默认选中核心
        }
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new ServerboundRequestNetworkDataPacket(this.corePos));
        // 添加“返回合成”按钮
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.spiritworld.core.back_to_craft"),
                        b -> {
                            // 直接切回父 Screen（也就是之前的合成界面）
                            Minecraft.getInstance().setScreen(this.parent);
                        })
                .bounds(this.width - 100, 20, 80, 20)
                .build());
    }
    @Override
    public void tick() {
        super.tick();
        refreshTimer++;
        if (refreshTimer >= 100) { // 每 5 秒刷新一次
            refreshTimer = 0;
            PacketDistributor.sendToServer(new ServerboundRequestNetworkDataPacket(this.corePos));
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick); // 画按钮

        // 渲染左侧树
        renderTree(g, 20, 50, mouseX, mouseY);

        // 渲染右侧详情面板
        renderDetailPanel(g, this.width / 2 + 20, 50);
    }

    private void renderTree(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int currentY = y;
        for (ClientboundNetworkTreePacket.NodeData node : nodes) {
            int indent = node.parentPos() == null ? 0 : 15;
            String prefix = node.type().equals("CORE") ? "▼ [核心] " : "  ├─ [聚集器] ";

            Component name = Component.literal(prefix + node.pos().toShortString());
            int color = node == selectedNode ? 0xFFFFD54F : 0xFFFFFF;

            // 简单的选中高亮
            if (node == selectedNode) {
                g.fill(x + indent - 2, currentY - 1, x + 200, currentY + 9, 0x40FFFFFF);
            }

            g.drawString(this.font, name, x + indent, currentY, color, false);
            currentY += 12;
        }
    }

    private void renderDetailPanel(GuiGraphics g, int x, int y) {
        if (selectedNode == null) return;

        // 背景面板
        g.fill(x - 5, y - 5, x + 180, y + 150, 0x80000000);
        int currentY = y;
        // 标题
        g.drawString(this.font, Component.translatable("gui.spiritworld.network.node_detail"), x, currentY, 0xFFFFD54F, false);
        currentY += 14;
        // 坐标
        g.drawString(this.font, Component.translatable("gui.spiritworld.network.pos", selectedNode.pos().toShortString()), x, currentY, 0xFFFFFF, false);
        currentY += 12;
        // 区分核心和聚集器的数据
        if (selectedNode.type().equals("CORE")) {
            g.drawString(this.font, Component.translatable("gui.spiritworld.network.total_mana", selectedNode.mana(), selectedNode.capacity()), x, currentY, 0x33CCFF, false);
            currentY += 14;
            g.drawString(this.font, Component.translatable("gui.spiritworld.network.pull_details"), x, currentY, 0xFFFFD54F, false);
            currentY += 12;
            for (var detail : details) {
                Component text = Component.translatable("gui.spiritworld.network.pull_line", detail.amount(), detail.type().id(), detail.collectorPos().toShortString());
                g.drawString(this.font, text, x, currentY, 0xAAAAAA, false);
                currentY += 10;
            }
        } else {
            g.drawString(this.font, Component.translatable("gui.spiritworld.network.efficiency", String.format("%.1f", selectedNode.efficiency() * 100)), x, currentY, 0x33CCFF, false);
            currentY += 12;
            g.drawString(this.font, Component.translatable("gui.spiritworld.network.stock", selectedNode.mana(), selectedNode.capacity()), x, currentY, 0x33CCFF, false);
            currentY += 12;
            g.drawString(this.font, Component.translatable("gui.spiritworld.network.type", selectedNode.manaType().id()), x, currentY, 0x33CCFF, false);
            currentY += 12;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftX = 20;
        int topY = 50;
        int currentY = topY;
        for (ClientboundNetworkTreePacket.NodeData node : nodes) {
            if (mouseX >= leftX && mouseX <= leftX + 200 && mouseY >= currentY && mouseY <= currentY + 12) {
                selectedNode = node;
                return true;
            }
            currentY += 12;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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