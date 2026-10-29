package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class ManaAlloyArmorSetEvents {

    private static final ResourceLocation SET_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "mana_alloy_set");

    /** 4 件效果 */
    private static final double FULL_SET_PERCENT = 0.25;
    /** 2-3 件效果 */
    private static final double PARTIAL_SET_PERCENT = 0.10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;   // 每 1 秒检查一次

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
        if (data == null) return;

        int pieces = countManaAlloyArmorPieces(player);

        if (pieces >= 4) {
            data.addMaxSpiritPercentModifier(SET_MODIFIER_ID, FULL_SET_PERCENT);
        } else if (pieces >= 2) {
            data.addMaxSpiritPercentModifier(SET_MODIFIER_ID, PARTIAL_SET_PERCENT);
        } else {
            data.removeMaxSpiritModifier(SET_MODIFIER_ID);
        }
    }

    private static int countManaAlloyArmorPieces(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.is(ModItems.MANA_ALLOY_HELMET.get())
                    || stack.is(ModItems.MANA_ALLOY_CHESTPLATE.get())
                    || stack.is(ModItems.MANA_ALLOY_LEGGINGS.get())
                    || stack.is(ModItems.MANA_ALLOY_BOOTS.get())) {
                count++;
            }
        }
        return count;
    }
}