package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

/**
 * 精神工具构建器。
 * 根据玩家当前的技能状态，生成一个带对应 ToolRule 和附魔的 ItemStack。
 * 只在召唤时调用。
 */
public final class SpiritToolBuilder {

    private SpiritToolBuilder() {}

    /** 每个分支等级对应的挖掘速度 */
    private static final float[] SPEED_BY_LEVEL = {1.0f, 2.0f, 4.0f, 6.0f, 8.0f, 10.0f};

    public static ItemStack build(Player player, PlayerSpirit data) {
        ItemStack stack = new ItemStack(ModItems.SPIRIT_TOOL.get());

        int pickLevel = data.getSkillLevel(ModSkills.TOOL_PICKAXE_ID);
        int axeLevel = data.getSkillLevel(ModSkills.TOOL_AXE_ID);
        int shovelLevel = data.getSkillLevel(ModSkills.TOOL_SHOVEL_ID);
        int hoeLevel = data.getSkillLevel(ModSkills.TOOL_HOE_ID);

        // ===== ToolRule =====
        java.util.List<net.minecraft.world.item.component.Tool.Rule> rules = new java.util.ArrayList<>();

        if (pickLevel > 0) {
            rules.add(new Tool.Rule(
                    tagOf(player, BlockTags.MINEABLE_WITH_PICKAXE),
                    Optional.of(SPEED_BY_LEVEL[Math.min(pickLevel, 5)]),
                    Optional.of(true)));
        }
        if (axeLevel > 0) {
            rules.add(new Tool.Rule(
                    tagOf(player, BlockTags.MINEABLE_WITH_AXE),
                    Optional.of(SPEED_BY_LEVEL[Math.min(axeLevel, 5)]),
                    Optional.of(true)));
        }
        if (shovelLevel > 0) {
            rules.add(new Tool.Rule(
                    tagOf(player, BlockTags.MINEABLE_WITH_SHOVEL),
                    Optional.of(SPEED_BY_LEVEL[Math.min(shovelLevel, 5)]),
                    Optional.of(true)));
        }
        if (hoeLevel > 0) {
            rules.add(new Tool.Rule(
                    tagOf(player, BlockTags.MINEABLE_WITH_HOE),
                    Optional.of(SPEED_BY_LEVEL[Math.min(hoeLevel, 5)]),
                    Optional.of(true)));
        }

        stack.set(DataComponents.TOOL, new Tool(rules, 1.0f, 0));

        // ===== 附魔（Fortune + Efficiency 共用同一个 Mutable）=====
        var lookup = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        boolean hasEnchant = false;

        // 时运 1-3
        int fortuneLevel = data.getSkillLevel(ModSkills.SPIRIT_FORTUNE_ID);
        if (data.isPassiveActive(ModSkills.SPIRIT_FORTUNE_ID) && fortuneLevel > 0) {
            mutable.set(lookup.getOrThrow(Enchantments.FORTUNE), Math.min(fortuneLevel, 3));
            hasEnchant = true;
        }

        // 效率 1-5
        int efficiencyLevel = data.getSkillLevel(ModSkills.SPIRIT_EFFICIENCY_ID);
        if (data.isPassiveActive(ModSkills.SPIRIT_EFFICIENCY_ID) && efficiencyLevel > 0) {
            mutable.set(lookup.getOrThrow(Enchantments.EFFICIENCY), Math.min(efficiencyLevel, 5));
            hasEnchant = true;
        }

        if (hasEnchant) {
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        return stack;
    }

    private static HolderSet<Block> tagOf(Player player, TagKey<Block> tag) {
        var registry = player.level().registryAccess().lookupOrThrow(Registries.BLOCK);
        var opt = registry.get(tag);
        return opt.<HolderSet<Block>>map(h -> h).orElseGet(() -> HolderSet.direct());
    }

    /** 根据方块获取挖掘消耗的精神力 */
    public static long getMiningCost(net.minecraft.world.level.block.state.BlockState state) {
        // 根据方块所需的工具等级计算消耗
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return 3L;
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return 2L;
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return 1L;
        // 其他按硬度计算
        float hardness = state.getDestroySpeed(null, null);
        if (hardness <= 0.5f) return 1L;
        if (hardness <= 1.5f) return 2L;
        if (hardness <= 3.0f) return 3L;
        return 4L;
    }
}