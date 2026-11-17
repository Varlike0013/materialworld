package com.varlike.oathbreaker.client;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.core.network.PlayerStatsPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * LoL 风格 HUD。
 *
 * <p>移除原版生命/护甲/饥饿条，改为左上角面板：生命条、法力条、
 * AD/AP、护甲/魔抗、暴击、攻速、技能急速与饥饿值。</p>
 *
 * <p>攻速显示做了 Minecraft → LoL 的展示换算（MC 4.0 ≈ LoL 0.625），
 * 只影响显示，不改变实际攻击冷却。</p>
 */
public final class OathbreakerHud implements LayeredDraw.Layer {
    private static final float MC_BASE_ATTACK_SPEED = 4.0F;
    private static final float LOL_BASE_ATTACK_SPEED = 0.625F;

    public static void register(RegisterGuiLayersEvent event) {
        // 原版血条/护甲条/饥饿条不再渲染；服务端逻辑保持不变。
        event.replaceLayer(VanillaGuiLayers.PLAYER_HEALTH, (graphics, delta) -> {});
        event.replaceLayer(VanillaGuiLayers.ARMOR_LEVEL, (graphics, delta) -> {});
        event.replaceLayer(VanillaGuiLayers.FOOD_LEVEL, (graphics, delta) -> {});
        event.registerAboveAll(Oathbreaker.id("hud"), new OathbreakerHud());
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.player.isSpectator()) {
            return;
        }
        PlayerStatsPayload stats = ClientStatsCache.get();
        if (stats == null) {
            return;
        }

        Font font = minecraft.font;
        int x = 8;
        int y = 8;
        int width = 200;
        int height = 80;

        graphics.fill(x, y, x + width, y + height, 0x99000000);
        graphics.renderOutline(x, y, width, height, 0xFF3A3A3A);

        drawBar(graphics, x + 6, y + 8, 120, 7, ratio(stats.health(), stats.maxHealth()), 0xFFCC3333);
        graphics.drawString(font, "HP " + one(stats.health()) + "/" + one(stats.maxHealth()),
                x + 132, y + 7, 0xFFFFFFFF, true);

        drawBar(graphics, x + 6, y + 19, 120, 7, ratio(stats.mana(), stats.maxMana()), 0xFF3366CC);
        graphics.drawString(font, "MANA " + whole(stats.mana()) + "/" + whole(stats.maxMana()),
                x + 132, y + 18, 0xFFFFFFFF, true);

        graphics.drawString(font, "AD " + one(stats.attackDamage()) + "   AP " + one(stats.abilityPower()),
                x + 6, y + 32, 0xFFFFFFFF, true);
        graphics.drawString(font, "ARMOR " + one(stats.armor()) + "   MR " + one(stats.magicResist()),
                x + 6, y + 42, 0xFFFFFFFF, true);
        graphics.drawString(font, "CRIT " + percent(stats.critChance()) + " (" + percent(stats.critDamage()) + ")"
                        + "   AS " + two(lolAttackSpeed(stats.attackSpeed())),
                x + 6, y + 52, 0xFFFFFFFF, true);
        graphics.drawString(font, "AH " + whole(stats.abilityHaste()) + "   FOOD " + stats.foodLevel(),
                x + 6, y + 62, 0xFFAAAAAA, true);
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float ratio, int color) {
        graphics.fill(x, y, x + width, y + height, 0xFF202020);
        int filled = (int) (width * Mth.clamp(ratio, 0.0F, 1.0F));
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + height, color);
        }
    }

    private static float ratio(float value, float max) {
        return max <= 0.0F ? 0.0F : value / max;
    }

    private static float lolAttackSpeed(float minecraftAttackSpeed) {
        return minecraftAttackSpeed / MC_BASE_ATTACK_SPEED * LOL_BASE_ATTACK_SPEED;
    }

    private static String whole(float value) {
        return Integer.toString(Math.round(value));
    }

    private static String one(float value) {
        return Double.toString(Math.round(value * 10.0F) / 10.0D);
    }

    private static String two(float value) {
        return Double.toString(Math.round(value * 100.0F) / 100.0D);
    }

    private static String percent(float fraction) {
        return Math.round(fraction * 100.0F) + "%";
    }
}
