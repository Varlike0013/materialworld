package com.varlike.aetherui.api;

import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.hud.RecipeInfoProvider;
import com.varlike.aetherui.api.hud.WorldMarkerProvider;
import com.varlike.aetherui.api.target.RenderTarget;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import com.varlike.aetherui.core.registry.BlockInfoRegistry;
import com.varlike.aetherui.core.registry.RecipeInfoRegistry;
import com.varlike.aetherui.core.registry.BuffIconRegistry;
import com.varlike.aetherui.core.registry.HudBarRegistry;
import com.varlike.aetherui.core.registry.ScreenUIRegistry;
import com.varlike.aetherui.core.registry.WorldUIRegistry;
import com.varlike.aetherui.core.registry.WorldMarkerRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public final class AetherUI {

    private AetherUI() {}

    /** 从不可变组件树构建 UI。 */
    public static UITree build(UIComponent root, RenderTarget target) {
        if (root == null || target == null) throw new IllegalArgumentException("root and target required");
        return new UITree(root, target);
    }

    /** 带可见性谓词的构建：谓词返回 false 时该树不参与渲染。 */
    public static UITree build(UIComponent root, RenderTarget target, BooleanSupplier visible) {
        return new UITree(root, target, visible);
    }

    /**
     * 一次声明，两处绘制：用同一个 {@code root} 同时构建并注册一棵屏幕树和一棵世界树。
     * 样式只写一次，即可同步出现在屏幕 HUD 与世界空间（如 Boss 头顶）。
     */
    public static DualTree buildAndRegisterBoth(UIComponent root,
                                                ScreenRenderTarget screenTarget,
                                                WorldRenderTarget worldTarget) {
        return buildAndRegisterBoth(root, screenTarget, worldTarget, () -> true);
    }

    /** 同上，带可见性谓词；谓词同时作用于屏幕树与世界树。 */
    public static DualTree buildAndRegisterBoth(UIComponent root,
                                                ScreenRenderTarget screenTarget,
                                                WorldRenderTarget worldTarget,
                                                BooleanSupplier visible) {
        if (root == null || screenTarget == null || worldTarget == null)
            throw new IllegalArgumentException("root, screenTarget and worldTarget required");
        UITree screen = register(new UITree(root, screenTarget, visible));
        UITree world = register(new UITree(root, worldTarget, visible));
        return new DualTree(screen, world);
    }

    /** 注册一棵 UI 树，使其参与每帧渲染。重复注册同一实例是安全的。 */
    public static UITree register(UITree tree) {
        if (tree == null) throw new IllegalArgumentException("tree required");
        if (tree.target() instanceof WorldRenderTarget) {
            WorldUIRegistry.add(tree);
        } else if (tree.target() instanceof ScreenRenderTarget) {
            ScreenUIRegistry.add(tree);
        }
        return tree;
    }

    /** 注销一棵 UI 树，使其不再参与渲染。 */
    public static void destroy(UITree tree) {
        if (tree == null) return;
        com.varlike.aetherui.core.tick.ClientTickHandler.cancelDestroy(tree);
        if (tree.target() instanceof WorldRenderTarget) {
            WorldUIRegistry.remove(tree);
        } else if (tree.target() instanceof ScreenRenderTarget) {
            ScreenUIRegistry.remove(tree);
        }
    }

    /** 清空当前所有已注册的屏幕 UI（主要用于测试与热重载）。 */
    public static void destroyAllScreenUIs() {
        ScreenUIRegistry.clear();
    }

    /**
     * 在 durationMs 后自动注销并释放这棵树（由客户端 tick 驱动）。
     * 到期后树会从注册表移除，其持有的组件与 lambda 捕获链不再被强引用，可被 GC。
     * 传负数表示不自动销毁（需手动 {@link #destroy(UITree)}）。
     */
    public static void destroyLater(UITree tree, long durationMs) {
        if (tree == null || durationMs < 0L) return;
        com.varlike.aetherui.core.tick.ClientTickHandler.scheduleDestroy(tree, durationMs);
    }

    // ---------------- 自定义 HUD 条扩展 ----------------

    /** 注册一条自定义 HUD 条，渲染在左下角 HUD 的饥饿条下方。重复 id 会抛异常。 */
    public static void registerHudBar(HudBar bar) {
        HudBarRegistry.register(bar);
    }

    /** 注销一条自定义 HUD 条。 */
    public static void unregisterHudBar(HudBar bar) {
        HudBarRegistry.unregister(bar);
    }

    /** 按 id 注销一条自定义 HUD 条。 */
    public static void unregisterHudBar(ResourceLocation id) {
        HudBarRegistry.unregister(id);
    }

    /** 清空所有已注册的自定义 HUD 条。 */
    public static void clearHudBars() {
        HudBarRegistry.clear();
    }

    // ---------------- Buff 图标扩展 ----------------

    /** 注册一个 buff 图标，绘制在物品栏上方。重复 id 会抛异常。 */
    public static void registerBuffIcon(BuffIcon icon) {
        BuffIconRegistry.register(icon);
    }

    /** 一行注册 17×17 文字 Buff 图标（深色背景 + 1px 边框 + 居中文字）。 */
    public static BuffIcon addTextBuffIcon(ResourceLocation id, String label, String text) {
        BuffIcon icon = BuffIcon.textBadge(id, label, text);
        registerBuffIcon(icon);
        return icon;
    }

    /** 同上，并绘制罗马数字层数；传 0 或不传的版本不绘制层数。 */
    public static BuffIcon addTextBuffIcon(ResourceLocation id, String label, String text, int level) {
        BuffIcon icon = BuffIcon.textBadge(id, label, text, level);
        registerBuffIcon(icon);
        return icon;
    }

    /** 一行注册 17×17 PNG Buff 图标（16×16 PNG + 1px 边框）。 */
    public static BuffIcon addPngBuffIcon(ResourceLocation id, String label, ResourceLocation texture) {
        BuffIcon icon = BuffIcon.pngBadge(id, label, texture);
        registerBuffIcon(icon);
        return icon;
    }

    /** 同上，并绘制罗马数字层数。 */
    public static BuffIcon addPngBuffIcon(ResourceLocation id, String label,
                                          ResourceLocation texture, int level) {
        BuffIcon icon = BuffIcon.pngBadge(id, label, texture, level);
        registerBuffIcon(icon);
        return icon;
    }

    /** 注销一个 buff 图标。 */
    public static void unregisterBuffIcon(BuffIcon icon) {
        BuffIconRegistry.unregister(icon);
    }

    /** 按 id 注销一个 buff 图标。 */
    public static void unregisterBuffIcon(ResourceLocation id) {
        BuffIconRegistry.unregister(id);
    }

    /** 清空所有已注册的 buff 图标。 */
    public static void clearBuffIcons() {
        BuffIconRegistry.clear();
    }

    // ---------------- 方块信息扩展 ----------------

    /** 注册一个方块信息提供者，瞄准匹配方块时在右下角 HUD 显示其信息行。 */
    public static void registerBlockInfoProvider(BlockInfoProvider provider) {
        BlockInfoRegistry.register(provider);
    }

    /** 注销一个方块信息提供者。 */
    public static void unregisterBlockInfoProvider(BlockInfoProvider provider) {
        BlockInfoRegistry.unregister(provider);
    }

    /** 按 id 注销一个方块信息提供者。 */
    public static void unregisterBlockInfoProvider(ResourceLocation id) {
        BlockInfoRegistry.unregister(id);
    }

    /** 清空所有已注册的方块信息提供者。 */
    public static void clearBlockInfoProviders() {
        BlockInfoRegistry.clear();
    }

    // ---------------- 配方信息扩展 ----------------

    /** 注册一个配方信息提供者，在右下角 HUD 显示 [材料] → [时间] → [产物]。重复 id 会抛异常。 */
    public static void registerRecipeInfoProvider(RecipeInfoProvider provider) {
        RecipeInfoRegistry.register(provider);
    }

    /** 注销一个配方信息提供者。 */
    public static void unregisterRecipeInfoProvider(RecipeInfoProvider provider) {
        RecipeInfoRegistry.unregister(provider);
    }

    /** 按 id 注销一个配方信息提供者。 */
    public static void unregisterRecipeInfoProvider(ResourceLocation id) {
        RecipeInfoRegistry.unregister(id);
    }

    /** 清空所有已注册的配方信息提供者。 */
    public static void clearRecipeInfoProviders() {
        RecipeInfoRegistry.clear();
    }

    // ---------------- 3D 世界标记扩展 ----------------

    /** 注册一个 3D 世界标记提供者，在世界中绘制图标+文字面板。 */
    public static void registerWorldMarker(WorldMarkerProvider provider) {
        WorldMarkerRegistry.register(provider);
    }

    /** 注销一个 3D 世界标记提供者。 */
    public static void unregisterWorldMarker(WorldMarkerProvider provider) {
        WorldMarkerRegistry.unregister(provider);
    }

    /** 按 id 注销一个 3D 世界标记提供者。 */
    public static void unregisterWorldMarker(ResourceLocation id) {
        WorldMarkerRegistry.unregister(id);
    }

    /** 清空所有已注册的 3D 世界标记提供者。 */
    public static void clearWorldMarkers() {
        WorldMarkerRegistry.clear();
    }
}
