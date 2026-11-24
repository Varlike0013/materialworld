package com.varlike.oathbreaker.client;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.oathbreaker.core.network.PlayerStatsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * LoL 风格 HUD，基于 AetherUI 的声明式组件树实现。
 *
 * <p>面板固定左上，内容只读 {@link ClientStatsCache}（服务端下发的
 * {@link PlayerStatsPayload}），客户端不自行计算面板值。相比旧版纯 GuiGraphics
 * 实现，新增穿透、吸血、回复等字段，并复用 AetherUI 的 CanvasStyles 样式。</p>
 *
 * <p>瞄准信息、Buff 图标与 3D 血条由 AetherUI 内置 HUD 提供；
 * 若希望只保留本 Mod 的 LoL 面板，可在 AetherUI 配置中关闭 enableCustomHud。</p>
 */
public final class OathbreakerHud {
    private static final float PANEL_WIDTH = 230F;
    private static final float PANEL_HEIGHT = 126F;
    private static final float COLUMN_LEFT = 6F;
    private static final float COLUMN_RIGHT = 120F;

    private static final int COLOR_TITLE = 0xFFFFD479;
    private static final int COLOR_HP = 0xFFE03333;
    private static final int COLOR_MANA = 0xFF3388FF;
    private static final int COLOR_AD = 0xFFFFCC66;
    private static final int COLOR_AP = 0xFF88CCFF;
    private static final int COLOR_ARMOR = 0xFFD0D0D0;
    private static final int COLOR_MR = 0xFFCC88FF;
    private static final int COLOR_CRIT = 0xFFFF8888;
    private static final int COLOR_SPEED = 0xFF88FF88;
    private static final int COLOR_HASTE = 0xFF88FFFF;
    private static final int COLOR_PEN = 0xFFFFD479;
    private static final int COLOR_VAMP = 0xFFFF88CC;
    private static final int COLOR_REGEN = 0xFF88FFAA;
    private static final int COLOR_SECONDARY = 0xFFAAAAAA;

    private static UITree tree;

    private OathbreakerHud() {}

    /** 替换原版生命/护甲/饥饿层；服务端逻辑保持不变。 */
    public static void registerVanillaLayers(RegisterGuiLayersEvent event) {
        event.replaceLayer(VanillaGuiLayers.PLAYER_HEALTH, (graphics, delta) -> {});
        event.replaceLayer(VanillaGuiLayers.ARMOR_LEVEL, (graphics, delta) -> {});
        event.replaceLayer(VanillaGuiLayers.FOOD_LEVEL, (graphics, delta) -> {});
    }

    /** 在客户端初始化阶段构建并注册 AetherUI 面板。 */
    public static void registerAetherUi() {
        if (tree != null) {
            return;
        }
        tree = AetherUI.register(AetherUI.build(
                UIBuilder.canvas(Size.fixed(PANEL_WIDTH), Size.fixed(PANEL_HEIGHT))
                        .canvasAnchor(Alignment.TOP_LEFT)
                        .painter(OathbreakerHud::paint)
                        .build(),
                new ScreenRenderTarget(ScreenAnchor.topLeft(8F, 8F))
                        .alignment(Alignment.TOP_LEFT),
                OathbreakerHud::isVisible
        ));
    }

    private static boolean isVisible() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && !minecraft.options.hideGui
                && !minecraft.player.isSpectator()
                && ClientStatsCache.get() != null;
    }

    private static void paint(CanvasContext ctx) {
        PlayerStatsPayload s = ClientStatsCache.get();
        if (s == null) {
            return;
        }

        CanvasStyles.panel(ctx, 0F, 0F, ctx.width(), ctx.height());
        ctx.drawString(Component.literal("OATHBREAKER"), COLUMN_LEFT, 4F, COLOR_TITLE, true);

        CanvasStyles.bar(ctx, COLUMN_LEFT, 16F, PANEL_WIDTH - 12F, 12F,
                ratio(s.health(), s.maxHealth()), COLOR_HP,
                Component.literal("HP " + one(s.health()) + "/" + one(s.maxHealth())));
        CanvasStyles.bar(ctx, COLUMN_LEFT, 30F, PANEL_WIDTH - 12F, 12F,
                ratio(s.mana(), s.maxMana()), COLOR_MANA,
                Component.literal("MP " + whole(s.mana()) + "/" + whole(s.maxMana())));

        cell(ctx, "AD " + one(s.attackDamage()), COLUMN_LEFT, 48F, COLOR_AD);
        cell(ctx, "AP " + one(s.abilityPower()), COLUMN_RIGHT, 48F, COLOR_AP);
        cell(ctx, "ARM " + one(s.armor()), COLUMN_LEFT, 59F, COLOR_ARMOR);
        cell(ctx, "MR " + one(s.magicResist()), COLUMN_RIGHT, 59F, COLOR_MR);
        cell(ctx, "CRIT " + percent(s.critChance()) + " x" + two(s.critDamage()),
                COLUMN_LEFT, 70F, COLOR_CRIT);
        cell(ctx, "AS " + two(lolAttackSpeed(s.attackSpeed())), COLUMN_RIGHT, 70F, COLOR_SPEED);
        cell(ctx, "AH " + whole(s.abilityHaste()) + " (" + percent(hasteToCdr(s.abilityHaste())) + ")",
                COLUMN_LEFT, 81F, COLOR_HASTE);
        cell(ctx, "FOOD " + s.foodLevel(), COLUMN_RIGHT, 81F, COLOR_SECONDARY);
        cell(ctx, "APEN " + one(s.armorPenetrationFlat()) + " / " + percent(s.armorPenetrationPercent()),
                COLUMN_LEFT, 92F, COLOR_PEN);
        cell(ctx, "MPEN " + one(s.magicPenetrationFlat()) + " / " + percent(s.magicPenetrationPercent()),
                COLUMN_RIGHT, 92F, COLOR_PEN);
        cell(ctx, "LS " + percent(s.lifesteal()), COLUMN_LEFT, 103F, COLOR_VAMP);
        cell(ctx, "OMNI " + percent(s.omnivamp()), COLUMN_RIGHT, 103F, COLOR_VAMP);
        cell(ctx, "HP5 " + one(s.healthRegeneration()), COLUMN_LEFT, 114F, COLOR_REGEN);
        cell(ctx, "MP5 " + one(s.manaRegeneration()), COLUMN_RIGHT, 114F, COLOR_REGEN);
    }

    private static void cell(CanvasContext ctx, String text, float x, float y, int color) {
        ctx.drawString(Component.literal(text), x, y, color, true);
    }

    private static float ratio(float value, float max) {
        if (max <= 0F) {
            return 0F;
        }
        return Math.max(0F, Math.min(1F, value / max));
    }

    private static float lolAttackSpeed(float minecraftAttackSpeed) {
        return minecraftAttackSpeed / 4.0F * 0.625F;
    }

    private static float hasteToCdr(float abilityHaste) {
        return abilityHaste / (100F + abilityHaste);
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
