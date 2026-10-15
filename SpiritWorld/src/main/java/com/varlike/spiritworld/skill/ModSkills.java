package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.resources.ResourceLocation;

public class ModSkills {

    // ===== 已有技能 ID =====
    public static final ResourceLocation EMC_CONVERSION_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "emc_conversion");

    // ===== 精神工具技能 ID =====
    public static final ResourceLocation SUMMON_TOOL_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "summon_spirit_tool");
    public static final ResourceLocation TOOL_PICKAXE_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_tool_pickaxe");
    public static final ResourceLocation TOOL_AXE_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_tool_axe");
    public static final ResourceLocation TOOL_SHOVEL_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_tool_shovel");
    public static final ResourceLocation TOOL_HOE_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_tool_hoe");
    public static final ResourceLocation SPIRIT_FORTUNE_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_fortune");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, path);
    }

    public static void registerAll() {
        SkillTree.clear();

        // ===== 基础：EMC 转化 =====
        SkillTree.register(SkillNode.builder(EMC_CONVERSION_ID)
                .passiveEquippable()
                .grid(2, 5)
                .maxLevel(5)
                .cost(20)
                .requiredVisionLevel(0)
                .icon("转")
                .build());

        // ===== 常驻被动 =====
        SkillTree.register(SkillNode.builder(id("spirit_sense"))
                .passiveAlways()
                .grid(0, 5)
                .maxLevel(3)
                .cost(5)
                .icon("感")
                .build());

        // ===== 主技能：召唤精神工具 =====
        // 前置：EMC 转化
        SkillTree.register(SkillNode.builder(SUMMON_TOOL_ID)
                .active()
                .grid(2, 1)
                .maxLevel(1)
                .cost(15)
                .requiredVisionLevel(1)
                .prereq(EMC_CONVERSION_ID)
                .icon("召")
                .build());

        // ===== 镐分支 =====
        SkillTree.register(SkillNode.builder(TOOL_PICKAXE_ID)
                .passiveAlways()
                .grid(2, 0)
                .maxLevel(5)
                .cost(8)
                .requiredVisionLevel(1)
                .prereq(SUMMON_TOOL_ID)
                .icon("镐")
                .build());

        // ===== 斧分支 =====
        SkillTree.register(SkillNode.builder(TOOL_AXE_ID)
                .passiveAlways()
                .grid(4, 1)
                .maxLevel(5)
                .cost(8)
                .requiredVisionLevel(1)
                .prereq(SUMMON_TOOL_ID)
                .icon("斧")
                .build());

        // ===== 铲分支 =====
        SkillTree.register(SkillNode.builder(TOOL_SHOVEL_ID)
                .passiveAlways()
                .grid(2, 2)
                .maxLevel(5)
                .cost(8)
                .requiredVisionLevel(1)
                .prereq(SUMMON_TOOL_ID)
                .icon("铲")
                .build());

        // ===== 锄分支 =====
        SkillTree.register(SkillNode.builder(TOOL_HOE_ID)
                .passiveAlways()
                .grid(4, 0)
                .maxLevel(5)
                .cost(8)
                .requiredVisionLevel(1)
                .prereq(SUMMON_TOOL_ID)
                .icon("锄")
                .build());

        // ===== 时运：开关被动，前置为镐 =====
        SkillTree.register(SkillNode.builder(SPIRIT_FORTUNE_ID)
                .passiveToggle()
                .grid(2, 3)
                .maxLevel(3)
                .cost(12)
                .requiredVisionLevel(2)
                .prereq(TOOL_PICKAXE_ID)
                .icon("运")
                .build());
    }
}