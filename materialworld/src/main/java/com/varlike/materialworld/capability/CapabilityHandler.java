package com.varlike.materialworld.capability;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class CapabilityHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(ModCapabilities.PLAYER_EMC).isPresent()) {
                event.addCapability(
                        ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "player_emc"),
                        new PlayerEMCProvider()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(ModCapabilities.PLAYER_EMC).ifPresent(oldData ->
                event.getEntity().getCapability(ModCapabilities.PLAYER_EMC).ifPresent(newData ->
                        newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            NetworkHandler.syncTo(sp);
        }
    }
}