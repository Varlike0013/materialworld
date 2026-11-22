package com.varlike.aetherui.api;

import com.varlike.aetherui.api.element.Panel;
import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.hud.WorldMarkerProvider;
import com.varlike.aetherui.api.target.RenderTarget;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import com.varlike.aetherui.core.registry.BlockInfoRegistry;
import com.varlike.aetherui.core.registry.BuffIconRegistry;
import com.varlike.aetherui.core.registry.HudBarRegistry;
import com.varlike.aetherui.core.registry.ScreenUIRegistry;
import com.varlike.aetherui.core.registry.WorldMarkerRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public final class AetherUI {

    private AetherUI() {}

    public static Panel panel() { return new Panel(); }

    /** 新 API：从不可变组件树构建 UI。 */
    public static UITree build(UIComponent root, RenderTarget target) {
        if (root == null || target == null) throw new IllegalArgumentException("root and target required");
        return new UITree(root, target);
    }

    /** 带可见性谓词的构建：谓词返回 false 时该树不参与渲染。 */
    public static UITree build(UIComponent root, RenderTarget target, BooleanSupplier visible) {
        return new UITree(root, target, visible);
    }

    /** 旧 API 兼容入口。 */
    public static UITree build(Panel root, RenderTarget target) {
        if (root == null || target == null) throw new IllegalArgumentException("root and target required");
        return new UITree(root, target);
    }

    /** 注册一棵 UI 树，使其参与每帧渲染。重复注册同一实例是安全的。 */
    public static UITree register(UITree tree) {
        if (tree == null) throw new IllegalArgumentException("tree required");
        if (tree.target() instanceof WorldRenderTarget) {
            // Phase 4：WorldUIRegistry.add(tree)
        } else if (tree.target() instanceof ScreenRenderTarget) {
            ScreenUIRegistry.add(tree);
        }
        return tree;
    }

    /** 注销一棵 UI 树，使其不再参与渲染。 */
    public static void destroy(UITree tree) {
        if (tree == null) return;
        if (tree.target() instanceof WorldRenderTarget) {
            // Phase 4：WorldUIRegistry.remove(tree)
        } else if (tree.target() instanceof ScreenRenderTarget) {
            ScreenUIRegistry.remove(tree);
        }
    }

    /** 清空当前所有已注册的屏幕 UI（主要用于测试与热重载）。 */
    public static void destroyAllScreenUIs() {
        ScreenUIRegistry.clear();
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
