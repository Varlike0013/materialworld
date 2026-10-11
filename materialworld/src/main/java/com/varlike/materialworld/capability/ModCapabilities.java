package com.varlike.materialworld.capability;

import com.varlike.materialworld.MaterialWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class ModCapabilities {

    public static final PlayerEMCProvider PROVIDER = new PlayerEMCProvider();

    public static final EntityCapability<PlayerEMC, Void> PLAYER_EMC =
            EntityCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "player_emc"),
                    PlayerEMC.class);

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                PLAYER_EMC,
                EntityType.PLAYER,
                PROVIDER
        );
    }
}