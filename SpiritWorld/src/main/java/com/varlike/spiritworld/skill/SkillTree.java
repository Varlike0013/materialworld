package com.varlike.spiritworld.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class SkillTree {

    private static final Map<ResourceLocation, SkillNode> NODES = new LinkedHashMap<>();

    public static void register(SkillNode node) {
        NODES.put(node.getId(), node);
    }

    public static SkillNode get(ResourceLocation id) {
        return NODES.get(id);
    }

    /** 按技能 ID 归类；不修改既有技能定义即可获得稳定的分支展示。 */
    public static SkillBranch getBranch(SkillNode node) {
        if (node == null) return SkillBranch.SPIRIT;
        String path = node.getId().getPath();
        if (path.contains("pocket") || path.contains("flight")) return SkillBranch.SPACE;
        if (path.contains("tool") || path.contains("fortune") || path.contains("efficiency")
                || path.contains("repair") || path.contains("scan") || path.contains("maintenance")
                || path.contains("summon")) return SkillBranch.TOOL;
        if (path.contains("vision") || path.contains("emc")) return SkillBranch.VISION;
        return SkillBranch.SPIRIT;
    }

    public static boolean exists(ResourceLocation id) {
        return NODES.containsKey(id);
    }

    public static Collection<SkillNode> getAll() {
        return Collections.unmodifiableCollection(NODES.values());
    }
    /**
     * 返回需要在 HUD 上显示的技能节点。
     * 仅过滤节点自身的静态配置（hudVisible），不判断运行时激活状态。
     * 激活状态（isPassiveActive）由 HUD 渲染层自行过滤。
     */
    public static Collection<SkillNode> getHud() {
        List<SkillNode> list = new ArrayList<>();
        for (SkillNode node : NODES.values()) {
            if (node.isHudVisible()) {
                list.add(node);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public static void clear() {
        NODES.clear();
    }
}