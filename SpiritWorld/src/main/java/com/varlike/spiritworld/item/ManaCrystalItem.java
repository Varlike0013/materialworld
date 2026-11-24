package com.varlike.spiritworld.item;

import com.varlike.spiritworld.component.CrystalMana;
import com.varlike.spiritworld.component.ModDataComponents;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelReader;

import java.util.List;

/**
 * 魔力水晶：可装入魔力聚集器 / 魔力核心，提供容量加成 + 携带魔力。
 */
public class ManaCrystalItem extends Item {

    private final long capacity;

    public ManaCrystalItem(Properties props, long capacity) {
        super(props);
        this.capacity = capacity;
    }

    public long getCapacity() {
        return capacity;
    }

    /**
     * 允许 Shift+右键时仍然调用方块的 useItemOn。
     * MC 1.21.1 默认在 Shift+右键时跳过方块交互，导致水晶无法装入聚集器/核心。
     */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    // ==================== 组件读写 ====================

    public static CrystalMana getMana(ItemStack stack) {
        CrystalMana c = stack.get(ModDataComponents.CRYSTAL_MANA.get());
        return c == null ? CrystalMana.EMPTY : c;
    }

    public static void setMana(ItemStack stack, CrystalMana mana) {
        stack.set(ModDataComponents.CRYSTAL_MANA.get(), mana);
    }

    public static long getTotal(ItemStack stack) {
        return getMana(stack).total();
    }

    // ==================== Tooltip ====================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        CrystalMana mana = getMana(stack);
        long total = mana.total();
        tooltip.add(Component.translatable("tooltip.spiritworld.mana_crystal.capacity",
                        total, capacity)
                .withStyle(ChatFormatting.AQUA));

        for (ManaType type : ManaType.values()) {
            long amt = mana.get(type);
            if (amt <= 0) continue;
            tooltip.add(type.applyStyle(
                    Component.translatable("tooltip.spiritworld.mana_crystal.entry",
                            type.displayName(), amt)));
        }

        tooltip.add(Component.translatable("tooltip.spiritworld.mana_crystal.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}