package com.varlike.spiritworld.item;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 瓶装精神力物品。
 * 饮用后恢复精神力并返还玻璃瓶。
 */
public class BottledSpiritItem extends Item {

    public BottledSpiritItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return super.finishUsingItem(stack, level, entity);
        }

        // ===== 服务端：恢复精神力 =====
        if (!level.isClientSide) {
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data != null) {
                long oldSpirit = data.getSpirit();
                data.addSpirit(Config.bottledSpiritRestoreAmount);
                long actual = data.getSpirit() - oldSpirit;

                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.syncTo(sp);
//                    if (actual > 0) {
//                        sp.sendSystemMessage(Component.translatable(
//                                "message.spiritworld.spirit_restored", actual));
//                    } else {
//                        sp.sendSystemMessage(Component.translatable(
//                                "message.spiritworld.spirit_already_full"));
//                    }
                }
            }
        }

        // ===== 消耗瓶子并返还玻璃瓶 =====
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);                                   // 堆叠 -1
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);      // 最后一个：返回玻璃瓶
            }
            if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);  // 背包满时丢出
            }
        }
        return stack;                                          // 还有剩余：保留堆叠
    }
}