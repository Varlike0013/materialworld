package com.varlike.aetherui.api.hud;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 3D 世界标记提供者：其它 mod 实现此接口注册后，可在世界中渲染自定义标记面板
 * （白色边框 + 黑色半透明背景 + 左侧 PNG 图标 + 右侧多行彩色文字）。
 *
 * <p>每个标记可独立设置 {@link WorldMarker#throughWall()} 决定是否穿透墙体。
 */
public interface WorldMarkerProvider {

    /** 唯一标识，用于去重与注销。 */
    ResourceLocation id();

    /**
     * 获取要渲染的世界标记。
     *
     * @param level  客户端世界
     * @param player 当前玩家
     * @return 标记列表；返回 null 或空表示无标记
     */
    List<WorldMarker> getMarkers(Level level, LocalPlayer player);
}
