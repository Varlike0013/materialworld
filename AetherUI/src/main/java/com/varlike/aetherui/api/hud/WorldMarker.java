package com.varlike.aetherui.api.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 3D 世界标记数据：在世界坐标处渲染一个带物品图标与多行文字的面板。
 *
 * @param id          唯一标识，用于生命周期跟踪（同一 id 的标记共享持续时间计时）
 * @param position    世界坐标（面板锚点）
 * @param icon        左侧物品图标的 PNG 纹理（可为 null 表示不显示图标）
 * @param lines       右侧文字行，每行的 Component 样式决定颜色/粗体等
 * @param throughWall 是否穿透墙体渲染（false 时被方块遮挡则不显示）
 * @param durationMs  持续时间（毫秒）。大于 0 时标记在此时长后自动消失；小于等于 0 表示永久显示。
 */
public record WorldMarker(String id, Vec3 position, ResourceLocation icon,
                          List<Component> lines, boolean throughWall, long durationMs) {

    public WorldMarker {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id required");
        if (position == null) throw new IllegalArgumentException("position required");
        if (lines == null) lines = List.of();
        lines = List.copyOf(lines);
    }

    public static final long PERMANENT = -1L;

    /** 永久显示的标记，id 由坐标生成。 */
    public static WorldMarker of(Vec3 position, ResourceLocation icon,
                                 boolean throughWall, Component... lines) {
        return of(keyOf(position), position, icon, throughWall, lines);
    }

    /** 永久显示的标记，使用自定义 id。 */
    public static WorldMarker of(String id, Vec3 position, ResourceLocation icon,
                                 boolean throughWall, Component... lines) {
        return new WorldMarker(id, position, icon, List.of(lines), throughWall, PERMANENT);
    }

    private static String keyOf(Vec3 p) {
        return String.format("%.2f,%.2f,%.2f", p.x, p.y, p.z);
    }

    /** 带持续时间的标记。 */
    public static WorldMarker timed(String id, Vec3 position, ResourceLocation icon,
                                    boolean throughWall, long durationMs, Component... lines) {
        return new WorldMarker(id, position, icon, List.of(lines), throughWall, durationMs);
    }
}
