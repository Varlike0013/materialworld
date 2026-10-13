package com.varlike.spiritworld.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpiritStored(long amount) {

    public static final SpiritStored EMPTY = new SpiritStored(0);

    public static final Codec<SpiritStored> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("amount").forGetter(SpiritStored::amount)
            ).apply(instance, SpiritStored::new));

    public static final StreamCodec<ByteBuf, SpiritStored> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, SpiritStored::amount,
                    SpiritStored::new
            );
}