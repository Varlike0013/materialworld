package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundUseSkillPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundUseSkillPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "use_skill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUseSkillPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundUseSkillPacket::encode, ServerboundUseSkillPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {}

    public ServerboundUseSkillPacket(RegistryFriendlyByteBuf buf) { this(); }

    public static void handle(ServerboundUseSkillPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
            if (data == null) return;

            ResourceLocation id = data.getEquippedSkill();
            if (id == null) return;

            SkillNode node = SkillTree.get(id);
            if (node == null) return;
            if (data.getSkillLevel(id) <= 0) return;

            // ===== 冷却检查 =====
            long gameTime = player.level().getGameTime();
            if (data.isOnCooldown(id, gameTime)) {
                long remain = data.getCooldownRemaining(id, gameTime);
                player.displayClientMessage(
                        Component.translatable("message.spiritworld.skill.cooldown",
                                        String.format("%.1f", remain / 20.0))
                                .withStyle(ChatFormatting.YELLOW),
                        true);
                return;
            }

            // ===== 执行技能 =====
            boolean executed = false;

            if (node.isToggleable()) {
                if (data.isPassiveActive(id)) {
                    data.removePassive(id);
                } else {
                    data.addPassive(id);
                }
                executed = true;
                NetworkHandler.syncTo(player);
            } else if (id.equals(ModSkills.SUMMON_TOOL_ID)) {
                executed = handleSummonTool(player, data);
            } else if (id.equals(ModSkills.SPIRIT_REPAIR_ID)) {
                executed = handleSpiritRepair(player, data);
            } else if (id.equals(ModSkills.SPIRIT_SCAN_ID)) {
                executed = SpiritScanHandler.handleSpiritScan(player, data);
            }

            // ===== 成功才进入冷却 =====
            if (executed) {
                long cooldown = data.getEffectiveCooldown(id);
                if (cooldown > 0L) {
                    data.setCooldown(id, gameTime + cooldown);
                }
                data.markDirty();
            }
        });
    }

    // ==================== 召唤精神工具 ====================

    /** 成功召唤返回 true；已有工具/无空位/精神力不足返回 false。 */
    private static boolean handleSummonTool(ServerPlayer player, PlayerSpirit data) {
        if (data.isSpiritToolActive()) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_tool.already_have")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        long cost = SkillValues.SUMMON_TOOL_SPIRIT_COST;
        if (data.getSpirit() < cost) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_tool.no_spirit", cost)
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        if (player.getInventory().getFreeSlot() == -1) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_tool.no_space")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        data.consumeSpirit(cost);
        ItemStack tool = SpiritToolBuilder.build(player, data);
        player.getInventory().add(tool);
        data.setSpiritToolActive(true);
        data.markDirty();

        NetworkHandler.syncTo(player);
        player.displayClientMessage(
                Component.translatable("message.spiritworld.spirit_tool.summoned")
                        .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    // ==================== 精神修复 ====================

    /** 成功修复返回 true；主手不可修/满耐久/精神力不足返回 false。 */
    private static boolean handleSpiritRepair(ServerPlayer player, PlayerSpirit data) {
        ItemStack held = player.getMainHandItem();

        if (!held.isDamageableItem()) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_repair.not_damageable")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        int currentDamage = held.getDamageValue();
        if (currentDamage <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_repair.full")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        long spirit = data.getSpirit();
        if (spirit < SkillValues.REPAIR_MIN_SPIRIT) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_repair.no_spirit")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        // ✅ 按技能等级取兑换比
        int lv = data.getSkillLevel(ModSkills.SPIRIT_REPAIR_ID);
        double perSpirit = SkillValues.repairDurabilityPerSpirit(lv);

        // 按需消耗：只花够修满的那部分
        long neededSpirit = (long) Math.ceil(currentDamage / perSpirit);
        long spent = Math.min(spirit, neededSpirit);
        int restore = (int) Math.min(currentDamage,
                (long) Math.floor(spent * perSpirit));

        if (restore <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_repair.no_spirit")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        data.setSpirit(spirit - spent);
        held.setDamageValue(currentDamage - restore);
        data.markDirty();

        NetworkHandler.syncTo(player);
        player.displayClientMessage(
                Component.translatable("message.spiritworld.spirit_repair.success", restore, spent)
                        .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}