package com.varlike.aetherui.api.hud;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.Anchor;
import com.varlike.aetherui.api.target.Orientation;
import com.varlike.aetherui.api.target.WorldRenderTarget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 方块上方图标：一站式绘制"边框 + 背景 + 图片"，并可让位置随时间/函数浮动、
 * 让图标绕多个轴旋转（如熔炼产物悬浮转圈展示）。
 *
 * <p>可配置项：
 * <ul>
 *     <li>朝向 {@link Orientation}：面向玩家（CENTER，默认）、竖直（VERTICAL）、
 *         固定世界朝向（fixed(yaw,pitch)）——满足"始终面向某个特定方向"的需求；</li>
 *     <li>自旋 {@link Spin}：yaw / pitch / roll 三轴独立，每帧按时间求值；</li>
 *     <li>位置 {@link PositionFunction}：每帧 (t, base) → 世界坐标，可做上下浮动。</li>
 * </ul>
 *
 * <p>使用示例（熔炼产物，上下浮动 + 平面转圈）：
 * <pre>{@code
 * WorldIcon.show(
 *     new Vec3(x + 0.5, y + 1.6, z + 0.5),   // 方块上方
 *     outputTexture, 32f,
 *     Orientation.CENTER,                     // 始终面向玩家
 *     (t, base) -> base.add(0, 0.12 * Math.sin(t * 3), 0),  // 位置浮动
 *     Spin.roll(t -> t * 60f)                 // 绕屏幕法线转圈
 * );
 * }</pre>
 */
public final class WorldIcon {

    private static final LongSupplier DEFAULT_CLOCK = System::currentTimeMillis;
    private static final float DEFAULT_PIXELS_PER_BLOCK = 64f;

    private WorldIcon() {}

    /**
     * 完整配置：朝向 + 位置函数 + 三轴自旋 + 持续时间。
     *
     * @param basePos   基准世界坐标（通常取方块中心上方）
     * @param texture   图片纹理（PNG），传 null 则只画边框+背景
     * @param size      外框尺寸（正方形，逻辑像素）
     * @param orientation 面板朝向；传 null 视为 CENTER（面向玩家）
     * @param positionFn  (t秒, 基准坐标) → 实际坐标；传 null 则固定在 basePos
     * @param spin       三轴自旋；传 null 视为不旋转
     * @param durationMs 持续时间（毫秒），负数表示永久
     * @return 已注册的树，销毁时调用 {@link AetherUI#destroy(UITree)}
     */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size,
                              Orientation orientation,
                              PositionFunction positionFn,
                              Spin spin,
                              long durationMs) {
        if (basePos == null) throw new IllegalArgumentException("basePos required");
        if (size <= 0f) throw new IllegalArgumentException("size must be > 0");

        final long birth = DEFAULT_CLOCK.getAsLong();

        // 动态位置：每帧 = 基准 + 浮动函数(t, base)
        Anchor anchor = Anchor.dynamic(() -> {
            float t = elapsedSeconds(birth);
            Vec3 pos = positionFn != null ? positionFn.apply(t, basePos) : basePos;
            return pos != null ? pos : basePos;
        });

        // 组件：边框 + 背景 + 图片（framedImage）
        UIComponent component = UIBuilder.canvas(Size.fixed(size), Size.fixed(size))
                .canvasAnchor(Alignment.CENTER)
                .painter(ctx -> {
                    if (expired(birth, durationMs)) return;
                    CanvasStyles.framedImage(ctx, texture, 0f, 0f, size);
                })
                .build();

        // 三轴自旋：每帧求值，到期归零
        Spin effectiveSpin = spin != null ? spin : Spin.none();
        Supplier<Float> yaw = spinSupplier(effectiveSpin.yaw(), birth, durationMs);
        Supplier<Float> pitch = spinSupplier(effectiveSpin.pitch(), birth, durationMs);
        Supplier<Float> roll = spinSupplier(effectiveSpin.roll(), birth, durationMs);

        WorldRenderTarget target = new WorldRenderTarget(anchor)
                .alignment(Alignment.CENTER)
                .pixelsPerBlock(DEFAULT_PIXELS_PER_BLOCK)
                .seeThrough(true)
                .orientation(orientation != null ? orientation : Orientation.CENTER)
                .yawDegrees(yaw)
                .pitchDegrees(pitch)
                .rollDegrees(roll);

        UITree tree = AetherUI.build(component, target);
        AetherUI.register(tree);
        // 有持续时间时安排自动注销：到期真正从注册表移除，释放 lambda 捕获链
        if (durationMs >= 0L) AetherUI.destroyLater(tree, durationMs);
        return tree;
    }

    /** 永久显示版本。 */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size,
                              Orientation orientation,
                              PositionFunction positionFn,
                              Spin spin) {
        return show(basePos, texture, size, orientation, positionFn, spin, -1L);
    }

    /**
     * 旧 API 兼容：单轴 roll 旋转（平面转圈，始终面向玩家）。
     * rotationFn (t秒) → 度数；传 null 不旋转。
     */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size,
                              PositionFunction positionFn,
                              Function<Float, Float> rotationFn) {
        Spin spin = rotationFn != null ? Spin.roll(rotationFn) : Spin.none();
        return show(basePos, texture, size, Orientation.CENTER, positionFn, spin, -1L);
    }

    /** 旧 API 兼容：可指定持续时间。 */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size,
                              PositionFunction positionFn,
                              Function<Float, Float> rotationFn,
                              long durationMs) {
        Spin spin = rotationFn != null ? Spin.roll(rotationFn) : Spin.none();
        return show(basePos, texture, size, Orientation.CENTER, positionFn, spin, durationMs);
    }

    /** 旧 API 兼容：固定位置 + roll 旋转。 */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size,
                              Function<Float, Float> rotationFn) {
        return show(basePos, texture, size, (PositionFunction) null, rotationFn);
    }

    /** 固定位置、不旋转（纯静态边框+背景+图片）。 */
    public static UITree show(Vec3 basePos, ResourceLocation texture, float size) {
        return show(basePos, texture, size, (PositionFunction) null, (Function<Float, Float>) null);
    }

    /** 位置函数：t 为经过秒数，base 为基准坐标，返回实际世界坐标。 */
    @FunctionalInterface
    public interface PositionFunction {
        Vec3 apply(float t, Vec3 base);
    }

    private static Supplier<Float> spinSupplier(Function<Float, Float> fn,
                                                long birth, long durationMs) {
        if (fn == null) return () -> 0f;
        return () -> {
            if (expired(birth, durationMs)) return 0f;
            Float v = fn.apply(elapsedSeconds(birth));
            return v != null ? v : 0f;
        };
    }

    private static float elapsedSeconds(long birth) {
        return (DEFAULT_CLOCK.getAsLong() - birth) / 1000f;
    }

    private static boolean expired(long birth, long durationMs) {
        return durationMs >= 0L && DEFAULT_CLOCK.getAsLong() - birth >= durationMs;
    }
}
