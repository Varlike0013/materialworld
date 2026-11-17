package com.varlike.oathbreaker.core.network;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.combat.CombatStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 服务端下发的玩家战斗面板。
 *
 * <p>字段与 {@link CombatStats} 一一对应；HUD 与 Tooltip 只读这份快照，
 * 不在客户端重新累加属性。</p>
 */
public record PlayerStatsPayload(
        float health,
        float maxHealth,
        float mana,
        float maxMana,
        float attackDamage,
        float abilityPower,
        float armor,
        float magicResist,
        float attackSpeed,
        float abilityHaste,
        float critChance,
        float critDamage,
        float armorPenetrationFlat,
        float armorPenetrationPercent,
        float magicPenetrationFlat,
        float magicPenetrationPercent,
        float healthRegeneration,
        float manaRegeneration,
        float lifesteal,
        float omnivamp,
        int foodLevel
) implements CustomPacketPayload {
    public static final Type<PlayerStatsPayload> TYPE =
            new Type<>(Oathbreaker.id("player_stats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStatsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeFloat(payload.health);
                buf.writeFloat(payload.maxHealth);
                buf.writeFloat(payload.mana);
                buf.writeFloat(payload.maxMana);
                buf.writeFloat(payload.attackDamage);
                buf.writeFloat(payload.abilityPower);
                buf.writeFloat(payload.armor);
                buf.writeFloat(payload.magicResist);
                buf.writeFloat(payload.attackSpeed);
                buf.writeFloat(payload.abilityHaste);
                buf.writeFloat(payload.critChance);
                buf.writeFloat(payload.critDamage);
                buf.writeFloat(payload.armorPenetrationFlat);
                buf.writeFloat(payload.armorPenetrationPercent);
                buf.writeFloat(payload.magicPenetrationFlat);
                buf.writeFloat(payload.magicPenetrationPercent);
                buf.writeFloat(payload.healthRegeneration);
                buf.writeFloat(payload.manaRegeneration);
                buf.writeFloat(payload.lifesteal);
                buf.writeFloat(payload.omnivamp);
                buf.writeVarInt(payload.foodLevel);
            },
            buf -> new PlayerStatsPayload(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt()
            )
    );

    public static PlayerStatsPayload from(CombatStats stats) {
        return new PlayerStatsPayload(
                stats.health(), stats.maxHealth(),
                stats.mana(), stats.maxMana(),
                stats.attackDamage(), stats.abilityPower(),
                stats.armor(), stats.magicResist(),
                stats.attackSpeed(), stats.abilityHaste(),
                stats.critChance(), stats.critDamage(),
                stats.armorPenetrationFlat(), stats.armorPenetrationPercent(),
                stats.magicPenetrationFlat(), stats.magicPenetrationPercent(),
                stats.healthRegeneration(), stats.manaRegeneration(),
                stats.lifesteal(), stats.omnivamp(),
                stats.foodLevel());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
