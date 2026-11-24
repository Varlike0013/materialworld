package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.client.ClientSpiritData;
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
            if (line.isVisibleAt(visionLevel)) lines.add(line.text());
        }
        return lines;
    }
}
