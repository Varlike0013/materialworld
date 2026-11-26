package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.lang.AetherUILang;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.CrossAxisAlignment;
import com.varlike.aetherui.api.layout.MainAxisAlignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.HudBarRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 左下角 HUD。
 *
 * <p>外层用 {@code COLUMN} 统一做纵向排列与贴底对齐，内层每个耐久条目用 {@code ROW}
 * 对齐图标、名称和耐久文本；经验/饥饿/血量/自定义条作为独立 canvas 子节点加入外层列。
 * 内容变化时才重建组件树，不再手动累加 Y 偏移。
 */
public final class CustomHud {

    private static final float W = 142f;
    private static final float H = 220f;
    private static final float GAP = 2f;
    private static final float ROW_H = 20f;
    private static final float ICON = 16f;
    private static final float NAME_W = 66f;
    private static final float DURABILITY_W = 48f;
    private static final float ARMOR_W = 24f;

    private static UITree tree;
    private static String signature = "";

    private CustomHud() {}

    public static void init() {
        update();
    }

    /** 每帧检查条目集合是否变化；变化时才重建组件树。 */
    public static void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!shouldRender() || player == null) return;

        State state = collect(player);
        String nextSignature = signatureOf(state);
        if (nextSignature.equals(signature)) return;

        if (tree != null) AetherUI.destroy(tree);
        signature = nextSignature;
        tree = AetherUI.build(buildRoot(state, player), target(), CustomHud::shouldRender);
        AetherUI.register(tree);
    }

    public static void destroy() {
        if (tree != null) AetherUI.destroy(tree);
        tree = null;
        signature = "";
    }

    private static boolean shouldRender() {
        if (!Config.customHudEnabled()) return false;
        LocalPlayer p = player();
        return p != null && !p.isCreative() && !p.isSpectator();
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    private static ScreenRenderTarget target() {
        return new ScreenRenderTarget(
                ScreenAnchor.bottomLeft(Config.customHudAnchorX(), Config.customHudAnchorY()))
                .alignment(Alignment.BOTTOM_LEFT)
                .guiScale(1f);
    }

    private record State(List<ItemStack> items, List<HudBar> bars) {}

    private static State collect(LocalPlayer p) {
        List<ItemStack> items = new ArrayList<>();
        if (Config.showArmorDurability()) {
            addIfDamageable(items, armor(p, 3));
            addIfDamageable(items, armor(p, 2));
            addIfDamageable(items, armor(p, 1));
            addIfDamageable(items, armor(p, 0));
        }
        if (Config.showHeldDurability()) {
            addIfDamageable(items, p.getMainHandItem());
            addIfDamageable(items, p.getOffhandItem());
        }

        List<HudBar> bars = new ArrayList<>();
        for (HudBar bar : HudBarRegistry.all()) {
            if (bar.visible(p)) bars.add(bar);
        }
        return new State(items, bars);
    }

    private static String signatureOf(State state) {
        StringBuilder sb = new StringBuilder();
        sb.append("cfg:").append(Config.customHudAnchorX()).append(',')
                .append(Config.customHudAnchorY()).append(',')
                .append(Config.customHudBarHeight()).append('|');
        for (ItemStack stack : state.items()) {
            sb.append(stack.getHoverName().getString()).append('#')
                    .append(stack.getDamageValue()).append('/').append(stack.getMaxDamage())
                    .append('|');
        }
        for (HudBar bar : state.bars()) {
            sb.append(bar.id()).append(':')
                    .append(bar.width()).append('x').append(bar.height())
                    .append('|');
        }
        return sb.toString();
    }

    private static UIComponent buildRoot(State state, LocalPlayer p) {
        float barH = Config.customHudBarHeight();
        UIBuilder column = UIBuilder.column()
                .size(Size.fixed(W), Size.fixed(H))
                .spacing(GAP)
                .mainAxisAlignment(MainAxisAlignment.END)
                .crossAxisAlignment(CrossAxisAlignment.START);

        for (ItemStack stack : state.items()) {
            column.add(durabilityRow(stack));
        }

        column.add(UIBuilder.barCanvas(W, barH,
                () -> clamp(p.experienceProgress),
                0xFF00DD00,
                () -> {
                    float exp = clamp(p.experienceProgress);
                    return Component.literal(Component.translatable(AetherUILang.LEVEL).getString()
                            + " " + p.experienceLevel + "  " + Math.round(exp * 100) + "%");
                }));

        column.add(UIBuilder.barCanvas(W, barH,
                () -> clamp(p.getFoodData().getFoodLevel() / 20f),
                0xFFD49A3C,
                () -> Component.literal(Component.translatable(AetherUILang.FOOD).getString()
                        + " " + p.getFoodData().getFoodLevel() + "/20")));

        column.add(UIBuilder.barWithValueCanvas(W, barH,
                () -> p.getMaxHealth() > 0 ? p.getHealth() / p.getMaxHealth() : 0f,
                0xFFE03333,
                () -> Component.literal(Component.translatable(AetherUILang.HP).getString()
                        + " " + Math.round(p.getHealth()) + "/" + Math.round(p.getMaxHealth())),
                p::getArmorValue,
                ARMOR_W,
                CanvasStyles.IRON_SILVER));

        for (HudBar bar : state.bars()) {
            column.add(UIBuilder.barCanvas(bar.width(), bar.height(), ctx -> bar.paint(ctx, p)));
        }
        return column.build();
    }

    /** 单个耐久条目：canvas 画底板，内部 ROW 自动对齐图标/名称/耐久文字。 */
    private static UIBuilder durabilityRow(ItemStack stack) {
        int max = stack.getMaxDamage();
        int left = Math.max(0, max - stack.getDamageValue());
        float ratio = max > 0 ? (float) left / max : 0f;

        UIBuilder slot = UIBuilder.canvas(Size.fixed(W), Size.fixed(ROW_H))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(ctx -> CanvasStyles.darkSlot(ctx, 0f, 0f, W, ROW_H));

        UIBuilder row = UIBuilder.row()
                .x(2f).y(2f)
                .spacing(4f)
                .crossAxisAlignment(CrossAxisAlignment.CENTER)
                .add(UIBuilder.canvas(Size.fixed(ICON), Size.fixed(ICON))
                        .canvasAnchor(Alignment.TOP_LEFT)
                        .painter(ctx -> ctx.renderItem(stack, 0f, 0f)))
                .add(UIBuilder.label(stack.getHoverName())
                        .size(Size.fixed(NAME_W), Size.fixed(ROW_H - 4f))
                        .textColor(0xFFFFFFFF))
                .add(UIBuilder.label(Component.literal(left + " / " + max))
                        .size(Size.fixed(DURABILITY_W), Size.fixed(ROW_H - 4f))
                        .textColor(durabilityColor(ratio)));

        return slot.add(row);
    }

    private static void addIfDamageable(List<ItemStack> out, ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) out.add(stack);
    }

    private static ItemStack armor(LocalPlayer p, int slot) {
        return p.getInventory().getArmor(slot);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int durabilityColor(float ratio) {
        return ratio > 0.6f ? 0xFF55FF55 : (ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555);
    }
}
