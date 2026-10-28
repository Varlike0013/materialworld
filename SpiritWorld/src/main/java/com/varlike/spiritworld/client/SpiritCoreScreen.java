package com.varlike.spiritworld.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.varlike.spiritworld.block.SpiritCoreBlockEntity;
import com.varlike.spiritworld.skill.ManaCraftingRecipe;
import com.varlike.spiritworld.skill.ManaCraftingRecipes;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpiritCoreScreen extends AbstractContainerScreen<SpiritCoreMenu> {

    private static final int BG_WIDTH = 280;
    private static final int BG_HEIGHT = 300;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 20;
    private int scrollRow = 0;                        // 当前首行（0 起始）
    private static final int VISIBLE_ROWS = GRID_ROWS;
    private Button startCancelButton;
    private final List<Button> quantityButtons = new ArrayList<>();

    public SpiritCoreScreen(SpiritCoreMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
        this.inventoryLabelY = BG_HEIGHT - 94;
    }

    @Override
    protected void init() {
        super.init();

        quantityButtons.clear();

        int x = leftPos;
        int y = topPos;

        // ===== 数量按钮行（6 个）=====
        int bw = 40, bh = 16, sp = 2;
        int totalW = bw * 6 + sp * 5;
        int startX = x + (BG_WIDTH - totalW) / 2;
        int by = y + 145;

        Button minBtn    = makeQtyButton("MIN", startX,                by, bw, bh, SpiritCoreBlockEntity.BTN_MIN);
        Button minus5Btn = makeQtyButton("-5",  startX + (bw + sp),   by, bw, bh, SpiritCoreBlockEntity.BTN_MINUS5);
        Button minus1Btn = makeQtyButton("-1",  startX + (bw + sp)*2, by, bw, bh, SpiritCoreBlockEntity.BTN_MINUS1);
        Button plus1Btn  = makeQtyButton("+1",  startX + (bw + sp)*3, by, bw, bh, SpiritCoreBlockEntity.BTN_PLUS1);
        Button plus5Btn  = makeQtyButton("+5",  startX + (bw + sp)*4, by, bw, bh, SpiritCoreBlockEntity.BTN_PLUS5);
        Button maxBtn    = makeQtyButton("MAX", startX + (bw + sp)*5, by, bw, bh, SpiritCoreBlockEntity.BTN_MAX);

        addRenderableWidget(minBtn);
        addRenderableWidget(minus5Btn);
        addRenderableWidget(minus1Btn);
        addRenderableWidget(plus1Btn);
        addRenderableWidget(plus5Btn);
        addRenderableWidget(maxBtn);

        quantityButtons.add(minBtn);
        quantityButtons.add(minus5Btn);
        quantityButtons.add(minus1Btn);
        quantityButtons.add(plus1Btn);
        quantityButtons.add(plus5Btn);
        quantityButtons.add(maxBtn);

        // ===== 开始 / 取消 =====
        startCancelButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.spiritworld.forge.start"),
                        b -> {
                            if (menu.isBatchActive()) sendButton(SpiritCoreBlockEntity.BTN_CANCEL);
                            else sendButton(SpiritCoreBlockEntity.BTN_START);
                        })
                .bounds(x + (BG_WIDTH - 80) / 2, y + 178, 80, 18)
                .build());
    }

    private Button makeQtyButton(String label, int x, int y, int w, int h, int action) {
        return Button.builder(Component.literal(label), b -> sendButton(action))
                .bounds(x, y, w, h)
                .build();
    }

    private void sendButton(int index) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - BG_WIDTH) / 2;
        int y = (height - BG_HEIGHT) / 2;

        // 半透明背景
        g.fill(x, y, x + BG_WIDTH, y + BG_HEIGHT, 0xE0201030);
        // 边框
        g.fill(x, y, x + BG_WIDTH, y + 1, 0xFF7E57C2);
        g.fill(x, y + BG_HEIGHT - 1, x + BG_WIDTH, y + BG_HEIGHT, 0xFF7E57C2);
        g.fill(x, y, x + 1, y + BG_HEIGHT, 0xFF7E57C2);
        g.fill(x + BG_WIDTH - 1, y, x + BG_WIDTH, y + BG_HEIGHT, 0xFF7E57C2);

        // 左侧配方网格
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int sx = x + GRID_X + col * SLOT_SIZE;
                int sy = y + GRID_Y + row * SLOT_SIZE;
                g.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF303030);
                g.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFF1A1A1A);
            }
        }

        // 中间详情区域背景
        int detailX = x + 88;
        int detailY = y + 20;
        g.fill(detailX, detailY, detailX + 84, detailY + 130, 0x80000000);

        // 右侧容器槽位
        int rightX = x + 180;
        int rightY = y + 20;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = rightX + col * SLOT_SIZE;
                int sy = rightY + row * SLOT_SIZE;
                g.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF303030);
                g.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFF1A1A1A);
            }
        }

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 60 + col * SLOT_SIZE - 1;
                int sy = y + 215 + row * SLOT_SIZE - 1;
                g.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF303030);
                g.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFF1A1A1A);
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            int sx = x + 60 + col * SLOT_SIZE - 1;
            int sy = y + 273 - 1;
            g.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF303030);
            g.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFF1A1A1A);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean active = menu.isBatchActive();

        // 数量按钮：炼成中禁用
        for (var btn : quantityButtons) {
            btn.active = !active && menu.getSelectedIndex() >= 0;
        }

        // 开始/取消按钮
        if (startCancelButton != null) {
            if (active) {
                startCancelButton.active = true;
                startCancelButton.setMessage(Component.translatable("gui.spiritworld.forge.cancel"));
            } else {
                boolean canStart = menu.getSelectedIndex() >= 0
                        && (menu.isInfiniteMode() || menu.getTargetCount() > menu.getCompletedCount());
                startCancelButton.active = canStart;
                startCancelButton.setMessage(Component.translatable("gui.spiritworld.forge.start"));
            }
        }

        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        renderRecipeGrid(g, leftPos, topPos, mouseX, mouseY);
        renderDetail(g, leftPos, topPos);
        renderTargetLine(g, leftPos, topPos);
        renderTooltip(g, mouseX, mouseY);
    }
    private void renderTargetLine(GuiGraphics g, int ox, int oy) {
        if (menu.getSelectedIndex() < 0) return;

        Component line;
        if (menu.isInfiniteMode()) {
            line = Component.translatable("gui.spiritworld.forge.target.infinite",
                    menu.getCompletedCount());
        } else {
            line = Component.translatable("gui.spiritworld.forge.target",
                    menu.getTargetCount(), menu.getCompletedCount());
        }
        g.drawCenteredString(font, line, ox + BG_WIDTH / 2, oy + 167, 0xFFFFD54F);
    }

    private void renderRecipeGrid(GuiGraphics g, int ox, int oy, int mouseX, int mouseY) {
        int total = ManaCraftingRecipes.size();
        int totalRows = (total + GRID_COLS - 1) / GRID_COLS;
        int startIdx = scrollRow * GRID_COLS;
        int endIdx = Math.min(total, startIdx + GRID_COLS * VISIBLE_ROWS);
        int hovered = getHoveredGrid(mouseX, mouseY, ox, oy);

        for (int i = startIdx; i < endIdx; i++) {
            int visualIdx = i - startIdx;
            int col = visualIdx % GRID_COLS;
            int row = visualIdx / GRID_COLS;
            int sx = ox + GRID_X + col * SLOT_SIZE + 1;
            int sy = oy + GRID_Y + row * SLOT_SIZE + 1;

            ManaCraftingRecipe recipe = ManaCraftingRecipes.get(i);
            g.renderItem(recipe.output(), sx, sy);
            g.renderItemDecorations(font, recipe.output(), sx, sy);

            if (menu.getSelectedIndex() == i) {
                g.fill(ox + GRID_X + col * SLOT_SIZE, oy + GRID_Y + row * SLOT_SIZE,
                        ox + GRID_X + col * SLOT_SIZE + SLOT_SIZE,
                        oy + GRID_Y + row * SLOT_SIZE + SLOT_SIZE, 0x80FFD54F);
            }
            if (hovered == i) {
                g.fill(ox + GRID_X + col * SLOT_SIZE, oy + GRID_Y + row * SLOT_SIZE,
                        ox + GRID_X + col * SLOT_SIZE + SLOT_SIZE,
                        oy + GRID_Y + row * SLOT_SIZE + SLOT_SIZE, 0x40FFFFFF);
            }
        }

        // 滚动条（仅当内容超出时显示）
        if (totalRows > VISIBLE_ROWS) {
            int trackX = ox + GRID_X + GRID_COLS * SLOT_SIZE + 1;
            int trackY = oy + GRID_Y;
            int trackH = VISIBLE_ROWS * SLOT_SIZE;
            g.fill(trackX, trackY, trackX + 2, trackY + trackH, 0xFF303030);

            int thumbH = Math.max(10, trackH * VISIBLE_ROWS / totalRows);
            int scrollableH = trackH - thumbH;
            int thumbY = trackY + (scrollableH * scrollRow / (totalRows - VISIBLE_ROWS));
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private void renderDetail(GuiGraphics g, int ox, int oy) {
        int dx = ox + 88 + 4;
        int dy = oy + 24;
        int lineH = 10;

        ManaCraftingRecipe recipe = menu.getSelectedIndex() >= 0
                ? ManaCraftingRecipes.get(menu.getSelectedIndex()) : null;

        if (recipe == null) {
            g.drawString(font, Component.translatable("gui.spiritworld.spirit_forge.select"),
                    dx, dy, 0xFF888888, false);
            return;
        }

        // 产出
        g.drawString(font, recipe.output().getHoverName(), dx, dy, 0xFFFFD54F, false);
        g.drawString(font, Component.literal("x" + recipe.output().getCount()),
                dx, dy + lineH, 0xFFFFD54F, false);
        dy += lineH * 2 + 2;

        // 魔力消耗
        for (Map.Entry<ManaType, Long> e : recipe.manaCost().entrySet()) {
            Component line = e.getKey().applyStyle(
                    Component.translatable("gui.spiritworld.spirit_forge.cost",
                            e.getKey().displayName(), e.getValue()));
            g.drawString(font, line, dx, dy, 0xFFFFFFFF, false);
            dy += lineH;
        }

        // 时间
        g.drawString(font, Component.translatable("gui.spiritworld.spirit_forge.time",
                recipe.craftingTicks() / 20), dx, dy, 0xFFFFFFFF, false);
        dy += lineH + 4;

        // 进度条
        int barW = 76;
        int barH = 6;
        int total = menu.getTotalTicks();
        int cur = menu.getProgress();
        if (menu.getSelectedIndex() == menu.getActiveRecipeIndex() || !menu.isBatchActive()) {
            // 画进度条
            g.fill(dx, dy, dx + barW, dy + barH, 0xFF303030);
            if (total > 0) {
                float ratio = Math.min(1f, (float) cur / total);
                g.fill(dx, dy, dx + (int)(barW * ratio), dy + barH, 0xFF66BB6A);
            }
        }
        int activeIdx = menu.getActiveRecipeIndex();
        if (menu.isBatchActive() && activeIdx >= 0 && activeIdx != menu.getSelectedIndex()) {
            ManaCraftingRecipe activeRecipe = ManaCraftingRecipes.get(activeIdx);
            if (activeRecipe != null) {
                Component line = Component.translatable("gui.spiritworld.forge.crafting_other",
                        activeRecipe.output().getHoverName());
                g.drawString(font, line, ox + 92, oy + 130, 0xFFFFD54F, false);
            }
        }
    }

    private void renderContainerIcon(GuiGraphics g, int ox, int oy) {
        // 可选的装饰
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        int ox = (width - BG_WIDTH) / 2;
        int oy = (height - BG_HEIGHT) / 2;
        int hovered = getHoveredGrid(mouseX, mouseY, ox, oy);
        if (hovered >= 0 && hovered < ManaCraftingRecipes.size()) {
            ManaCraftingRecipe recipe = ManaCraftingRecipes.get(hovered);
            List<Component> lines = new ArrayList<>();
            lines.add(recipe.output().getHoverName().copy().withStyle(ChatFormatting.GOLD));
            lines.add(Component.literal("x" + recipe.output().getCount()).withStyle(ChatFormatting.WHITE));
            for (Map.Entry<ManaType, Long> e : recipe.manaCost().entrySet()) {
                lines.add(e.getKey().applyStyle(
                        Component.translatable("gui.spiritworld.spirit_forge.cost",
                                e.getKey().displayName(), e.getValue())));
            }
            lines.add(Component.translatable("gui.spiritworld.spirit_forge.time",
                    recipe.craftingTicks() / 20).withStyle(ChatFormatting.GRAY));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int ox = (width - BG_WIDTH) / 2;
        int oy = (height - BG_HEIGHT) / 2;
        int idx = getHoveredGrid((int) mouseX, (int) mouseY, ox, oy);
        if (idx >= 0 && button == 0 && minecraft != null && minecraft.gameMode != null) {
            sendButton(idx);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalRows = (ManaCraftingRecipes.size() + GRID_COLS - 1) / GRID_COLS;
        int maxRow = Math.max(0, totalRows - VISIBLE_ROWS);
        if (maxRow == 0) return false;

        scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxRow);
        return true;
    }

    private int getHoveredGrid(int mx, int my, int ox, int oy) {
        int startIdx = scrollRow * GRID_COLS;
        for (int visualIdx = 0; visualIdx < GRID_COLS * VISIBLE_ROWS; visualIdx++) {
            int realIdx = startIdx + visualIdx;
            if (realIdx >= ManaCraftingRecipes.size()) break;

            int col = visualIdx % GRID_COLS;
            int row = visualIdx / GRID_COLS;
            int sx = ox + GRID_X + col * SLOT_SIZE;
            int sy = oy + GRID_Y + row * SLOT_SIZE;
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                return realIdx;
            }
        }
        return -1;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 空实现，避免原版模糊
    }
}