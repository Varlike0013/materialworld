package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.client.ClientSpiritData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.varlike.spiritworld.SpiritWorld.MOD_ID;

/** 把 SpiritInfoHolder 的灵视信息接入 AetherUI 的右下角瞄准信息 HUD。 */
public final class SpiritBlockInfoProvider implements BlockInfoProvider {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "spirit_info");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public List<Component> getLines(Level level, BlockPos pos, BlockState state,
                                    BlockEntity blockEntity, BlockHitResult hit, LocalPlayer player) {
        if (!(blockEntity instanceof SpiritInfoHolder holder)) return List.of();
        List<SpiritInfoLine> raw = holder.getSpiritInfo();
        if (raw == null || raw.isEmpty()) return List.of();

        int visionLevel = ClientSpiritData.getVisionLevel();
        List<Component> lines = new ArrayList<>();
        for (SpiritInfoLine line : raw) {
            if (line == null || line.text() == null) continue;
            if (line.isVisibleAt(visionLevel)) {
                lines.add(line.text());
            } else {
                lines.add(obfuscate(line.text(), pos, line.requiredVisionLevel()));
            }
        }
        return lines;
    }

    /** 灵视不足时用固定种子生成乱序符号，既提示“信息存在但看不懂”，也不会每帧闪烁。 */
    private static Component obfuscate(Component source, BlockPos pos, int requiredVisionLevel) {
        final char[] symbols = {'*', '%', '￥', '#', '@', '&', '?', '$', '§', 'Ω', '≠', '※'};
        String text = source.getString();
        int length = Math.max(4, Math.min(22, text.length()));
        long seed = (long) text.hashCode() ^ ((long) pos.getX() * 31L) ^ ((long) pos.getY() * 131L)
                ^ ((long) pos.getZ() * 17L) ^ (requiredVisionLevel * 37L);
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(symbols[random.nextInt(symbols.length)]);
        }
        return Component.literal(sb.toString()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
    }
}
