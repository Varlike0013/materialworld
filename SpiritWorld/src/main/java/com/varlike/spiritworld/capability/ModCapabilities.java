package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static final PlayerSpiritProvider PROVIDER = new PlayerSpiritProvider();

    public static final EntityCapability<PlayerSpirit, Void> PLAYER_SPIRIT =
            EntityCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "player_spirit"),
                    PlayerSpirit.class);

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                PLAYER_SPIRIT,
                EntityType.PLAYER,
                PROVIDER
        );
    }
}