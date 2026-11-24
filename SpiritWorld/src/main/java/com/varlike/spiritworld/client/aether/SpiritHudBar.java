package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.spiritworld.ClientConfig;
import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.varlike.spiritworld.SpiritWorld.MOD_ID;

/** 精神力条：通过 AetherUI 注册到左下角内置 HUD，并保留飞行/装备状态图标。 */
public final class SpiritHudBar implements HudBar {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "spirit_bar");

    private static final float BAR_X = 18f;
    private static final float BAR_Y = 5f;
    private static final float BAR_W = 104f;
    private static final float BAR_H = 6f;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public float width() {
        return 142f;
    }

    @Override
    public float height() {
        return 16f;
    }

    @Override
    public boolean visible(LocalPlayer player) {
        return ClientConfig.showSpiritBar
                && !player.isCreative()
                && !player.isSpectator()
                && ClientSpiritData.getMaxSpirit() > 0L;
    }

    @Override
    public void paint(CanvasContext ctx, LocalPlayer player) {
        long spirit = ClientSpiritData.getSpirit();
        long max = ClientSpiritData.getMaxSpirit();

        // 背景与空槽
        ctx.fill(BAR_X - 1f, BAR_Y - 1f, BAR_W + 2f, BAR_H + 2f, 0xFF000000);
        ctx.fill(BAR_X, BAR_Y, BAR_W, BAR_H, 0xFF303030);

        // 蓝色填充（左深蓝 → 上半亮蓝）
        float ratio = max > 0L ? Math.clamp((float) spirit / (float) max, 0f, 1f) : 0f;
        float filled = BAR_W * ratio;
        if (filled > 0f) {
            ctx.fill(BAR_X, BAR_Y, filled, BAR_H, 0xFF0066CC);
            ctx.fill(BAR_X, BAR_Y, filled, BAR_H / 2f, 0xFF3399FF);
        }

        // 被扣掉的上限：红色段
        long baseMax = ClientSpiritData.getBaseMaxSpirit();
        if (baseMax > max && baseMax > 0L) {
            float redStart = BAR_W * ((float) max / (float) baseMax);
            if (redStart < BAR_W) {
                ctx.fill(BAR_X + redStart, BAR_Y, BAR_W - redStart, BAR_H, 0xFF8B1A1A);
                ctx.fill(BAR_X + redStart, BAR_Y, BAR_W - redStart, BAR_H / 2f, 0xFFCC4444);
            }
        }

        // 数值文本
        if (ClientConfig.showSpiritText) {
            Component text = Component.literal(spirit + " / " + max);
            float tw = Minecraft.getInstance().font.width(text);
            ctx.drawString(text, BAR_X + (BAR_W - tw) / 2f, BAR_Y - 1f, 0xFFFFFFFF, true);
        }

        drawFlightIndicator(ctx, player);
        drawEquippedSkill(ctx);
    }

    /** 精神力条左侧：飞行状态图标。 */
    private static void drawFlightIndicator(CanvasContext ctx, LocalPlayer player) {
        if (!ClientSpiritData.isPassiveActive(ModSkills.SPIRIT_FLIGHT_ID)) return;
        int level = ClientSpiritData.getSkillLevel(ModSkills.SPIRIT_FLIGHT_ID);
        if (level <= 0) return;

        long spirit = ClientSpiritData.getSpirit();
        double costPerSec = SkillValues.flightCostPerSecond(level);
        int borderColor;
        if (spirit <= 0L) {
            borderColor = 0xFFFF4444;
        } else if (spirit < costPerSec * SkillValues.FLIGHT_GLIDE_EXIT_SECONDS) {
            borderColor = 0xFFFFD54F;
        } else {
            borderColor = 0xFF4CAF50;
        }

        CanvasShapes.fillCircle(ctx, 9f, 8f, 7f, borderColor);
        CanvasShapes.fillCircle(ctx, 9f, 8f, 5f, 0x80201030);
        ctx.drawString(Component.literal("飞"), 5f, 2f, 0xFFFFFFFF, false);
    }

    /** 精神力条右侧：当前装配技能图标 + 槽位编号。 */
    private static void drawEquippedSkill(CanvasContext ctx) {
        ResourceLocation equipped = ClientSpiritData.getEquippedSkill();
        if (equipped == null) return;
        SkillNode node = SkillTree.get(equipped);
        if (node == null) return;

        boolean active = ClientSpiritData.isPassiveActive(equipped) || node.isAlwaysOn();
        int borderColor = active ? 0xFF4CAF50 : 0xFFFFD54F;
        CanvasShapes.fillShape(ctx, node.getType(), 131f, 8f, 7f, borderColor, 0x80201030);
        ctx.drawString(Component.literal(node.getIcon()), 127f, 2f, 0xFFFFFFFF, false);
        ctx.drawString(Component.literal(String.valueOf(ClientSpiritData.getSelectedSlot() + 1)),
                136f, 9f, 0xFFAAAAAA, false);
    }
}
