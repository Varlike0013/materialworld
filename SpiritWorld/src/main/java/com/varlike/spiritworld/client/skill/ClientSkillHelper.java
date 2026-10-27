package com.varlike.spiritworld.client.skill;

import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.skill.ModSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * 客户端技能界面辅助方法。
 * 统一「打开某个技能的配置界面」入口，避免多处硬编码。
 */
public final class ClientSkillHelper {

    private ClientSkillHelper() {}

    /**
     * 打开精神扫描配置界面。
     * 未学技能 / 已在配置界面 / 无玩家时静默忽略。
     *
     * @param parent 关闭配置界面时返回的目标界面
     */
    public static void openScanConfig(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (ClientSpiritData.getSkillLevel(ModSkills.SPIRIT_SCAN_ID) <= 0) return;
        if (mc.screen instanceof ScanConfigScreen) return;   // 防止重复打开
        mc.setScreen(new ScanConfigScreen(parent));
    }
}