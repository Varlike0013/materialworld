package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.resources.ResourceLocation;

/**
 * 所有技能的效果数值集中声明处。
 * 计算层（PlayerSpirit / SpiritShieldEvents / SpiritToolBuilder / ...）
 * 和显示层（ModSkills.tooltipExtra）都必须引用这里的常量，禁止硬编码。
 *
 * 需要玩家在配置里调的项（默认上限、恢复间隔、夜视基础消耗等）放在 Config，
 * 这里只放「技能平衡数值」。
 */
public final class SkillValues {

    private SkillValues() {}

    // ==================== EMC 转化 ====================

    /** Lv1、视觉 0 级时，每 1 灵视碎片的 EMC 消耗 */
    public static final long EMC_CONVERSION_COST_BASE = 100L;

    /** 每级技能减少的 EMC 消耗 */
    public static final long EMC_CONVERSION_COST_STEP = 10L;

    /** 每级视觉等级额外折扣（0.02 = 2%） */
    public static final double VISION_CONVERSION_DISCOUNT_PER_LEVEL = 0.02;

    /** 视觉折扣上限（0.5 = 50%） */
    public static final double VISION_CONVERSION_DISCOUNT_CAP = 0.5;

    /** 单参数版本：只算技能等级（tooltip 在未同步视觉等级时也可用） */
    public static long emcPerFragment(int skillLevel) {
        return emcPerFragment(skillLevel, 0);
    }

    /**
     * 每 1 灵视碎片的 EMC 消耗。
     * 技能等级越高越便宜，视觉等级再叠加折扣。
     * Lv1 视觉0 = 100；Lv5 视觉0 = 40；Lv5 视觉25 = 20（折扣封顶 50%）
     */
    public static long emcPerFragment(int skillLevel, int visionLevel) {
        long base = EMC_CONVERSION_COST_BASE
                - Math.max(0, skillLevel - 1) * EMC_CONVERSION_COST_STEP;
        double discount = Math.min(VISION_CONVERSION_DISCOUNT_CAP,
                Math.max(0, visionLevel) * VISION_CONVERSION_DISCOUNT_PER_LEVEL);
        return Math.max(1L, (long) Math.ceil(base * (1.0 - discount)));
    }
    // ==================== 精神力系 ====================
    /** 精神耐力：每级 +精神力上限（固定） */
    public static final long SPIRIT_ENDURANCE_MAX_PER_LEVEL = 10L;
    /** 精神韧性：每级 +精神力恢复（固定，浮点） */
    public static final double SPIRIT_TOUGHNESS_REGEN_PER_LEVEL = 0.5;
    /** 精神强健：每级 +精神力上限百分比（0.05 = 5%） */
    public static final double SPIRIT_STRENGTH_MAX_PERCENT_PER_LEVEL = 0.05;
    /** 精神涌动：每级 +精神力恢复百分比（0.05 = 5%） */
    public static final double SPIRIT_SURGE_REGEN_PERCENT_PER_LEVEL = 0.05;
    /** 精神洪流：每级每秒恢复「上限的百分比」（0.0025 = 0.25%） */
    public static final double SPIRIT_FLOW_REGEN_PER_SECOND_PER_LEVEL = 0.0025;
    /** 精神源泉：固定 +50% 上限和恢复（0.5 = 50%） */
    public static final double SPIRIT_SOURCE_MAX_PERCENT = 0.5;
    public static final double SPIRIT_SOURCE_REGEN_PERCENT = 0.5;

    // ==================== 灵视系 ====================
    /** 灵视敏锐：每级 +灵视碎片（固定） */
    public static final long VISION_SHARPNESS_FRAGMENTS_PER_LEVEL = 2L;
    /** EMC 亲和：每级 -EMC 转化消耗百分比（0.03 = 3%） */
    public static final double EMC_AFFINITY_COST_REDUCTION_PER_LEVEL = 0.03;
    /** 灵视精通：每级 +灵视碎片百分比（0.15 = 15%） */
    public static final double VISION_MASTERY_FRAGMENT_PERCENT_PER_LEVEL = 0.15;
    // ==================== 精神工具 ====================
    public static final long SUMMON_TOOL_SPIRIT_COST = 10L;
    /** 各分支等级 1-5 对应的挖掘速度（索引 0 为占位） */
    public static final float[] TOOL_SPEED_BY_LEVEL = {
            1.0f,   // 0 级（无 ToolRule，实际不使用）
            2.0f,   // 1 级
            4.0f,   // 2 级
            6.0f,   // 3 级
            8.0f,   // 4 级
            10.0f   // 5 级
    };
    /** 时运最大等级（同时用于 clamp 和显示） */
    public static final int FORTUNE_MAX_LEVEL = 3;
    /** 效率最大等级 */
    public static final int EFFICIENCY_MAX_LEVEL = 5;
    // ==================== 精神护盾 ====================
    /** 每级吸收伤害比例（0.05 = 5%/级，5 级封顶 25%） */
    public static final float SHIELD_RATIO_PER_LEVEL = 0.05f;
    /** 每点伤害消耗的精神力（5.0 = 1 伤害 = 5 精神力） */
    public static final float SHIELD_SPIRIT_PER_DAMAGE = 5.0f;
    // ==================== 精神夜视 ====================
    /**
     * 开启夜视时的精神力上限惩罚，索引为等级。
     * Lv1=-12%, Lv2=-8%, Lv3=-4%, Lv4=0%, Lv5=0%
     */
    public static final double[] NIGHT_VISION_MAX_PENALTY = {
            0.00,  // 0 级（未学）
            0.12,  // Lv1
            0.09,  // Lv2
            0.06,  // Lv3
            0.03,  // Lv4
            0.00   // Lv5：无惩罚
    };
    /** 给定等级返回上限惩罚（0.0-1.0），越界返回 0 */
    public static double nightVisionMaxPenalty(int level) {
        if (level <= 0 || level >= NIGHT_VISION_MAX_PENALTY.length) return 0.0;
        return NIGHT_VISION_MAX_PENALTY[level];
    }
    /** 夜视效果刷新间隔（tick），200 = 10 秒 */
    public static final int NIGHT_VISION_REFRESH_TICKS = 200;
    /** 夜视检查节拍（tick），100 = 5 秒 */
    public static final int NIGHT_VISION_CHECK_INTERVAL = 100;
    // ==================== 精神修复 ====================
    // ==================== 精神修复 ====================

    /**
     * 各等级「每点精神力恢复的耐久」，索引 0 为占位。
     * Lv1=2.0, Lv2=3.0, Lv3=4.5
     */
    public static final double[] REPAIR_DURABILITY_PER_SPIRIT_BY_LEVEL = {
            0.0,   // 0 级（未学）
            2.0,   // Lv1
            3.0,   // Lv2
            4.5    // Lv3
    };

    /** 给定等级返回兑换比；越界返回 Lv1 的值，避免除零 */
    public static double repairDurabilityPerSpirit(int level) {
        if (level <= 0) return REPAIR_DURABILITY_PER_SPIRIT_BY_LEVEL[1];
        int idx = Math.min(level, REPAIR_DURABILITY_PER_SPIRIT_BY_LEVEL.length - 1);
        return REPAIR_DURABILITY_PER_SPIRIT_BY_LEVEL[idx];
    }
    /** 触发修复所需的最低精神力 */
    public static final long REPAIR_MIN_SPIRIT = 1L;
    // ==================== 便捷格式化 ====================
    /** 罗马数字 1-5，用于时运/效率显示 */
    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};
    public static String roman(int n) {
        return (n >= 1 && n <= ROMAN.length - 1) ? ROMAN[n] : String.valueOf(n);
    }
    // ==================== 冷却缩减 ====================
    /**
     * 各等级冷却减少比例，索引 0 为占位。
     * Lv1=10%, Lv2=18%, Lv3=25%, Lv4=30%, Lv5=33%
     * 递减曲线，避免高等级堆满后冷却归零。
     */
    public static final double[] COOLDOWN_REDUCTION_BY_LEVEL = {
            0.00, 0.10, 0.18, 0.25, 0.30, 0.33
    };
    /** 冷却倍率下限，防止天赋叠加后无限接近 0 */
    public static final double COOLDOWN_MULTIPLIER_MIN = 0.2;
    /** 给定等级返回冷却倍率（1.0 = 无减少） */
    public static double cooldownMultiplier(int level) {
        if (level <= 0 || level >= COOLDOWN_REDUCTION_BY_LEVEL.length) {
            return 1.0;
        }
        return Math.max(COOLDOWN_MULTIPLIER_MIN, 1.0 - COOLDOWN_REDUCTION_BY_LEVEL[level]);
    }
    // ==================== 精神饱食 ====================

    /**
     * 激活时的精神力上限惩罚，索引为等级。
     * Lv1=-10%, Lv2=-5%, Lv3=0%（满级无惩罚）
     */
    public static final double[] SATIETY_MAX_PENALTY = {
            0.00,  // 0 级（未学）
            0.10,  // Lv1
            0.05,  // Lv2
            0.00   // Lv3：无惩罚
    };

    public static double satietyMaxPenalty(int level) {
        if (level <= 0 || level >= SATIETY_MAX_PENALTY.length) return 0.0;
        return SATIETY_MAX_PENALTY[level];
    }
    /** 饱食度满值 */
    public static final int SATIETY_FULL_FOOD = 20;
    /** 饱和度满值 */
    public static final float SATIETY_FULL_SATURATION = 5.0f;
    // ==================== 精神维护 ====================
    /** 每级每秒恢复的耐久（X = 3 * LV） */
    public static final int MAINTENANCE_RESTORE_PER_LEVEL = 3;
    /** 触发间隔（tick），20 = 1 秒 */
    public static final int MAINTENANCE_INTERVAL = 20;
    /** 给定等级返回每秒恢复量；0 级返回 0 */
    public static int maintenanceRestorePerSecond(int level) {
        if (level <= 0) return 0;
        return MAINTENANCE_RESTORE_PER_LEVEL * level;
    }
    // ==================== 精神飞行 ====================
    /** 各等级悬停每秒消耗，索引 0 为占位 */
    public static final double[] FLIGHT_COST_PER_SECOND_BY_LEVEL = {
            0.0,   // 0 级（未学）
            3.0,   // Lv1
            2.5,   // Lv2
            2.0    // Lv3
    };

    /** 上升消耗倍率（相对悬停） */
    public static final double FLIGHT_RISING_MULTIPLIER = 2.0;

    /** 缓降状态恢复飞行所需的最低精神力（秒数） */
    public static final double FLIGHT_GLIDE_EXIT_SECONDS = 5.0;

    public static double flightCostPerSecond(int level) {
        if (level <= 0 || level >= FLIGHT_COST_PER_SECOND_BY_LEVEL.length) return 0.0;
        return FLIGHT_COST_PER_SECOND_BY_LEVEL[level];
    }
    // ==================== 精神扫描 ====================

    /** 各等级范围上限（格），索引 0 为占位 */
    public static final int[] SCAN_RANGE_MAX_BY_LEVEL = {
            0,   // 0 级（未学）
            16,  // Lv1
            24,  // Lv2
            32,  // Lv3
            40,  // Lv4
            48   // Lv5
    };

    /** 各等级图标持续秒数，索引 0 为占位 */
    public static final int[] SCAN_DURATION_BY_LEVEL = {
            0,   // 0 级
            5,   // Lv1
            7,   // Lv2
            9,   // Lv3
            11,  // Lv4
            13   // Lv5
    };

    /** 扫描配置滑块的最小值 */
    public static final int SCAN_RANGE_MIN = 8;

    /** 扫描配置滑块步长 */
    public static final int SCAN_RANGE_STEP = 2;

    /** 扫描冷却（tick），基础 3 秒，受精神迅捷影响 */
    public static final long SCAN_BASE_COOLDOWN_TICKS = 60L;

    public static int scanRangeMax(int level) {
        if (level <= 0 || level >= SCAN_RANGE_MAX_BY_LEVEL.length) return 0;
        return SCAN_RANGE_MAX_BY_LEVEL[level];
    }

    public static int scanDurationSeconds(int level) {
        if (level <= 0 || level >= SCAN_DURATION_BY_LEVEL.length) return 0;
        return SCAN_DURATION_BY_LEVEL[level];
    }

    /** 按范围平方计算精神力消耗：ceil(range² / 128) */
    public static long scanCostForRange(int range) {
        if (range <= 0) return 0L;
        long sq = (long) range * range;
        return Math.max(1L, (sq + 127L) / 128L);
    }

// ---------- 可扫描矿物标识 ----------

    private static ResourceLocation scanId(String name) {
        return ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "scan/" + name);
    }

    public static final ResourceLocation SCAN_COAL          = scanId("coal");
    public static final ResourceLocation SCAN_IRON          = scanId("iron");
    public static final ResourceLocation SCAN_GOLD          = scanId("gold");
    public static final ResourceLocation SCAN_REDSTONE      = scanId("redstone");
    public static final ResourceLocation SCAN_LAPIS         = scanId("lapis");
    public static final ResourceLocation SCAN_DIAMOND       = scanId("diamond");
    public static final ResourceLocation SCAN_EMERALD       = scanId("emerald");
    public static final ResourceLocation SCAN_ANCIENT_DEBRIS = scanId("ancient_debris");
    public static final ResourceLocation SCAN_OTHER         = scanId("other");

    /** 该矿物标识解锁所需等级 */
    public static int scanOreUnlockLevel(ResourceLocation oreId) {
        if (SCAN_COAL.equals(oreId) || SCAN_IRON.equals(oreId)) return 1;
        if (SCAN_GOLD.equals(oreId) || SCAN_REDSTONE.equals(oreId) || SCAN_LAPIS.equals(oreId)) return 2;
        if (SCAN_DIAMOND.equals(oreId) || SCAN_EMERALD.equals(oreId)) return 3;
        if (SCAN_ANCIENT_DEBRIS.equals(oreId)) return 4;
        if (SCAN_OTHER.equals(oreId)) return 5;
        return Integer.MAX_VALUE;   // 未知标识，永不解锁
    }

    /** 按技能等级返回所有已解锁矿物标识（用于界面展示，不读玩家勾选） */
    public static java.util.Set<ResourceLocation> unlockedOres(int level) {
        java.util.Set<ResourceLocation> set = new java.util.HashSet<>();
        for (ResourceLocation id : ALL_SCAN_ORES) {
            if (scanOreUnlockLevel(id) <= level) set.add(id);
        }
        return set;
    }

    /** 固定顺序的矿物标识列表，用于配置界面按顺序显示 */
    public static final java.util.List<ResourceLocation> ALL_SCAN_ORES = java.util.List.of(
            SCAN_COAL, SCAN_IRON,
            SCAN_GOLD, SCAN_REDSTONE, SCAN_LAPIS,
            SCAN_DIAMOND, SCAN_EMERALD,
            SCAN_ANCIENT_DEBRIS,
            SCAN_OTHER
    );
}