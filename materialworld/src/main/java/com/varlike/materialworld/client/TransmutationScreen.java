package com.varlike.materialworld.client;

import com.varlike.materialworld.emc.EMCManager;
import com.varlike.materialworld.network.NetworkHandler;
import com.varlike.materialworld.network.ServerboundRecyclePacket;
import com.varlike.materialworld.network.ServerboundRequestSyncPacket;
import com.varlike.materialworld.network.ServerboundTransmutePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TransmutationScreen extends Screen {

    private static final int CELL = 18;
    private static final int COLS = 9;
    private static final int EMC_ROWS = 5;          // 上方 EMC 网格行数
    private static final int EMC_GRID_HEIGHT = EMC_ROWS * CELL;

    private static final int INV_MAIN_ROWS = 3;     // 主背包 3 行
    private static final int INV_TOTAL_ROWS = 4;    // 含热键栏共 4 行
    private static final int INV_HEIGHT = INV_TOTAL_ROWS * CELL;

    private final List<ItemStack> allItems = new ArrayList<>();
    private int scrollRow = 0;

    // 布局位置
    private int gridX, gridY;      // EMC 网格
    private int invX, invY;        // 背包区

    public TransmutationScreen() {
        super(Component.literal("物质转换"));
    }

    @Override
    protected void init() {
        super.init();
        NetworkHandler.CHANNEL.sendToServer(new ServerboundRequestSyncPacket());

        allItems.clear();
        List<Item> items = new ArrayList<>();
        EMCManager.forEach((item, emc) -> items.add(item));
        items.sort(Comparator.comparingLong(EMCManager::getEMC));
        for (Item item : items) allItems.add(new ItemStack(item));

        int totalHeight = EMC_GRID_HEIGHT + 10 + INV_HEIGHT;
        int startY = (this.height - totalHeight) / 2 - 10;

        gridX = (this.width - COLS * CELL) / 2;
        gridY = startY;

        invX = gridX;
        invY = gridY + EMC_GRID_HEIGHT + 10;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // 标题和余额
        graphics.drawCenteredString(this.font, this.title, this.width / 2, gridY - 40, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                "§6EMC: §a" + ClientEMCData.getBalance(),
                this.width / 2, gridY - 27, 0xFFFFFF);

        // ==== EMC 网格 ====
        graphics.fill(gridX - 3, gridY - 3, gridX + COLS * CELL + 3, gridY + EMC_GRID_HEIGHT + 3, 0xFF303030);
        graphics.fill(gridX - 2, gridY - 2, gridX + COLS * CELL + 2, gridY + EMC_GRID_HEIGHT + 2, 0xFF808080);

        int startIndex = scrollRow * COLS;
        for (int row = 0; row < EMC_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = startIndex + row * COLS + col;
                if (idx >= allItems.size()) break;
                int cx = gridX + col * CELL;
                int cy = gridY + row * CELL;

                graphics.fill(cx, cy, cx + CELL, cy + CELL, 0xFF8B8B8B);

                ItemStack stack = allItems.get(idx);
                graphics.renderItem(stack, cx + 1, cy + 1);

                if (!ClientEMCData.isLearned(stack.getItem())) {
                    graphics.fill(cx + 1, cy + 1, cx + 17, cy + 17, 0x80000000);
                }
                if (inside(mouseX, mouseY, cx, cy)) {
                    graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x60FFFFFF);
                }
            }
        }

        // ==== 玩家背包 ====
        graphics.fill(invX - 3, invY - 3, invX + COLS * CELL + 3, invY + INV_HEIGHT + 3, 0xFF303030);
        graphics.fill(invX - 2, invY - 2, invX + COLS * CELL + 2, invY + INV_HEIGHT + 2, 0xFF808080);

        Inventory inv = Minecraft.getInstance().player.getInventory();

        // 主背包 3 行：inventory 索引 9..35
        for (int row = 0; row < INV_MAIN_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int invIndex = 9 + row * COLS + col;
                int cx = invX + col * CELL;
                int cy = invY + row * CELL;
                drawInvCell(graphics, inv, invIndex, cx, cy, mouseX, mouseY);
            }
        }

        // 热键栏 1 行：inventory 索引 0..8，下移 4 像素分隔
        int hotbarY = invY + INV_MAIN_ROWS * CELL + 4;
        for (int col = 0; col < COLS; col++) {
            int invIndex = col;
            int cx = invX + col * CELL;
            drawInvCell(graphics, inv, invIndex, cx, hotbarY, mouseX, mouseY);
        }

        // ==== Tooltip ====
        int hoveredGridIdx = getHoveredGridIndex(mouseX, mouseY);
        if (hoveredGridIdx >= 0) {
            renderGridTooltip(graphics, hoveredGridIdx, mouseX, mouseY);
        }

        int hoveredInv = getHoveredInvIndex(mouseX, mouseY);
        if (hoveredInv >= 0) {
            ItemStack stack = inv.getItem(hoveredInv);
            if (!stack.isEmpty()) {
                long unit = EMCManager.getEMC(stack.getItem());
                List<Component> tip = new ArrayList<>();
                tip.add(stack.getHoverName());
                if (unit > 0) {
                    tip.add(Component.literal("§6EMC: §a" + (unit * stack.getCount())
                            + " §7(§a" + unit + " §7x " + stack.getCount() + ")"));
                    tip.add(Component.literal("§7左键点击回收整堆"));
                    if (!ClientEMCData.isLearned(stack.getItem())) {
                        tip.add(Component.literal("§e回收时自动解锁"));
                    }
                } else {
                    tip.add(Component.literal("§c该物品无 EMC 值，无法回收"));
                }
                graphics.renderTooltip(this.font, tip, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void drawInvCell(GuiGraphics graphics, Inventory inv, int invIndex,
                             int cx, int cy, int mouseX, int mouseY) {
        graphics.fill(cx, cy, cx + CELL, cy + CELL, 0xFF8B8B8B);
        ItemStack stack = inv.getItem(invIndex);
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, cx + 1, cy + 1);
            graphics.renderItemDecorations(this.font, stack, cx + 1, cy + 1);
        }
        if (inside(mouseX, mouseY, cx, cy)) {
            graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x60FFFFFF);
        }
    }

    private void renderGridTooltip(GuiGraphics graphics, int idx, int mouseX, int mouseY) {
        ItemStack stack = allItems.get(idx);
        Item item = stack.getItem();
        long emc = EMCManager.getEMC(item);
        List<Component> tip = new ArrayList<>();
        tip.add(stack.getHoverName());
        tip.add(Component.literal("§6EMC: §a" + emc));
        if (!ClientEMCData.isLearned(item)) {
            tip.add(Component.literal("§c尚未学会（回收一个即可解锁）"));
        } else if (ClientEMCData.getBalance() < emc) {
            tip.add(Component.literal("§cEMC 不足"));
        } else {
            tip.add(Component.literal("§7左键点击提取一个"));
        }
        graphics.renderTooltip(this.font, tip, Optional.empty(), mouseX, mouseY);
    }

    private boolean inside(int mx, int my, int x, int y) {
        return mx >= x && mx < x + CELL && my >= y && my < y + CELL;
    }

    private int getHoveredGridIndex(int mx, int my) {
        if (mx < gridX || mx >= gridX + COLS * CELL) return -1;
        if (my < gridY || my >= gridY + EMC_GRID_HEIGHT) return -1;
        int col = (mx - gridX) / CELL;
        int row = (my - gridY) / CELL;
        int idx = scrollRow * COLS + row * COLS + col;
        return idx < allItems.size() ? idx : -1;
    }

    /** 返回玩家背包索引 (0..35)，未命中返回 -1 */
    private int getHoveredInvIndex(int mx, int my) {
        if (mx < invX || mx >= invX + COLS * CELL) return -1;

        // 主背包
        if (my >= invY && my < invY + INV_MAIN_ROWS * CELL) {
            int col = (mx - invX) / CELL;
            int row = (my - invY) / CELL;
            return 9 + row * COLS + col;
        }
        // 热键栏
        int hotbarY = invY + INV_MAIN_ROWS * CELL + 4;
        if (my >= hotbarY && my < hotbarY + CELL) {
            int col = (mx - invX) / CELL;
            return col;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // 优先处理 EMC 网格点击
        int gridIdx = getHoveredGridIndex((int) mouseX, (int) mouseY);
        if (gridIdx >= 0) {
            Item item = allItems.get(gridIdx).getItem();
            if (ClientEMCData.isLearned(item) && ClientEMCData.getBalance() >= EMCManager.getEMC(item)) {
                NetworkHandler.CHANNEL.sendToServer(new ServerboundTransmutePacket(item));
            }
            return true;
        }

        // 再处理背包点击（回收）
        int invIdx = getHoveredInvIndex((int) mouseX, (int) mouseY);
        if (invIdx >= 0) {
            ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(invIdx);
            if (!stack.isEmpty() && EMCManager.getEMC(stack.getItem()) > 0) {
                NetworkHandler.CHANNEL.sendToServer(new ServerboundRecyclePacket(invIdx));
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int totalRows = (allItems.size() + COLS - 1) / COLS;
        int maxScroll = Math.max(0, totalRows - EMC_ROWS);
        scrollRow = Math.max(0, Math.min(maxScroll, scrollRow - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}