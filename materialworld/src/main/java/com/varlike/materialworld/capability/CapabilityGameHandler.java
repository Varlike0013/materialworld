package com.varlike.materialworld.capability;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class CapabilityGameHandler {

    private static final String PERSIST_KEY = "materialworld:player_emc";

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        PlayerEMC oldData = oldPlayer.getCapability(ModCapabilities.PLAYER_EMC);
        PlayerEMC newData = newPlayer.getCapability(ModCapabilities.PLAYER_EMC);
        if (oldData != null && newData != null) {
            newData.copyFrom(oldData);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(PERSIST_KEY)) {
            PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
            if (data != null) {
                data.load(persistent.getCompound(PERSIST_KEY));
            }
        }
        NetworkHandler.syncTo(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
        if (data != null) {
            player.getPersistentData().put(PERSIST_KEY, data.save());
        }
        ModCapabilities.PROVIDER.remove(player.getUUID());
    }
}