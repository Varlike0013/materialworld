package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import com.varlike.spiritworld.registry.ModItems;
import com.varlike.spiritworld.skill.SpiritToolBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class SpiritToolEvents {

    /** Q 丢弃工具 → 销毁 */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getEntity().getItem();
        if (!stack.is(ModItems.SPIRIT_TOOL.get())) return;

        event.setCanceled(true);
        event.getEntity().discard();
        player.getInventory().removeItem(stack);

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data != null) {
            data.setSpiritToolActive(false);
            NetworkHandler.syncTo(player);
        }
        player.sendSystemMessage(Component.translatable("message.spiritworld.spirit_tool.destroyed"));
    }

    /** 死亡时销毁 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data != null) {
            data.setSpiritToolActive(false);
        }
        // 清空物品栏中的精神工具
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.SPIRIT_TOOL.get())) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        // 重生后尝试重新召唤
        trySummonOnRespawn(player);
    }

    private static void trySummonOnRespawn(ServerPlayer player) {
        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;
        if (data.getSkillLevel(com.varlike.spiritworld.skill.ModSkills.SUMMON_TOOL_ID) <= 0) return;

        // 检查背包是否已有工具
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.SPIRIT_TOOL.get())) return;
        }

        // 消耗精神力
        long cost = 10L;
        if (!data.consumeSpirit(cost)) {
            player.sendSystemMessage(Component.translatable("message.spiritworld.spirit_tool.no_spirit"));
            return;
        }

        // 找空位
        int emptySlot = -1;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) { emptySlot = i; break; }
        }
        if (emptySlot < 0) {
            player.sendSystemMessage(Component.translatable("message.spiritworld.spirit_tool.no_space"));
            return;
        }

        ItemStack tool = SpiritToolBuilder.build(player, data);
        player.getInventory().setItem(emptySlot, tool);
        data.setSpiritToolActive(true);
        NetworkHandler.syncTo(player);
    }

    /** 挖掘方块消耗精神力 */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(ModItems.SPIRIT_TOOL.get())) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        long cost = SpiritToolBuilder.getMiningCost(event.getState());
        if (!data.consumeSpirit(cost)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable(
                    "message.spiritworld.spirit_tool.no_spirit_mining", cost));
            return;
        }
        NetworkHandler.syncTo(player);
    }
}