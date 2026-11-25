package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.spiritworld.ClientConfig;
import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.varlike.spiritworld.SpiritWorld.MOD_ID;

/**
 * 精神力条：直接复用 AetherUI 血条的 {@code barWithValue} 样式。
 * 左侧 142×12 与血条同宽同高，右侧 24 像素数值方块显示灵视等级，
 * 更右侧额外保留状态/技能图标区域。
 */
public final class SpiritHudBar implements HudBar {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "spirit_bar");

    private static final float HUD_W = 178f;
    private static final float HUD_H = 12f;

    private static final float BAR_X = 0f;
    private static final float BAR_Y = 0f;
    private static final float BAR_W = 142f;
    private static final float BAR_H = 12f;
    private static final float VISION_BOX_W = 24f;
    private static final int BAR_COLOR = 0xFF3399FF;

    private static final float STATUS_X = 154f;
    private static final float FLIGHT_Y = 3.5f;
    private static final float SKILL_Y = 8.5f;
    private static final float STATUS_R = 3.5f;
    private static final float SLOT_X = 165f;
    private static final float SLOT_Y = 2f;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public float width() {
        return HUD_W;
    }

    @Override
    public float height() {
        return HUD_H;
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
        float ratio = max > 0L ? Math.clamp((float) spirit / (float) max, 0f, 1f) : 0f;

        // 与 AetherUI 血条完全一致：深色槽 + 单色填充 + 条内标签 + 右侧数值方块
        CanvasStyles.barWithValue(ctx, BAR_X, BAR_Y, BAR_W, BAR_H, ratio,
                BAR_COLOR, null, ClientSpiritData.getVisionLevel(), VISION_BOX_W);

        // 被扣掉的上限：红色段，仅覆盖条内部，不触碰数值方块
        long baseMax = ClientSpiritData.getBaseMaxSpirit();
        float barW = BAR_W - VISION_BOX_W - 2f;
        float innerW = Math.max(0f, barW - 4f);
        if (baseMax > max && baseMax > 0L && innerW > 0f) {
            float redStart = innerW * ((float) max / (float) baseMax);
            if (redStart < innerW) {
                ctx.fill(BAR_X + 2f + redStart, BAR_Y + 2f, innerW - redStart, BAR_H - 4f, 0xFF8B1A1A);
                ctx.fill(BAR_X + 2f + redStart, BAR_Y + 2f, innerW - redStart, (BAR_H - 4f) / 2f, 0xFFCC4444);
            }
        }

        // 魔力值文字：与血条 HP 文本同位置、同阴影
        if (ClientConfig.showSpiritText) {
            Component text = Component.literal("魔力 " + spirit + " / " + max);
            ctx.drawString(text, BAR_X + 4f, BAR_Y + 1f, 0xFFFFFFFF, true);
        }

        drawFlightIndicator(ctx, player);
        drawEquippedSkill(ctx);
    }

    /** 灵视等级右侧上方：飞行状态图标。 */
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

        CanvasShapes.fillCircle(ctx, STATUS_X, FLIGHT_Y, STATUS_R, borderColor);
        CanvasShapes.fillCircle(ctx, STATUS_X, FLIGHT_Y, STATUS_R - 2f, 0x80201030);
        ctx.drawString(Component.literal("飞"), STATUS_X - 3f, FLIGHT_Y - 4f, 0xFFFFFFFF, false);
    }

    /** 灵视等级右侧下方：当前装配技能图标 + 槽位编号。 */
    private static void drawEquippedSkill(CanvasContext ctx) {
        ResourceLocation equipped = ClientSpiritData.getEquippedSkill();
        if (equipped == null) return;
        SkillNode node = SkillTree.get(equipped);
        if (node == null) return;

        boolean active = ClientSpiritData.isPassiveActive(equipped) || node.isAlwaysOn();
        int borderColor = active ? 0xFF4CAF50 : 0xFFFFD54F;
        CanvasShapes.fillShape(ctx, node.getType(), STATUS_X, SKILL_Y, STATUS_R, borderColor, 0x80201030);
        ctx.drawString(Component.literal(node.getIcon()), STATUS_X - 3f, SKILL_Y - 4f, 0xFFFFFFFF, false);
        ctx.drawString(Component.literal(String.valueOf(ClientSpiritData.getSelectedSlot() + 1)),
                SLOT_X, SLOT_Y, 0xFFAAAAAA, false);
    }
}
