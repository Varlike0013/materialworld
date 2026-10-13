package com.varlike.spiritworld.item;

import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.component.ModDataComponents;
import com.varlike.spiritworld.component.SpiritStored;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 精神秘戒。
 * 可以存储玩家的精神力，也可以将含水炼药锅转化为精神药水炼药锅。
 */
public class SpiritRingItem extends Item {

    private final long capacity;

    public SpiritRingItem(long capacity, Properties properties) {
        super(properties);
        this.capacity = capacity;
    }

    public long getCapacity() {
        return capacity;
    }

    public static long getStored(ItemStack stack) {
        SpiritStored stored = stack.get(ModDataComponents.SPIRIT_STORED.get());
        return stored != null ? stored.amount() : 0L;
    }

    public static void setStored(ItemStack stack, long amount) {
        stack.set(ModDataComponents.SPIRIT_STORED.get(), new SpiritStored(Math.max(0, amount)));
    }

    // ==================== 手持右键：存取精神力 ====================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        PlayerSpirit data = sp.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return InteractionResultHolder.fail(stack);

        long stored = getStored(stack);

        if (player.isShiftKeyDown()) {
            // 潜行右键：从戒指提取到玩家
            if (stored <= 0) {
                sp.sendSystemMessage(Component.translatable("message.spiritworld.ring.empty")
                        .withStyle(ChatFormatting.GRAY));
                return InteractionResultHolder.fail(stack);
            }
            long free = data.getEffectiveMaxSpirit() - data.getSpirit();
            long toTransfer = Math.min(stored, free);
            if (toTransfer <= 0) {
                sp.sendSystemMessage(Component.translatable("message.spiritworld.ring.player_full")
                        .withStyle(ChatFormatting.GRAY));
                return InteractionResultHolder.fail(stack);
            }
            data.addSpirit(toTransfer);
            setStored(stack, stored - toTransfer);
            NetworkHandler.syncTo(sp);
            sp.sendSystemMessage(Component.translatable(
                    "message.spiritworld.ring.extract", toTransfer));
        } else {
            // 普通右键：从玩家存入戒指
            if (stored >= capacity) {
                sp.sendSystemMessage(Component.translatable("message.spiritworld.ring.full")
                        .withStyle(ChatFormatting.GRAY));
                return InteractionResultHolder.fail(stack);
            }
            long available = data.getSpirit();
            if (available <= 0) {
                sp.sendSystemMessage(Component.translatable("message.spiritworld.ring.no_spirit")
                        .withStyle(ChatFormatting.GRAY));
                return InteractionResultHolder.fail(stack);
            }
            long toTransfer = Math.min(available, capacity - stored);
            data.consumeSpirit(toTransfer);
            setStored(stack, stored + toTransfer);
            NetworkHandler.syncTo(sp);
            sp.sendSystemMessage(Component.translatable(
                    "message.spiritworld.ring.store", toTransfer));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // ==================== 右键方块：炼药锅转化 ====================

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 只处理含水炼药锅
        if (!state.is(Blocks.WATER_CAULDRON)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.SUCCESS;
        }

        // 消耗 100 精神力
        PlayerSpirit data = sp.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return InteractionResult.PASS;

        final long COST = 100L;
        if (data.getSpirit() < COST) {
            sp.sendSystemMessage(Component.translatable(
                    "message.spiritworld.ring.not_enough", COST));
            return InteractionResult.FAIL;
        }

        // 转换为精神药水炼药锅（保持水位）
        int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
        BlockState spiritState = ModBlocks.SPIRIT_CAULDRON.get().defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, waterLevel);

        data.consumeSpirit(COST);
        NetworkHandler.syncTo(sp);

        level.setBlockAndUpdate(pos, spiritState);
        level.levelEvent(1047, pos, 0); // 炼药锅使用音效

        sp.sendSystemMessage(Component.translatable("message.spiritworld.ring.converted"));
        return InteractionResult.SUCCESS;
    }

    // ==================== Tooltip ====================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        long stored = getStored(stack);
        tooltip.add(Component.translatable("tooltip.spiritworld.ring.stored", stored, capacity)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.spiritworld.ring.usage")
                .withStyle(ChatFormatting.GRAY));
    }
}