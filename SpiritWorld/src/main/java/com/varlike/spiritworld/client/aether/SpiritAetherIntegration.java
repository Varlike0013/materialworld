package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillType;

import java.util.ArrayList;
import java.util.List;

/** 统一注册 / 注销 Spirit World 对 AetherUI 的扩展。 */
public final class SpiritAetherIntegration {

    private static final List<BuffIcon> PASSIVE_ICONS = new ArrayList<>();
    private static boolean initialized = false;

    private SpiritAetherIntegration() {}

    public static void init() {
        if (initialized) return;

        AetherUI.registerHudBar(new SpiritHudBar());
        AetherUI.registerBlockInfoProvider(new SpiritBlockInfoProvider());

        // 被动图标的可见性随时间变化，节点列表本身是静态的，注册一次即可。
        for (SkillNode node : SkillTree.getHud()) {
            if (node.getType() != SkillType.PASSIVE) continue;
            BuffIcon icon = new SpiritPassiveIcon(node);
            AetherUI.registerBuffIcon(icon);
            PASSIVE_ICONS.add(icon);
        }

        initialized = true;
    }

    public static void destroy() {
        if (!initialized) return;

        AetherUI.unregisterHudBar(SpiritHudBar.ID);
        AetherUI.unregisterBlockInfoProvider(SpiritBlockInfoProvider.ID);
        for (BuffIcon icon : PASSIVE_ICONS) {
            AetherUI.unregisterBuffIcon(icon);
        }
        PASSIVE_ICONS.clear();

        initialized = false;
    }
}
