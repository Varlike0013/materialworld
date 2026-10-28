package com.varlike.spiritworld.block;

import com.mojang.serialization.MapCodec;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.registry.ModItems;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 精神魔力聚集器。
 * 三个等级通过构造参数区分（速度/容量/tier），共用同一个 BlockEntity 类型。
 */
public class SpiritCollectorBlock extends BaseEntityBlock {

    private final long speed;
    private final long capacity;
    private final int tier;

    public SpiritCollectorBlock(Properties props, long speed, long capacity, int tier) {
        super(props);
        this.speed = speed;
        this.capacity = capacity;
        this.tier = tier;
    }

    /** Codec fallback（datagen / simpleCodec 使用）；游戏运行时走 4 参构造。 */
    public SpiritCollectorBlock(Properties props) {
        this(props, 0L, 0L, 0);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SpiritCollectorBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritCollectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof SpiritCollectorBlockEntity collector) {
                SpiritCollectorBlockEntity.serverTick(lvl, pos, st, collector);
            }
        };
    }

    public long getSpeed()    { return speed; }
    public long getCapacity() { return capacity; }
    public int  getTier()     { return tier; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpiritCollectorBlockEntity collector)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ==== 分支 1：水瓶 → 瓶装精神力（消耗 20 精神魔力）====
        if (isWaterBottle(stack)) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;

            long cost = 20L;
            long have = collector.getMana(ManaType.SPIRIT);
            if (have < cost) {
                player.displayClientMessage(
                        Component.translatable("message.spiritworld.spirit_collector.no_spirit_mana", cost)
                                .withStyle(ChatFormatting.RED),
                        true);
                return ItemInteractionResult.CONSUME;
            }

            collector.consumeMana(ManaType.SPIRIT, cost);

            // 消耗水瓶 + 返还玻璃瓶（创造模式不消耗）
            if (!player.isCreative()) {
                stack.shrink(1);
                ItemStack glass = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(glass)) player.drop(glass, false);
            }

            // 给瓶装精神力
            ItemStack bottled = new ItemStack(ModItems.BOTTLED_SPIRIT.get());
            if (!player.getInventory().add(bottled)) player.drop(bottled, false);

            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_collector.converted")
                            .withStyle(ChatFormatting.GREEN),
                    true);
            return ItemInteractionResult.CONSUME;
        }

        // ==== 分支 2：Shift + 魔力水晶 → 装入聚集器 ====
        if (player.isShiftKeyDown() && stack.getItem() instanceof ManaCrystalItem) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            collector.insertCrystal(stack, player);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SpiritCollectorBlockEntity be)) return InteractionResult.PASS;
        if (be.getCrystal().isEmpty()) return InteractionResult.PASS;

        ItemStack out = be.extractCrystalWithMana();  // 同样方法需要加上
        if (!player.getInventory().add(out)) player.drop(out, false);
        return InteractionResult.CONSUME;
    }
    /** 判断是否水药水 */
    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }
}