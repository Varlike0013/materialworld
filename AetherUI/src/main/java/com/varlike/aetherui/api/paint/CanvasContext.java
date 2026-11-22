package com.varlike.aetherui.api.paint;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface CanvasContext {

    /** Canvas 的逻辑像素宽度。 */
    float width();

    /** Canvas 的逻辑像素高度。 */
    float height();

    /**
     * 2D 后端返回真实 GuiGraphics，其 PoseStack 已对齐到 Canvas 锚点并应用 guiScale。
     * 3D 后端返回 null。
     * <p>
     * 使用此方法意味着该 painter 依赖原版屏幕空间绘制能力，仅在 2D 后端生效。
     */
    @Nullable GuiGraphics guiGraphics();

    // ---------------- 两端统一的绘制原语 ----------------

    /** 填充矩形。坐标相对 Canvas 锚点，单位为逻辑像素。 */
    void fill(float x, float y, float w, float h, int argb);

    /**
     * 绘制纹理区域。坐标相对 Canvas 锚点，单位为逻辑像素。
     * 纹理为完整 ResourceLocation；图集子区域由 u/v/uw/uh 指定（归一化 UV）。
     */
    void blit(ResourceLocation texture,
              float x, float y, float w, float h,
              float u, float v, float uw, float uh,
              int tintArgb);

    /** 绘制图集中的 sprite（按 sprite 名称）。坐标为逻辑像素。 */
    void blitSprite(ResourceLocation sprite, float x, float y, float w, float h);

    /** 绘制已解析的图集 sprite 实例（例如状态效果图标）。坐标为逻辑像素。 */
    void blitAtlasSprite(TextureAtlasSprite sprite, float x, float y, float w, float h);

    /** 绘制文字。坐标相对 Canvas 锚点，单位为逻辑像素。 */
    void drawString(Component text, float x, float y, int color, boolean shadow);

    /**
     * 绘制物品图标（16×16 逻辑像素左上角对齐）。
     * 3D 后端可返回空实现并提示用户。
     */
    void renderItem(ItemStack stack, float x, float y);
}