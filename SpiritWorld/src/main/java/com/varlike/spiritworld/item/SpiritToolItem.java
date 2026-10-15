package com.varlike.spiritworld.item;

import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpiritToolItem extends Item {

    public SpiritToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        // 如果玩家不再拥有召唤技能，销毁工具
        if (data.getSkillLevel(ModSkills.SUMMON_TOOL_ID) <= 0) {
            stack.setCount(0);
            data.setSpiritToolActive(false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        // 从客户端缓存读取
        var learned = com.varlike.spiritworld.client.ClientSpiritData.getLearnedSkills();

        int pick = learned.getOrDefault(ModSkills.TOOL_PICKAXE_ID, 0);
        int axe = learned.getOrDefault(ModSkills.TOOL_AXE_ID, 0);
        int shovel = learned.getOrDefault(ModSkills.TOOL_SHOVEL_ID, 0);
        int hoe = learned.getOrDefault(ModSkills.TOOL_HOE_ID, 0);
        int fortune = learned.getOrDefault(ModSkills.SPIRIT_FORTUNE_ID, 0);

        tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.pickaxe", levelName(pick))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.axe", levelName(axe))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.shovel", levelName(shovel))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.hoe", levelName(hoe))
                .withStyle(ChatFormatting.GRAY));

        if (fortune > 0
                && com.varlike.spiritworld.client.ClientSpiritData.isPassiveActive(ModSkills.SPIRIT_FORTUNE_ID)) {
            tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.fortune", fortune)
                    .withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("tooltip.spiritworld.spirit_tool.bound")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    private static Component levelName(int level) {
        return switch (level) {
            case 0 -> Component.translatable("tooltip.spiritworld.spirit_tool.level.empty");
            case 1 -> Component.translatable("tooltip.spiritworld.spirit_tool.level.wood");
            case 2 -> Component.translatable("tooltip.spiritworld.spirit_tool.level.stone");
            case 3 -> Component.translatable("tooltip.spiritworld.spirit_tool.level.iron");
            case 4 -> Component.translatable("tooltip.spiritworld.spirit_tool.level.diamond");
            default -> Component.translatable("tooltip.spiritworld.spirit_tool.level.netherite");
        };
    }
}