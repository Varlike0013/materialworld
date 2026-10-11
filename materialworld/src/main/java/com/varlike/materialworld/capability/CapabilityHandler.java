package com.varlike.materialworld.capability;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class CapabilityHandler {

    /** NeoForge 实体能力：Player -> PlayerEMC */
    public static final EntityCapability<PlayerEMC, Void> PLAYER_EMC =
            EntityCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "player_emc"),
                    PlayerEMC.class);

    /** 每个玩家对应一个 Provider 实例，用于持有其 PlayerEMC 数据 */
    private static final java.util.Map<java.util.UUID, PlayerEMCProvider> PROVIDERS =
            new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                PLAYER_EMC,
                EntityType.PLAYER,
                new PlayerEMCProvider()   // 显式传入 Provider 实例，避免 lambda 类型推断干扰
        );
    }
}