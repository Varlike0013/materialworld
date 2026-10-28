package com.varlike.spiritworld.component;

import com.mojang.serialization.Codec;
import com.varlike.spiritworld.skill.ManaType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

/** 魔力水晶内部存储的魔力（每种类型独立）。 */
public record CrystalMana(Map<ManaType, Long> mana) {

    public static final CrystalMana EMPTY = new CrystalMana(Map.of());

    public static final Codec<CrystalMana> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.LONG).xmap(
                    m -> {
                        Map<ManaType, Long> out = new HashMap<>();
                        for (var e : m.entrySet()) {
                            ManaType t = ManaType.byId(e.getKey());
                            if (e.getValue() > 0) out.put(t, e.getValue());
                        }
                        return new CrystalMana(out);
                    },
                    c -> {
                        Map<String, Long> out = new HashMap<>();
                        c.mana.forEach((t, v) -> { if (v > 0) out.put(t.id(), v); });
                        return out;
                    });

    public static final StreamCodec<ByteBuf, CrystalMana> STREAM_CODEC =
            StreamCodec.of(
                    (buf, c) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, c.mana.size());
                        for (var e : c.mana.entrySet()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, e.getKey().id());
                            ByteBufCodecs.VAR_LONG.encode(buf, e.getValue());
                        }
                    },
                    buf -> {
                        int size = ByteBufCodecs.VAR_INT.decode(buf);
                        Map<ManaType, Long> out = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            ManaType t = ManaType.byId(ByteBufCodecs.STRING_UTF8.decode(buf));
                            long v = ByteBufCodecs.VAR_LONG.decode(buf);
                            if (v > 0) out.put(t, v);
                        }
                        return new CrystalMana(out);
                    });

    public long get(ManaType type) {
        return mana.getOrDefault(type, 0L);
    }

    public long total() {
        long s = 0;
        for (long v : mana.values()) s += v;
        return s;
    }

    public CrystalMana with(ManaType type, long amount) {
        Map<ManaType, Long> copy = new HashMap<>(mana);
        if (amount <= 0) copy.remove(type);
        else copy.put(type, amount);
        return new CrystalMana(copy);
    }
}