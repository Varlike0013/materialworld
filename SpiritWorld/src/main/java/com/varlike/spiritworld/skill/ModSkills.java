package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.event.SpiritShieldEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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
    public static final ResourceLocation SPIRIT_EFFICIENCY_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_efficiency");
    // ===== 精神力系 x=0 =====
    public static final ResourceLocation SPIRIT_ENDURANCE_ID = id("spirit_endurance");
    public static final ResourceLocation SPIRIT_TOUGHNESS_ID = id("spirit_toughness");
    public static final ResourceLocation SPIRIT_STRENGTH_ID  = id("spirit_strength");
    public static final ResourceLocation SPIRIT_SURGE_ID     = id("spirit_surge");
    public static final ResourceLocation SPIRIT_FLOW_ID      = id("spirit_flow");
    public static final ResourceLocation SPIRIT_SOURCE_ID    = id("spirit_source");

    // ===== 灵视系 x=1 =====
    public static final ResourceLocation VISION_SHARPNESS_ID = id("vision_sharpness");
    public static final ResourceLocation EMC_AFFINITY_ID     = id("emc_affinity");
    public static final ResourceLocation VISION_MASTERY_ID   = id("vision_mastery");

    // ==== 其他 =====
    public static final ResourceLocation SPIRIT_SHIELD_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_shield");
    public static final ResourceLocation SPIRIT_NIGHT_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_night_vision");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, path);
    }
    // ===== 工具辅助 =====

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    private static String roman(int n) {
        return (n >= 1 && n <= 5) ? ROMAN[n] : String.valueOf(n);
    }
    /** 通用：读取当前等级，0 级返回空列表；否则返回单行提示 */
    private static List<Component> currentLine(SkillNode node, String key, java.util.function.IntFunction<String> valueOf) {
        int lv = ClientSpiritData.getSkillLevel(node.getId());
        if (lv <= 0) return List.of();
        return List.of(
                Component.translatable(key, valueOf.apply(lv))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
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

        // ===== 主技能：召唤精神工具 =====
        // 前置：EMC 转化
        SkillTree.register(SkillNode.builder(SUMMON_TOOL_ID)
                .active()
                .grid(2, 2)
                .maxLevel(1)
                .cost(15)
                .requiredVisionLevel(1)
                .prereq(EMC_CONVERSION_ID)
                .icon("召")
                .build());

        // ===== 镐分支 =====
        SkillTree.register(SkillNode.builder(TOOL_PICKAXE_ID)
                .passiveAlways()
                .grid(4, 2)
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
                .grid(2, 0)
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
                .grid(6, 2)
                .maxLevel(3)
                .cost(5).costIncrement(1)
                .requiredVisionLevel(2)
                .prereq(TOOL_PICKAXE_ID)
                .icon("运")
                .build());
        // === 效率
        SkillTree.register(SkillNode.builder(SPIRIT_EFFICIENCY_ID)
                .passiveAlways()
                .grid(6, 3)                   // 时运 (6,2) 下面
                .maxLevel(5)
                .cost(2).costIncrement(1)
                .costIncrement(2)
                .prereq(TOOL_PICKAXE_ID, 1)
                .requiredVisionLevel(1)
                .icon("效")
                .build());

        // ==================== x=0 精神力系 ====================
        SkillTree.register(SkillNode.builder(SPIRIT_ENDURANCE_ID)
                .passiveAlways()
                .grid(0, 0)
                .maxLevel(20)
                .cost(3).costIncrement(1)
                .icon("耐")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_TOUGHNESS_ID)
                .passiveAlways()
                .grid(0, 1)
                .maxLevel(20)
                .cost(3).costIncrement(1)
                .prereq(SPIRIT_ENDURANCE_ID)
                .icon("韧")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_STRENGTH_ID)
                .passiveAlways()
                .grid(0, 2)
                .maxLevel(10)
                .cost(5).costIncrement(2)
                .prereq(SPIRIT_TOUGHNESS_ID)
                .icon("健")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_SURGE_ID)
                .passiveAlways()
                .grid(0, 3)
                .maxLevel(10)
                .cost(5).costIncrement(2)
                .prereq(SPIRIT_STRENGTH_ID)
                .icon("涌")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_FLOW_ID)
                .passiveAlways()
                .grid(0, 4)
                .maxLevel(5)
                .cost(10).costIncrement(3)
                .prereq(SPIRIT_SURGE_ID)
                .icon("洪")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_SOURCE_ID)
                .passiveAlways()
                .grid(0, 5)
                .maxLevel(1)
                .cost(50).costIncrement(0)
                .prereq(SPIRIT_FLOW_ID, 3)
                .icon("源")
                .build());

        // ==================== x=1 灵视系 ====================
        SkillTree.register(SkillNode.builder(VISION_SHARPNESS_ID)
                .passiveAlways()
                .grid(1, 0)
                .maxLevel(5)
                .cost(5).costIncrement(2)
                .icon("锐")
                .build());

        SkillTree.register(SkillNode.builder(EMC_AFFINITY_ID)
                .passiveAlways()
                .grid(1, 1)
                .maxLevel(5)
                .cost(5).costIncrement(2)
                .prereq(VISION_SHARPNESS_ID)
                .icon("亲")
                .build());

        SkillTree.register(SkillNode.builder(VISION_MASTERY_ID)
                .passiveAlways()
                .grid(1, 2)
                .maxLevel(3)
                .cost(10).costIncrement(3)
                .prereq(EMC_AFFINITY_ID)
                .icon("精")
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_SHIELD_ID)
                .passiveToggle()                       // 可开关，不需要装备
                .grid(1, 3)                            // x=1, y=3（灵视系下方空位）
                .maxLevel(5)
                .cost(8).costIncrement(2)
                .requiredVisionLevel(0)
                .icon("盾")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    float ratio = Math.min(1.0f, Math.max(0, lv) * SpiritShieldEvents.RATIO_PER_LEVEL);
                    List<Component> list = new ArrayList<>();
                    list.add(Component.translatable("skill.tooltip.spirit_shield.ratio",
                                    String.format("%.0f", ratio * 100))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    list.add(Component.translatable("skill.tooltip.spirit_shield.cost",
                                    String.format("%.1f", SpiritShieldEvents.SPIRIT_PER_DAMAGE))
                            .withStyle(ChatFormatting.GRAY));
                    if (lv > 0) {
                        long spiritFor100 = (long) Math.ceil(100 * ratio * SpiritShieldEvents.SPIRIT_PER_DAMAGE);
                        list.add(Component.translatable("skill.tooltip.spirit_shield.example", spiritFor100)
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                    return list;
                })
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_NIGHT_VISION_ID)
                .passiveToggle()                       // 开关被动
                .grid(1, 4)                            // 灵视系下方
                .maxLevel(5)
                .cost(5)
                .costIncrement(2)
                .requiredVisionLevel(0)
                .icon("夜")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    double costPerSec = Config.nightVisionBaseCostPerSecond / lv;
                    return List.of(
                            Component.translatable("skill.tooltip.spirit_night_vision.cost",
                                            String.format("%.2f", costPerSec))
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    );
                })
                .build());

    }
}