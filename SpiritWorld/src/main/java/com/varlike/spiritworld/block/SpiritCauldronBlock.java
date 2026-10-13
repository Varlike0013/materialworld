package com.varlike.spiritworld.block;

import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 精神药水炼药锅。
 * 用玻璃瓶右键可以取出瓶装精神力，水位减 1，减到 0 变回空炼药锅。
 */
public class SpiritCauldronBlock extends LayeredCauldronBlock {

    public SpiritCauldronBlock(Properties properties) {
        super(Biome.Precipitation.NONE, CauldronInteraction.WATER, properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int currentLevel = state.getValue(LEVEL);

        // 消耗玻璃瓶，给予瓶装精神力
        stack.shrink(1);
        ItemStack potion = new ItemStack(ModItems.BOTTLED_SPIRIT.get());
        if (!player.getInventory().add(potion)) {
            player.drop(potion, false);
        }

        // 水位 -1，减到 0 变空炼药锅
        BlockState newState;
        if (currentLevel <= 1) {
            newState = Blocks.CAULDRON.defaultBlockState();
        } else {
            newState = state.setValue(LEVEL, currentLevel - 1);
        }
        level.setBlockAndUpdate(pos, newState);

        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }
}