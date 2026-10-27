package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.network.ServerboundSetScanConfigPacket;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 精神扫描配置界面。
 * 滑块调范围，勾选框选矿物。
 * 关闭时一次性发送 ServerboundSetScanConfigPacket。
 */
public class ScanConfigScreen extends Screen {

    private static final int COLOR_BG_TOP = 0xFF1A0F2E;
    private static final int COLOR_BG_BOTTOM = 0xFF0D0618;

    private final Screen parent;

    private int scanRange;
    private final Set<ResourceLocation> selectedOres = new HashSet<>();

    public ScanConfigScreen(Screen parent) {
        super(Component.translatable("screen.spiritworld.scan_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int level = ClientSpiritData.getSkillLevel(ModSkills.SPIRIT_SCAN_ID);
        int maxRange = SkillValues.scanRangeMax(level);

        // 初始化当前值（clamp 到本等级上限）
        this.scanRange = Math.max(SkillValues.SCAN_RANGE_MIN,
                Math.min(ClientSpiritData.getScanRange(), maxRange));
        this.selectedOres.clear();
        this.selectedOres.addAll(ClientSpiritData.getScanOres());

        // ===== 滑块 =====
        int sliderW = 220;
        int sliderX = (this.width - sliderW) / 2;
        int sliderY = 70;
        addRenderableWidget(new RangeSlider(sliderX, sliderY, sliderW, 20, maxRange));

        // ===== 勾选框（3 列 × N 行）=====
        List<ResourceLocation> all = SkillValues.ALL_SCAN_ORES;
        int cols = 3;
        int cbW = 110;
        int cbH = 20;
        int gapX = 10;
        int gapY = 4;
        int totalW = cols * cbW + (cols - 1) * gapX;
        int startX = (this.width - totalW) / 2;
        int startY = 130;

        for (int i = 0; i < all.size(); i++) {
            ResourceLocation oreId = all.get(i);
            boolean unlocked = level >= SkillValues.scanOreUnlockLevel(oreId);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (cbW + gapX);
            int y = startY + row * (cbH + gapY);
            addRenderableWidget(new OreCheckbox(x, y, cbW, cbH, oreId, unlocked));
        }

        // ===== 关闭按钮 =====
        int btnW = 80;
        int btnH = 20;
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        b -> this.onClose())
                .bounds((this.width - btnW) / 2, this.height - 40, btnW, btnH)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

        g.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFD54F);

        // 精神力消耗提示
        long cost = SkillValues.scanCostForRange(scanRange);
        g.drawCenteredString(this.font,
                Component.translatable("screen.spiritworld.scan_config.cost", cost),
                this.width / 2, 100, 0xFFAAAAFF);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 空实现，避免 super.render 触发原版半透明黑 + 模糊
    }

    @Override
    public void onClose() {
        // 关闭时一次性把配置发给服务端
        PacketDistributor.sendToServer(
                new ServerboundSetScanConfigPacket(scanRange, new HashSet<>(selectedOres)));

        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void toggleOre(ResourceLocation oreId) {
        if (!selectedOres.remove(oreId)) {
            selectedOres.add(oreId);
        }
    }

    private static String oreKey(ResourceLocation oreId) {
        String path = oreId.getPath();
        if (path.startsWith("scan/")) path = path.substring(5);
        return "spiritworld.scan." + path;
    }

    // ==================== 滑块 ====================

    private class RangeSlider extends AbstractSliderButton {

        private final int maxRange;

        RangeSlider(int x, int y, int w, int h, int maxRange) {
            super(x, y, w, h, Component.empty(),
                    (double) (scanRange - SkillValues.SCAN_RANGE_MIN)
                            / Math.max(1, maxRange - SkillValues.SCAN_RANGE_MIN));
            this.maxRange = maxRange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.spiritworld.scan_config.range", scanRange));
        }

        @Override
        protected void applyValue() {
            int min = SkillValues.SCAN_RANGE_MIN;
            int raw = min + (int) Math.round(this.value * (maxRange - min));
            int step = SkillValues.SCAN_RANGE_STEP;
            raw = ((raw - min) / step) * step + min;
            raw = Math.max(min, Math.min(raw, maxRange));
            scanRange = raw;
            updateMessage();
        }
    }

    // ==================== 勾选框 ====================

    private class OreCheckbox extends Button {

        private final ResourceLocation oreId;
        private final boolean unlocked;

        OreCheckbox(int x, int y, int w, int h, ResourceLocation oreId, boolean unlocked) {
            super(x, y, w, h, Component.empty(), b -> {}, DEFAULT_NARRATION);
            this.oreId = oreId;
            this.unlocked = unlocked;
            refreshLabel();
        }

        @Override
        public void onPress() {
            if (!unlocked) return;
            toggleOre(oreId);
            refreshLabel();
        }

        private void refreshLabel() {
            boolean sel = selectedOres.contains(oreId);
            String mark = !unlocked ? "🔒" : (sel ? "✓" : " ");
            Component txt = Component.literal("[" + mark + "] ")
                    .append(Component.translatable(oreKey(oreId)));
            setMessage(txt);
            this.active = unlocked;   // 锁定状态不可点击
        }
    }
}