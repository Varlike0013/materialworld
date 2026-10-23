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
    public static final ResourceLocation SPIRIT_REPAIR_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_repair");
    public static final ResourceLocation COOLDOWN_REDUCTION_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "cooldown_reduction");
    public static final ResourceLocation SPIRIT_SATIETY_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_satiety");
    public static final ResourceLocation SPIRIT_MAINTENANCE_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "spirit_maintenance");

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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    int vision = ClientSpiritData.getVisionLevel();
                    long perFragment = SkillValues.emcPerFragment(lv, vision);
                    List<Component> list = new ArrayList<>();
                    list.add(Component.translatable("skill.tooltip.emc_conversion.rate",
                                    String.valueOf(perFragment))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    list.add(Component.translatable("skill.tooltip.emc_conversion.note")
                            .withStyle(ChatFormatting.GRAY));
                    return list;
                })
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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    int idx = Math.min(lv, SkillValues.TOOL_SPEED_BY_LEVEL.length - 1);
                    return List.of(Component.translatable("skill.tooltip.tool_speed.current",
                                    String.format("%.1f", SkillValues.TOOL_SPEED_BY_LEVEL[idx]))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    int idx = Math.min(lv, SkillValues.TOOL_SPEED_BY_LEVEL.length - 1);
                    return List.of(Component.translatable("skill.tooltip.tool_speed.current",
                                    String.format("%.1f", SkillValues.TOOL_SPEED_BY_LEVEL[idx]))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    int idx = Math.min(lv, SkillValues.TOOL_SPEED_BY_LEVEL.length - 1);
                    return List.of(Component.translatable("skill.tooltip.tool_speed.current",
                                    String.format("%.1f", SkillValues.TOOL_SPEED_BY_LEVEL[idx]))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    int idx = Math.min(lv, SkillValues.TOOL_SPEED_BY_LEVEL.length - 1);
                    return List.of(Component.translatable("skill.tooltip.tool_speed.current",
                                    String.format("%.1f", SkillValues.TOOL_SPEED_BY_LEVEL[idx]))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
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
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    return List.of(Component.translatable("skill.tooltip.fortune.current",
                                    SkillValues.roman(Math.min(lv, SkillValues.FORTUNE_MAX_LEVEL)))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
                .build());
        // === 效率
        SkillTree.register(SkillNode.builder(SPIRIT_EFFICIENCY_ID)
                .passiveAlways()
                .grid(6, 3)                   // 时运 (6,2) 下面
                .maxLevel(5)
                .cost(2).costIncrement(1)
                .prereq(TOOL_PICKAXE_ID, 1)
                .requiredVisionLevel(1)
                .icon("效")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    return List.of(Component.translatable("skill.tooltip.efficiency.current",
                                    SkillValues.roman(Math.min(lv, SkillValues.EFFICIENCY_MAX_LEVEL)))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                })
                .build());

        // ==================== x=0 精神力系 ====================
        SkillTree.register(SkillNode.builder(SPIRIT_ENDURANCE_ID)
                .passiveAlways()
                .grid(0, 0)
                .maxLevel(20)
                .cost(3).costIncrement(1)
                .icon("耐")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.spirit_endurance.current",
                        lv -> String.valueOf(lv * SkillValues.SPIRIT_ENDURANCE_MAX_PER_LEVEL)))
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_TOUGHNESS_ID)
                .passiveAlways()
                .grid(0, 1)
                .maxLevel(20)
                .cost(3).costIncrement(1)
                .prereq(SPIRIT_ENDURANCE_ID)
                .icon("韧")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.spirit_toughness.current",
                        lv -> String.format("%.1f", lv * SkillValues.SPIRIT_TOUGHNESS_REGEN_PER_LEVEL)))

                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_STRENGTH_ID)
                .passiveAlways()
                .grid(0, 2)
                .maxLevel(10)
                .cost(5).costIncrement(2)
                .prereq(SPIRIT_TOUGHNESS_ID)
                .icon("健")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.spirit_strength.current",
                        lv -> String.format("%.0f", lv * SkillValues.SPIRIT_STRENGTH_MAX_PERCENT_PER_LEVEL * 100)))
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_SURGE_ID)
                .passiveAlways()
                .grid(0, 3)
                .maxLevel(10)
                .cost(5).costIncrement(2)
                .prereq(SPIRIT_STRENGTH_ID)
                .icon("涌")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.spirit_surge.current",
                        lv -> String.format("%.0f", lv * SkillValues.SPIRIT_SURGE_REGEN_PERCENT_PER_LEVEL * 100)))
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_FLOW_ID)
                .passiveAlways()
                .grid(0, 4)
                .maxLevel(5)
                .cost(10).costIncrement(3)
                .prereq(SPIRIT_SURGE_ID)
                .icon("洪")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.spirit_flow.current",
                        lv -> String.format("%.2f", lv * SkillValues.SPIRIT_FLOW_REGEN_PER_SECOND_PER_LEVEL * 100)))
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_SOURCE_ID)
                .passiveAlways()
                .grid(0, 5)
                .maxLevel(1)
                .cost(50).costIncrement(0)
                .prereq(SPIRIT_FLOW_ID, 3)
                .icon("源")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();
                    return List.of(
                            Component.translatable("skill.tooltip.spirit_source.current",
                                            String.format("%.0f", SkillValues.SPIRIT_SOURCE_MAX_PERCENT * 100),
                                            String.format("%.0f", SkillValues.SPIRIT_SOURCE_REGEN_PERCENT * 100))
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    );
                })
                .build());

        // ==================== x=1 灵视系 ====================
        SkillTree.register(SkillNode.builder(VISION_SHARPNESS_ID)
                .passiveAlways()
                .grid(1, 0)
                .maxLevel(5)
                .cost(5).costIncrement(2)
                .icon("锐")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.vision_sharpness.current",
                        lv -> String.valueOf(lv * SkillValues.VISION_SHARPNESS_FRAGMENTS_PER_LEVEL)))
                .build());

        SkillTree.register(SkillNode.builder(EMC_AFFINITY_ID)
                .passiveAlways()
                .grid(1, 1)
                .maxLevel(5)
                .cost(5).costIncrement(2)
                .prereq(VISION_SHARPNESS_ID)
                .icon("亲")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.emc_affinity.current",
                        lv -> String.format("%.0f", lv * SkillValues.EMC_AFFINITY_COST_REDUCTION_PER_LEVEL * 100)))
                .build());

        SkillTree.register(SkillNode.builder(VISION_MASTERY_ID)
                .passiveAlways()
                .grid(1, 2)
                .maxLevel(3)
                .cost(10).costIncrement(3)
                .prereq(EMC_AFFINITY_ID)
                .icon("精")
                .tooltipExtra(node -> currentLine(node, "skill.tooltip.vision_mastery.current",
                        lv -> String.format("%.0f", lv * SkillValues.VISION_MASTERY_FRAGMENT_PERCENT_PER_LEVEL * 100)))
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
                    float ratio = Math.min(1.0f, Math.max(0, lv) * SkillValues.SHIELD_RATIO_PER_LEVEL);
                    List<Component> list = new ArrayList<>();
                    list.add(Component.translatable("skill.tooltip.spirit_shield.ratio",
                                    String.format("%.0f", ratio * 100))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    list.add(Component.translatable("skill.tooltip.spirit_shield.cost",
                                    String.format("%.1f", SkillValues.SHIELD_SPIRIT_PER_DAMAGE))
                            .withStyle(ChatFormatting.GRAY));
                    if (lv > 0) {
                        long spiritFor100 = (long) Math.ceil(100 * ratio * SkillValues.SHIELD_SPIRIT_PER_DAMAGE);
                        list.add(Component.translatable("skill.tooltip.spirit_shield.example", spiritFor100)
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                    return list;
                })
                .build());

        SkillTree.register(SkillNode.builder(SPIRIT_NIGHT_VISION_ID)
                .passiveToggle()
                .grid(1, 4)
                .maxLevel(5)
                .cost(5).costIncrement(2)
                .requiredVisionLevel(0)
                .icon("夜")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();

                    double penalty = SkillValues.nightVisionMaxPenalty(lv);
                    List<Component> list = new ArrayList<>();

                    if (penalty > 0.0) {
                        list.add(Component.translatable("skill.tooltip.spirit_night_vision.penalty",
                                        String.format("%.0f", penalty * 100))
                                .withStyle(ChatFormatting.RED));
                    } else {
                        list.add(Component.translatable("skill.tooltip.spirit_night_vision.penalty.none")
                                .withStyle(ChatFormatting.GREEN));
                    }

                    return list;
                })
                .build());
        SkillTree.register(SkillNode.builder(SPIRIT_REPAIR_ID)
                .active()
                .grid(2, 4)                       // EMC 转化(2,5) 上方
                .maxLevel(1)
                .cost(5).costIncrement(3)
                .requiredVisionLevel(1)
                .prereq(EMC_CONVERSION_ID)
                .icon("修")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();

                    double perSpirit = SkillValues.repairDurabilityPerSpirit(lv);
                    List<Component> list = new ArrayList<>();

                    // 当前等级兑换比
                    list.add(Component.translatable("skill.tooltip.spirit_repair.rate",
                                    String.format("%.1f", perSpirit))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));

                    // 下一级提升预览
                    if (lv < node.getMaxLevel()) {
                        double nextPerSpirit = SkillValues.repairDurabilityPerSpirit(lv + 1);
                        list.add(Component.translatable("skill.tooltip.spirit_repair.next",
                                        String.format("%.1f", nextPerSpirit))
                                .withStyle(ChatFormatting.GRAY));
                    }

                    return list;
                })
                .build());
        SkillTree.register(SkillNode.builder(COOLDOWN_REDUCTION_ID)
                .passiveAlways()
                .grid(0, 7)
                .maxLevel(5)
                .cost(10).costIncrement(3)
                .requiredVisionLevel(1)
                .icon("冷")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();

                    double reduction = SkillValues.COOLDOWN_REDUCTION_BY_LEVEL[
                            Math.min(lv, SkillValues.COOLDOWN_REDUCTION_BY_LEVEL.length - 1)];
                    double mult = SkillValues.cooldownMultiplier(lv);

                    // 示例：默认 5 秒 / 1 秒冷却缩短后的实际值
                    double normalSec = SkillNode.DEFAULT_COOLDOWN_NORMAL * mult / 20.0;
                    double toggleSec = SkillNode.DEFAULT_COOLDOWN_TOGGLE * mult / 20.0;

                    return List.of(
                            Component.translatable("skill.tooltip.cooldown_reduction.current",
                                            String.format("%.0f", reduction * 100))
                                    .withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("skill.tooltip.cooldown_reduction.example",
                                            String.format("%.2f", toggleSec),
                                            String.format("%.2f", normalSec))
                                    .withStyle(ChatFormatting.GRAY)
                    );
                })
                .build());
        SkillTree.register(SkillNode.builder(SPIRIT_SATIETY_ID)
                .passiveToggle()
                .grid(1, 5)                          // 夜视 (1,4) 下面
                .maxLevel(3)
                .cost(8).costIncrement(3)            // 8/11/14
                .requiredVisionLevel(1)
                .prereq(VISION_MASTERY_ID, 1)        // 前置：灵视精通 1 级
                .icon("饱")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();

                    double penalty = SkillValues.satietyMaxPenalty(lv);
                    List<Component> list = new ArrayList<>();

                    if (penalty > 0.0) {
                        list.add(Component.translatable("skill.tooltip.spirit_satiety.penalty",
                                        String.format("%.0f", penalty * 100))
                                .withStyle(ChatFormatting.RED));
                    } else {
                        list.add(Component.translatable("skill.tooltip.spirit_satiety.penalty.none")
                                .withStyle(ChatFormatting.GREEN));
                    }

                    list.add(Component.translatable("skill.tooltip.spirit_satiety.note")
                            .withStyle(ChatFormatting.GRAY));

                    return list;
                })
                .build());
        SkillTree.register(SkillNode.builder(SPIRIT_MAINTENANCE_ID)
                .passiveToggle()
                .grid(3, 4)                          // 精神修复 (2,4) 右侧
                .maxLevel(3)
                .cost(10).costIncrement(5)          // 20/30/40
                .requiredVisionLevel(2)
                .prereq(SPIRIT_REPAIR_ID, 3)         //前置：精神修复满级
                .icon("维")
                .tooltipExtra(node -> {
                    int lv = ClientSpiritData.getSkillLevel(node.getId());
                    if (lv <= 0) return List.of();

                    int restore = SkillValues.maintenanceRestorePerSecond(lv);
                    List<Component> list = new ArrayList<>();
                    list.add(Component.translatable("skill.tooltip.spirit_maintenance.rate", restore)
                            .withStyle(ChatFormatting.LIGHT_PURPLE));

                    if (lv < node.getMaxLevel()) {
                        int next = SkillValues.maintenanceRestorePerSecond(lv + 1);
                        list.add(Component.translatable("skill.tooltip.spirit_maintenance.next", next)
                                .withStyle(ChatFormatting.GRAY));
                    }

                    return list;
                })
                .build());
    }
}