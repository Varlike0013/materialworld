# AetherUI 开发者指南

AetherUI 是一个基于 NeoForge 1.21.1 / Java 21 的声明式 UI 框架库（mod id: `aetherui`，包名 `com.varlike.aetherui`）。它本身不提供游戏内容，而是给其它 Mod 提供：

- 声明式 UI 构建 + 自动布局 + 屏幕空间渲染
- 左下角状态 HUD、右下角瞄准信息 HUD、物品栏上方 Buff 图标 HUD
- 3D 世界空间生物血条（面向玩家、遮挡检测）
- 3D 世界标记（图标 + 多行文字，支持穿墙与持续时间）
- 双语言（en_us / zh_cn）
- 多个扩展接口，允许其它 Mod 注入自定义内容

---

## 1. 依赖与构建

本项目使用 NeoForge 1.21.1 + Java 21。若你的 Mod 依赖 AetherUI，在 `build.gradle` 中添加（按你的实际发布渠道调整）：

```groovy
dependencies {
    implementation "net.neoforged:neoforge:21.1.256"
    // 依赖 AetherUI（本地开发或发布后的 Maven 坐标）
    implementation files("libs/aetherui-1.0.0.jar")
}
```

`neoforge.mods.toml` 中加入依赖声明：

```toml
[[dependencies.aetherui]]
modId = "aetherui"
type = "required"
versionRange = "[1.0.0,)"
ordering = "NONE"
side = "CLIENT"
```

构建命令：

```bash
./gradlew build          # 编译并打包
./gradlew runClient      # 启动客户端
./gradlew runData        # 生成双语语言文件
```

---

## 2. 核心概念

- `UIComponent`：不可变的组件树节点，由 `UIBuilder` 构建
- `UIBuilder`：一次性构建器，提供 `panel()` / `row()` / `column()` / `table()` / `canvas()` / `label()` / `icon()` 工厂；这里的 `panel()` 返回 `UIComponent`，旧的 `Panel` 类型已移除
- `UITree`：`UIComponent` 根节点 + `RenderTarget` + 可见性谓词
- `Bindable<T>`：惰性取值包装，`Bindable.supplier(...)` 可实现每帧动态数据
- `RenderTarget`：sealed 接口，两种实现：`ScreenRenderTarget`（屏幕空间）与 `WorldRenderTarget`（世界空间，billboard 投影渲染）。用 `AetherUI.buildAndRegisterBoth(...)` 可让同一棵组件树同时注册到两种目标，一次声明、两处绘制
- `LayoutEngine`：measure → place → emit，输出 `ElementSnapshot` 列表，由 `SnapshotRenderer` 绘制

---

## 3. 构建并显示一个屏幕 UI

### 3.1 从零构建组件树

```java
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import net.minecraft.network.chat.Component;

UITree tree = AetherUI.build(
    UIBuilder.panel(Size.fixed(200f), Size.fixed(60f))
        .background(0xC0202020)
        .add(UIBuilder.label(Component.literal("Hello AetherUI"))
                .textColor(0xFFFFFFFF))
        .build(),
    new ScreenRenderTarget(ScreenAnchor.topLeft(20f, 20f))
);
AetherUI.register(tree);
```

### 3.2 锚点与对齐

`ScreenAnchor` 提供以下工厂：

| 方法 | 含义 |
|------|------|
| `topLeft(x, y)` | 左上角 + 像素偏移 |
| `bottomLeft(x, y)` | 左下角 + 像素偏移 |
| `bottomRight(x, y)` | 右下角 + 像素偏移 |
| `bottomCenter(off)` | 底边中点 + 偏移 |
| `center()` | 屏幕中心 |
| `percent(xr, yr)` | 按屏幕比例定位 |

`ScreenRenderTarget` 可链式设置：

```java
new ScreenRenderTarget(ScreenAnchor.bottomCenter(-32f))
    .alignment(Alignment.BOTTOM_CENTER)  // 根节点哪个点对齐到锚点
    .guiScale(1f)                        // 逻辑像素到 GUI 坐标的缩放
    .layer(0);                           // 预留字段，当前版本不生效（渲染层顺序固定）
```

### 3.3 动态数据（Bindable）

```java
UIBuilder.label(Bindable.supplier(
    () -> Component.literal("HP: " + player.getHealth())
))
```

每帧布局时会重新取值，无需手动刷新。

### 3.4 生命周期

```java
AetherUI.register(tree);            // 注册，参与每帧渲染
AetherUI.destroy(tree);             // 注销并释放注册表的强引用
AetherUI.destroyLater(tree, 5000L); // 5 秒后自动注销，到期后组件与 lambda 可被 GC
AetherUI.destroyAllScreenUIs();     // 清空所有屏幕 UI（测试/热重载用）
```

`buildAndRegisterBoth(...)` 返回 `DualTree`，调用 `dualTree.destroy()` 会同时注销屏幕树和世界树。

带条件可见性（谓词返回 false 时不渲染，配置开关即时生效）：

```java
UITree tree = AetherUI.build(root, target, () -> Config.isSomethingEnabled());
```

---

## 4. Canvas 与自定义绘制

`canvas()` 是最灵活的组件：在 `CanvasPainter` 中通过 `CanvasContext` 绘制任意内容。

```java
UIBuilder.canvas(Size.fixed(120f), Size.fixed(20f))
    .canvasAnchor(Alignment.TOP_LEFT)   // 原点在 Canvas 左上角
    .painter(ctx -> {
        ctx.fill(0f, 0f, ctx.width(), ctx.height(), 0x80000000);
        ctx.drawString(Component.literal("Custom"), 2f, 2f, 0xFFFFFFFF, false);
    })
    .build();
```

`CanvasContext` 提供的绘制原语（坐标均为相对 Canvas 锚点的逻辑像素）：

| 方法 | 说明 |
|------|------|
| `fill(x, y, w, h, argb)` | 填充矩形 |
| `blit(texture, x, y, w, h, u, v, uw, uh, tint)` | 绘制纹理（归一化 UV） |
| `blitSprite(sprite, x, y, w, h)` | 绘制图集 sprite（按名称） |
| `blitAtlasSprite(sprite, x, y, w, h)` | 绘制已解析的 `TextureAtlasSprite` |
| `drawString(component, x, y, color, shadow)` | 绘制文字 |
| `renderItem(stack, x, y)` | 绘制物品图标 |
| `guiGraphics()` | 返回底层 `GuiGraphics`（仅 2D 后端，3D 为 null） |

### 4.1 可复用绘制样式（`CanvasStyles`）

Demo 与内置 HUD 使用的边框面板、带框图标、进度条样式已提取为公共工具类
`com.varlike.aetherui.api.paint.CanvasStyles`。它只依赖 `CanvasContext` 原语，
在 2D / 3D 后端都可用，避免每个开发者重复用 4 次 `fill` 拼边框。

> **定位：可选、推荐，不强制。** 这些只是封装好的便利方法，用来减少其它 Mod 添加 HUD 时的样板代码；
> 它们**不会**取代 `CanvasContext` 原语或 `HudBar.paint()`。任何样式你都可以不用，
> 直接用 `ctx.fill` / `ctx.blit` / `ctx.drawString` 等原语完全自定义绘制。

| 方法 | 说明 |
|------|------|
| `border(ctx, x, y, w, h, argb, t)` | 四边等宽边框，不填充内部 |
| `panel(ctx, x, y, w, h)` | 半透明黑底 + 1px 灰白边框的信息面板 |
| `panel(ctx, x, y, w, h, bg, borderArgb, borderWidth)` | 自定义底板色与边框色的面板 |
| `darkSlot(ctx, x, y, w, h)` | 深色底板 + 1px 灰边框的 HUD 槽位 |
| `framedImage(ctx, texture, x, y, size)` | 带半透明白灰边框的 PNG 图标（边框 1px，图片缩放填满内侧） |
| `framedImage(ctx, tex, x, y, w, h, border, bg, borderArgb)` | 完全可定制的带框图片 |
| `bar(ctx, x, y, w, h, ratio, color, label)` | **进度条变体一（饥饿条式）**：深色底框 + 比例填充 + 条内白色带阴影文字（如 `Food 20/20`） |
| `bar(ctx, x, y, w, h, ratio, color)` | 变体一的无文字版本（仍有底框） |
| `barWithValue(ctx, x, y, w, h, ratio, color, label, value, boxWidth)` | **进度条变体二（血条式）**：变体一 + 右侧独立数值方块，数字颜色与条填充色 `color` 一致 |
| `valueBox(ctx, x, y, w, h, value)` | 单独的数值方块（深色底框 + 居中浅灰数字），可独立复用 |
| `valueBox(ctx, x, y, w, h, value, textColor)` | 可指定数字颜色的数值方块 |

两个进度条变体的 `ratio` 都会被钳制到 `[0,1]`。两者区别只在**右侧有没有独立数值方块**
（条内文字两个变体都有）。按需选择，或两者都不用：

```java
UIBuilder.canvas(Size.fixed(120f), Size.fixed(20f))
    .canvasAnchor(Alignment.TOP_LEFT)
    .painter(ctx -> {
        // 变体一（饥饿条式）：条 + 边框 + 条内文字，无右侧方块
        CanvasStyles.bar(ctx, 2f, 2f, 116f, 16f, 0.6f, 0xFFD49A3C,
                Component.literal("Food 20/20"));

        // 变体二（血条式）：条 + 文字 + 右侧护甲数值方块（boxWidth=24）
        CanvasStyles.barWithValue(ctx, 2f, 24f, 116f, 16f, 0.6f, 0xFFE03333,
                Component.literal("HP 12/20"), 8, 24f);

        // 完全自定义（AetherUI 不干涉）：直接用原语
        // ctx.fill(2f, 46f, 116f * 0.6f, 16f, 0xFFE03333);
    })
    .build();
```

配色常量：`LIGHT_BORDER`（`0x99D0D0D0`）、`PANEL_BG`（`0x70000000`）、
`DARK_BG`（`0xB0080808`）、`DARK_BORDER`（`0xFF5A5A5A`）、`WHITE`、
`VALUE_TEXT_COLOR`（`0xFFC0C0C0`，数值方块内文字）。

### 4.2 带边框的 PNG 图标（`framedIcon`）

如果只想显示一张带边框的图片、并让 UI 树自动处理位置，直接用 `UIBuilder.framedIcon`：

```java
ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("mymod", "textures/icon.png");

UITree tree = AetherUI.build(
    UIBuilder.framedIcon(icon, 16f)              // 外框 16×16，边框 1px，图片缩放填满内侧
        .build(),
    new ScreenRenderTarget(ScreenAnchor.bottomRight(-8f, -8f))
        .alignment(Alignment.BOTTOM_RIGHT)        // 位置由树自动对齐，无需手写坐标
);
AetherUI.register(tree);
```

`framedIcon(tex, size, borderWidth)` 重载可自定义边框宽度。
其内部使用 `CanvasStyles.framedImage` 绘制，因此图片按 STRETCH 缩放填满边框内侧；
若需要 FIT / TILE 等其它缩放方式，请用 `UIBuilder.icon(...)` 配合 `.fitMode(...)`。

### 4.3 一次声明、两处绘制（世界空间 UI）

同一棵组件树可以同时出现在屏幕 HUD 与世界空间（如 Boss 头顶）。
用 `AetherUI.buildAndRegisterBoth(...)` 一次构建并注册两棵树，它们共享同一个 `UIComponent`，
因此**样式只写一次**，动态数据也只算一次：

```java
import com.varlike.aetherui.api.DualTree;
import com.varlike.aetherui.api.target.Anchor;
import com.varlike.aetherui.api.target.WorldRenderTarget;

// 样式只声明一次（这里用 canvas + painter，Bindable 或每帧动态数据均可）
UIComponent bossBar = UIBuilder.canvas(Size.fixed(180f), Size.fixed(38f))
    .canvasAnchor(Alignment.TOP_LEFT)
    .painter(ctx -> {
        CanvasStyles.panel(ctx, 0f, 0f, ctx.width(), ctx.height());
        CanvasStyles.bar(ctx, 4f, 16f, ctx.width() - 8f, 14f,
                bossRatio(), 0xFFE03333, Component.literal("HP ..."));
    })
    .build();

DualTree dual = AetherUI.buildAndRegisterBoth(
    bossBar,
    new ScreenRenderTarget(ScreenAnchor.percent(0.5f, 0f))
        .alignment(Alignment.TOP_CENTER),                       // 屏幕顶部中间
    new WorldRenderTarget(new Anchor.Entity(bossUUID, new Vec3(0f, 3.2f, 0f)))
        .alignment(Alignment.BOTTOM_CENTER)                     // Boss 头顶
        .pixelsPerBlock(64f)                                    // 每方块对应的逻辑像素
        .seeThrough(false)                                       // 被墙遮挡时不显示
        .maxRenderDistance(48f)
);

// 不再需要时：dual.destroy();  // 同时注销屏幕树与世界树
```

#### 位置（Anchor）：固定 / 绑定 / 表达式

`Anchor` 支持四种定位方式，静态与动态可混用：

```java
Anchor.fixed(new Vec3(100, 64, 200))          // ① 默认：固定世界坐标
Anchor.entity(bossUUID, new Vec3(0, 3.2, 0))  // ② 绑定实体：自动跟随，offset 按实体朝向旋转
Anchor.playerRelative(new Vec3(0, 0.5, 3))    // ③ 绑定玩家视线：z 为前方
Anchor.dynamic(() -> computeBossPos())        // ④ 表达式：每帧求值世界坐标
Anchor.bindable(bindableVec3.map(v -> v.above(1))) // 绑定参数 + 表达式变换（map）
```

`Anchor.dynamic(Supplier<Vec3>)` / `Anchor.bindable(Bindable<Vec3>)` 都是每帧读取，
内部对应 `Anchor.Dynamic` 记录；可放任意逻辑（读实体当前位置、按状态换算、插值等）。

#### 朝向（Orientation）：面向玩家 / 固定朝向 / 绑定 / 表达式

```java
Orientation.CENTER                            // ① 面向玩家（billboard，默认）
Orientation.VERTICAL                          // ② 仅绕 Y 轴面向玩家，保持竖直（血条首选）
Orientation.fixed(90f, 0f)                    // ③ 固定世界朝向（yaw/pitch 角度制）
Orientation.dynamic(() -> isRaid ? Orientation.CENTER
                                  : Orientation.fixed(90f, 0f))  // ④ 表达式：每帧切换
Orientation.bindable(bindableOrientation)     //    绑定参数
```

`WorldRenderTarget.orientation(...)` 也支持直接传 lambda（`Supplier<Orientation>`）：
`.orientation(() -> cond ? Orientation.CENTER : Orientation.VERTICAL)`；
若手上是 `Bindable`，用 `.orientation(bindable::get)`。

> 渲染实现：当前是在把世界 UI 投影到屏幕后，用 pose 的水平/垂直缩放近似 yaw/pitch
> 旋转效果（面向玩家=billboard 不缩放，竖直=抵消相机 pitch，固定朝向=按相机差值缩放）。
> `doubleSided=false` 时，固定朝向背面（偏航差 >90°）会被剔除。

`WorldRenderTarget` 可链式设置：

| 方法 | 含义 | 默认 |
|------|------|------|
| `alignment(Alignment)` | 根节点哪个点对齐到世界坐标 | `BOTTOM_CENTER` |
| `orientation(Orientation)` / `orientation(Supplier<Orientation>)` | 朝向，支持上面四种 | `CENTER` |
| `pixelsPerBlock(float)` | 每世界方块对应的逻辑像素，越小 UI 在世界里越小 | 128 |
| `seeThrough(boolean)` | true 穿墙显示；false 被方块遮挡时隐藏（VISUAL 射线检测） | true |
| `doubleSided(boolean)` | 固定朝向背面是否仍渲染 | false |
| `maxRenderDistance(float)` | 超过该距离不渲染 | 32 |
| `rollDegrees(float)` / `rollDegrees(Supplier<Float>)` | 绕 Z 轴旋转角（度），每帧求值可做平面转圈 | 0 |
| `yawDegrees(float)` / `yawDegrees(Supplier<Float>)` | 绕 Y 轴（垂直轴）旋转角，每帧求值 | 0 |
| `pitchDegrees(float)` / `pitchDegrees(Supplier<Float>)` | 绕 X 轴（水平轴）旋转角，每帧求值 | 0 |

> Demo 的 `BossBarDemo`（`showDemo=true` 时启用）展示了位置与朝向都用表达式绑定：
> 位置每帧计算玩家前方并做正弦浮动，朝向在"面向玩家 / 竖直"间周期切换。
> 真实 Boss 场景可改用 `Anchor.entity(bossUUID, ...)` 跟随。

### 4.4 方块上方图标（`WorldIcon`）：边框 + 背景 + 图片 + 浮动 + 多轴旋转

如果你只想**在一个方块上方显示一张带边框和背景的图片**，并让它随时间上下浮动、
绕多个轴旋转（典型场景：熔炼产物的悬浮展示），用 `WorldIcon` 一行搞定，无需自己拼 canvas/target：

```java
import com.varlike.aetherui.api.hud.WorldIcon;
import com.varlike.aetherui.api.hud.Spin;
import com.varlike.aetherui.api.target.Orientation;

UITree icon = WorldIcon.show(
    new Vec3(x + 0.5, y + 1.6, z + 0.5),          // 方块上方基准坐标
    ResourceLocation.fromNamespaceAndPath("mymod", "textures/item/result.png"), // 产物 PNG
    32f,                                           // 外框尺寸（正方形）
    Orientation.CENTER,                            // 朝向：始终面向玩家
    (t, base) -> base.add(0, 0.12 * Math.sin(t * 3), 0),  // 位置函数：随时间上下浮动
    Spin.roll(t -> t * 60f)                        // 自旋：绕 Z 轴平面转圈
);

// 不再需要时：AetherUI.destroy(icon);
```

**朝向 `Orientation`**（决定面板基准朝向，满足"始终面向某个特定方向"）：
- `Orientation.CENTER` —— 面向玩家（billboard，默认）
- `Orientation.VERTICAL` —— 仅绕 Y 轴面向玩家，保持竖直
- `Orientation.fixed(yaw, pitch)` —— 固定世界朝向（如 `fixed(90f, 0f)` 恒定朝东）；
  配合 `WorldRenderTarget.doubleSided(true)` 可让背面也渲染

**自旋 `Spin`**（三轴独立，每帧按时间 t 求值，可组合）：
- `Spin.yaw(t -> ...)` —— 绕 Y 轴（垂直轴）翻，像硬币旋转，90° 时侧面不可见
- `Spin.pitch(t -> ...)` —— 绕 X 轴（水平轴）前后翻
- `Spin.roll(t -> ...)` —— 绕 Z 轴（屏幕法线）平面转圈，始终面向玩家
- `Spin.of(yawFn, pitchFn, rollFn)` —— 三轴同时配置，任一传 `null` 表示该轴不转
- 三轴用四元数组合（`rotateY → rotateX → rotateZ`）做真正 3D 旋转，避免万向锁

**位置函数** `PositionFunction (t秒, base) → Vec3`：每帧求值，`base.add(0, A*sin(t*ω), 0)` 即上下浮动；传 `null` 固定。

**其它重载**：
- 旧 API 兼容：`show(base, tex, size, positionFn, rotationFn)` 中 `rotationFn` 为单轴 roll（平面转圈）
- `show(base, tex, size, orientation, positionFn, spin, durationMs)` —— `durationMs` > 0 时到期自动不渲染，负数永久
- `show(base, tex, size)` —— 纯静态边框+背景+图片

> 图片请用**独立的 PNG 纹理**（如 `textures/item/xxx.png`）。原版 `textures/item/iron_ingot.png` 是独立小 PNG，可正常显示（放大可能偏糊）；但**图集纹理**（`textures/atlas/*`）用整张 UV 会错位，需改用 `CanvasContext.blitSprite`。

> Demo `SmeltingIconDemo.show(blockPos)` 展示了朝向 CENTER + 位置浮动 + yaw/roll 双轴自旋（用原版 dirt 纹理演示）。

---

## 5. 扩展点

所有扩展接口都通过 `AetherUI` 的静态方法注册，注册表按注册顺序查询，重复 `id` 会抛异常。建议在你的客户端初始化阶段（如 `FMLClientSetupEvent`）注册。

### 5.1 自定义 HUD 条（`HudBar`）

其它 Mod 实现 `HudBar` 并调用 `AetherUI.registerHudBar(bar)`，即可把自己的状态条
（如魔力条）加入左下角 HUD。多条按注册顺序从上到下排列，显示在内置血条与护甲方块之后；
`ctx` 已对齐到本条左上角，无需关心屏幕坐标。

```java
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// 建议在你的客户端初始化（FMLClientSetupEvent）中注册
ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mymod", "mana");

AetherUI.registerHudBar(new HudBar() {
    @Override public ResourceLocation id() { return ID; }
    @Override public float width()  { return 142f; }  // 可选，默认 142
    @Override public float height() { return 12f; }   // 可选，默认 12

    @Override public boolean visible(LocalPlayer p) {
        return ManaProvider.max(p) > 0;               // 可选：无魔力时隐藏该条
    }

    @Override public void paint(CanvasContext ctx, LocalPlayer p) {
        int max = ManaProvider.max(p);
        float ratio = max > 0 ? (float) ManaProvider.get(p) / max : 0f;
        // bar() 会画深色底框 + 按比例填充 + 带阴影的白色标签
        CanvasStyles.bar(ctx, 0f, 0f, ctx.width(), ctx.height(),
                ratio, 0xFF3388FF,
                Component.literal("MP " + ManaProvider.get(p) + "/" + max));
    }
});
```

要点：

- **位置自动处理**：条固定在左下角，紧跟内置血条/护甲方块；`visible()` 返回 false 时不占位。
- **数据需自行同步**：`paint()` 运行在客户端，`LocalPlayer` 是客户端玩家；魔力若由服务端维护，
  需通过 Data Attachment / 网络包等方式同步到客户端后再读取（示例中的 `ManaProvider` 由你实现）。
- **显示条件**：左下角 HUD 受 `enableCustomHud` 配置控制，且创造 / 旁观模式整体不渲染。
- **id 唯一**：使用你自己 mod 的 namespace，重复 id 会抛异常。

注销：`AetherUI.unregisterHudBar(id)` / `AetherUI.unregisterHudBar(bar)` / `AetherUI.clearHudBars()`。

### 5.2 Buff 图标（`BuffIcon`）

在物品栏上方显示自定义图标，从左到右、超出换行向上。提供三种工厂：

```java
// 整张纹理图片
AetherUI.registerBuffIcon(
    BuffIcon.texture(id, ResourceLocation.fromNamespaceAndPath("mymod", "textures/buff.png"), "My Buff"));

// 图集 sprite
AetherUI.registerBuffIcon(
    BuffIcon.sprite(id, ResourceLocation.fromNamespaceAndPath("mymod", "buff_sprite"), "My Buff"));

// 完全自定义绘制（支持任意类型）
AetherUI.registerBuffIcon(
    BuffIcon.custom(id, "My Buff", (ctx, player) -> {
        ctx.fill(0f, 0f, ctx.width(), ctx.height(), 0x80FF0000);
        ctx.drawString(Component.literal("X"), 8f, 6f, 0xFFFFFFFF, false);
    }));
```

`BuffIcon` 也可直接实现接口，覆写 `visible(LocalPlayer)` 控制显示时机。注销：`AetherUI.unregisterBuffIcon(id)` / `clearBuffIcons()`。

### 5.3 方块信息（`BlockInfoProvider`）

当玩家瞄准方块时，在右下角 HUD 的挖掘信息下方追加你的信息。可以注册全局 `BlockInfoProvider`，
也可以让方块/方块实体直接实现 `BlockInfoSource`（见 5.3.1，无需注册）。返回 `List<Component>`，
**每行一个 Component，Component 的 Style 决定颜色/粗体等**：

```java
import com.varlike.aetherui.api.hud.BlockInfoProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

AetherUI.registerBlockInfoProvider(new BlockInfoProvider() {
    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("mymod", "block_info");
    }
    @Override public List<Component> getLines(Level level, BlockPos pos, BlockState state,
                                              BlockEntity be, BlockHitResult hit, LocalPlayer player) {
        if (!(be instanceof MyBlockEntity myBE)) return List.of(); // 不匹配返回空
        return List.of(
            Component.literal("Energy: " + myBE.energy)
                .withStyle(ChatFormatting.AQUA),
            Component.literal("Owner: " + myBE.owner)
                .withStyle(ChatFormatting.GRAY)
        );
    }
});
```

注销：`AetherUI.unregisterBlockInfoProvider(id)` / `unregisterBlockInfoProvider(provider)` / `clearBlockInfoProviders()`。

> **只在瞄准方块时调用**（实体不触发），受 `enableTargetInfo` 配置控制，所有游戏模式均显示。
> 多个 provider 按注册顺序把行依次追加。面板宽度自动适配最长行。

> 提示：把数据保存在你的 `BlockEntity` 中，provider 从 `be` 参数读取即可。客户端需通过同步机制（如 `getUpdateTag` / `onDataPacket`）把数据同步到客户端。

**常见场景一：容器已用格子数**（箱子、木桶、发射器等任意 `Container`）：

```java
AetherUI.registerBlockInfoProvider(new BlockInfoProvider() {
    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("mymod", "container_slots");
    }
    @Override public List<Component> getLines(Level level, BlockPos pos, BlockState state,
                                              BlockEntity be, BlockHitResult hit, LocalPlayer player) {
        if (!(be instanceof Container container)) return List.of(); // 非容器不显示
        int total = container.getContainerSize();
        int used = 0;
        for (int i = 0; i < total; i++) {
            if (!container.getItem(i).isEmpty()) used++;
        }
        return List.of(
            Component.literal("格子: " + used + "/" + total + " 已用")
                .withStyle(used == total ? ChatFormatting.RED : ChatFormatting.GREEN)
        );
    }
});
// import net.minecraft.world.Container;
// 若只想匹配箱子，可加：if (!state.is(Blocks.CHEST)) return List.of();
```

> Demo 已在 `TargetInfoHud` 中内置了这条 provider（namespace `aetherui`），
> 瞄准任意容器即可看到效果。

**常见场景二：其它 Mod 的容器（如"魔力聚集器 MK1"）**：

```java
AetherUI.registerBlockInfoProvider(new BlockInfoProvider() {
    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("mymod", "mana_collector_info");
    }
    @Override public List<Component> getLines(Level level, BlockPos pos, BlockState state,
                                              BlockEntity be, BlockHitResult hit, LocalPlayer player) {
        if (!(be instanceof ManaCollectorBlockEntity collector)) return List.of(); // 你的 BE 类
        int mana = collector.getMana();      // 从客户端 BE 读取（需已同步）
        int max  = collector.getMaxMana();
        return List.of(
            Component.literal("魔力聚集器 MK1")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE),
            Component.literal("魔力: " + mana + "/" + max)
                .withStyle(ChatFormatting.AQUA)
        );
    }
});
```

> 场景二的关键：`collector` 的魔力值必须**从服务端同步到客户端 BlockEntity**
> （`getUpdateTag` / `onDataPacket` 或 Data Attachment + 网络包），否则客户端读到的是默认值。

### 5.3.1 方块自提供信息（`BlockInfoSource`）

如果你的信息属于某个具体方块，可以直接让 `Block`（或 `BlockEntity`）实现 `BlockInfoSource`，
**无需注册、无需 id**。TargetInfoHud 瞄准该方块时会自动调用并追加返回的行：

```java
import com.varlike.aetherui.api.hud.BlockInfoSource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public final class MyBlock extends Block implements BlockInfoSource {
    @Override
    public List<Component> getTargetInfoLines(Level level, BlockPos pos, BlockState state,
                                              BlockEntity be, BlockHitResult hit, Player player) {
        if (!(be instanceof MyBlockEntity myBE)) return List.of();
        return List.of(
            Component.literal("能量: " + myBE.energy + " FE").withStyle(ChatFormatting.AQUA),
            Component.literal("拥有者: " + myBE.owner).withStyle(ChatFormatting.GRAY)
        );
    }
}
```

说明：

- 回调只在客户端、且 `showTargetBlockInfo` 开启时触发。
- 方块与方块实体都实现时，两者都会显示；顺序为 **方块 → 方块实体 → 已注册的 `BlockInfoProvider`**。
- 与注册式 provider 一样，服务端数据必须先同步到客户端，否则客户端读到的是默认值。

### 5.3.2 配方显示（`RecipeInfoProvider` / `RecipeInfo`）

当希望瞄准机器时显示一条可视化配方 **`[材料] → [时间] → [产物]`**（物品用图标绘制），
实现 `RecipeInfoProvider` 并注册。它与 `BlockInfoProvider` 并行工作，配方行追加在文本信息之后。

```java
import com.varlike.aetherui.api.hud.RecipeInfo;
import com.varlike.aetherui.api.hud.RecipeInfoProvider;

AetherUI.registerRecipeInfoProvider(new RecipeInfoProvider() {
    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("mymod", "recipe");
    }
    @Override public Component title() { return Component.literal("熔炼配方:"); } // 可选

    @Override public List<RecipeInfo> getRecipes(Level level, BlockPos pos, BlockState state,
                                                 BlockEntity be, BlockHitResult hit, LocalPlayer player) {
        if (!(be instanceof MyMachineBlockEntity machine)) return List.of();
        // 材料、产物、时间（秒）；时间传 0 则不显示时间标签
        return List.of(RecipeInfo.of(machine.getInput(), machine.getOutput(), machine.getSeconds()));
    }
});
```

`RecipeInfo` 构造：
- `RecipeInfo.of(ItemStack ingredient, ItemStack result, int seconds)` —— 单材料快捷构造
- `new RecipeInfo(List<ItemStack> ingredients, ItemStack result, int seconds)` —— 多材料
- 列表不可变；空材料/空产物会跳过对应图标；`seconds <= 0` 不显示时间。

> Demo 内置了 `DemoFurnaceRecipe`：瞄准熔炉 / 烟熏炉 / 熔岩炉时，
> 读取槽位输入并通过 `RecipeManager.getRecipeFor(RecipeType.SMELTING, ...)` 查询配方，
> 显示 `[输入] → [配方时间(秒)] → [输出]`。
> 注意 MC 1.21.1 熔炉的实时 `cookingProgress` 是包私有字段，Mod 无法直接读取，
> 因此 Demo 显示的是配方所需时间，而非动态剩余进度；你有自己机器的数据源时可提供真实剩余时间。

注销：`AetherUI.unregisterRecipeInfoProvider(id)` / `unregisterRecipeInfoProvider(provider)` / `clearRecipeInfoProviders()`。

### 5.4 3D 世界标记（`WorldMarkerProvider` / `WorldMarker`）

在世界坐标处渲染一个面板：**白色边框 + 黑色半透明背景 + 左侧物品 PNG 图标 + 右侧多行彩色文字**，每个标记可独立设置是否穿透墙体。

```java
import com.varlike.aetherui.api.hud.WorldMarker;
import com.varlike.aetherui.api.hud.WorldMarkerProvider;
import net.minecraft.ChatFormatting;

AetherUI.registerWorldMarker(new WorldMarkerProvider() {
    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("mymod", "world_marker");
    }
    @Override public List<WorldMarker> getMarkers(Level level, LocalPlayer player) {
        Vec3 pos = new Vec3(100, 64, 200);
        ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("mymod", "textures/item.png");
        // throughWall=false 时被方块遮挡则不显示；true 则穿墙显示
        return List.of(WorldMarker.of(pos, icon, false,
            Component.literal("Treasure Chest").withStyle(ChatFormatting.GOLD),
            Component.literal("Distance: 12m").withStyle(ChatFormatting.GRAY)
        ));
    }
});

// 带持续时间的标记：5 秒后自动消失（同一 id 共享计时，消失后再次出现会重新计时）
WorldMarker timed = WorldMarker.timed("chest-hint", pos, icon, false, 5000L,
    Component.literal("Found!").withStyle(ChatFormatting.GREEN));
```

`WorldMarker` 字段：

| 字段 | 说明 |
|------|------|
| `id` | 唯一标识，用于持续时间计时（同一 id 共享首次出现时间） |
| `position` | 世界坐标 `Vec3`（面板中心） |
| `icon` | 左侧 PNG 纹理 `ResourceLocation`，可为 `null` |
| `lines` | 右侧文字行，Component 样式决定颜色/粗体 |
| `throughWall` | `true` 穿墙渲染；`false` 被方块遮挡时隐藏（VISUAL 射线检测） |
| `durationMs` | 持续时间（毫秒），大于 0 时到期自动消失；`WorldMarker.PERMANENT`（-1）表示永久 |

工厂方法：
- `WorldMarker.of(position, icon, throughWall, lines...)` — 永久，id 由坐标生成
- `WorldMarker.of(id, position, icon, throughWall, lines...)` — 永久，自定义 id
- `WorldMarker.timed(id, position, icon, throughWall, durationMs, lines...)` — 带持续时间

注销：`AetherUI.unregisterWorldMarker(id)` / `unregisterWorldMarker(provider)` / `clearWorldMarkers()`。

---

## 6. 内置 HUD 行为

| HUD | 位置 | 内容 | 受配置控制 |
|-----|------|------|-----------|
| 状态 HUD | 左下角 | 装备/手持耐久行、经验条、饥饿条、血条+护甲方块 | `enableCustomHud`、`showArmorDurability`、`showHeldDurability` |
| 瞄准信息 HUD | 右下角 | 实体名/HP/护甲/距离；方块名/工具/等级/可否挖掘；`BlockInfoSource` / `BlockInfoProvider` 扩展信息；容器格子数；配方 `[材料]→[时间]→[产物]` | `enableTargetInfo`、`showTargetBlockInfo` |
| Buff HUD | 物品栏上方 | 原版状态效果 + 注册的 `BuffIcon` | `enableBuffHud`（旁观者不显示） |
| 3D 血条 | 生物头顶 | 名字 + 血条 + 护甲方块（仅有护甲时） | `enableWorldHealthBar`、`showHostileHealthBar`、`showAnimalHealthBar`、`showNeutralHealthBar` |
| 世界空间 UI | 世界坐标 / 实体 / 玩家相对 | 任意 `UIComponent`（与屏幕树共享样式，一次声明两处绘制） | `enableWorldUI` 与构建时的可见性谓词 |

原版状态条（血量、护甲、饥饿、空气、经验、坐骑血量、跳跃条）与状态效果图标会被隐藏，物品栏保留。

---

## 7. 配置项

客户端配置（Mods → AetherUI → Config）：

| 键 | 默认 | 说明 |
|----|------|------|
| `enableAllAssists` | true | **总开关**：关闭后本 Mod 提供的所有辅助全部停用 |
| `enableCustomHud` | true | 启用左下角状态 HUD（创造/旁观者自动隐藏） |
| `showArmorDurability` | true | 显示盔甲耐久 |
| `showHeldDurability` | true | 显示手持物品耐久 |
| `enableBuffHud` | true | 启用物品栏上方的 Buff 图标 HUD |
| `enableTargetInfo` | true | 启用右下角瞄准信息 |
| `showTargetBlockInfo` | true | 瞄准信息中显示方块扩展信息（容器格子、配方等） |
| `enableWorldHealthBar` | true | 启用 3D 血条 |
| `showHostileHealthBar` | true | 显示敌对怪物血条 |
| `showAnimalHealthBar` | true | 显示被动动物血条 |
| `showNeutralHealthBar` | true | 显示中立生物血条 |
| `enableWorldMarkers` | true | 启用其它 Mod 注册的 3D 世界标记 |
| `enableWorldUI` | true | 启用世界空间 UI（WorldRenderTarget / WorldIcon） |
| `hideVanillaBars` | true | 隐藏原版状态条 |
| `showDemo` | false | 显示左上角 demo 面板（演示总开关） |
| `showBossBarDemo` | false | 显示 Boss 血条双端绘制演示（需 `showDemo` 同时开启） |
| `showSmeltingIconDemo` | false | 显示熔炼产物浮动旋转图标演示 |

**完全关闭本 Mod 的辅助**：把 `enableAllAssists` 设为 false 即可——所有内置 HUD、瞄准信息、血条、世界标记、世界 UI、隐藏原版条全部停用。其它 Mod 通过 AetherUI API 注册的内容不受此总开关影响（它们各自的可见性由调用方控制）。

所有子开关都受总开关约束（如 `showArmorDurability` = `enableAllAssists && enableCustomHud && showArmorDurability`），通过 `UITree` 可见性谓词或每帧渲染检查实现，**修改后即时生效，无需重启**。

---

## 8. 双语言

翻译键集中在 `com.varlike.aetherui.api.lang.AetherUILang`，由 `ModLanguageProvider` 生成 `en_us.json` / `zh_cn.json`。HUD 文案通过 `Component.translatable(key)` 获取，随游戏语言自动切换。

如需为你的 Mod 添加文案，在自己的数据生成器中参照 `ModLanguageProvider` 的模式：

```java
generator.addProvider(event.includeClient(), new YourLanguageProvider(output, "en_us"));
generator.addProvider(event.includeClient(), new YourLanguageProvider(output, "zh_cn"));
```

配置界面的翻译键格式为 `aetherui.configuration.<配置项名>` 及 `<...>.tooltip`，已在 `ModLanguageProvider` 中生成中英文。

---

## 9. 包结构速查

```
com.varlike.aetherui
├── AetherUIMod / AetherUIClient   入口
├── Config                         配置
├── api
│   ├── AetherUI                   主入口（build/register/扩展注册）
│   ├── UIBuilder / UIComponent / UITree / DualTree（双端树）
│   ├── binding                    Bindable（惰性取值）
│   ├── hud                        HudBar / BuffIcon / BlockInfoProvider / BlockInfoSource / RecipeInfo / RecipeInfoProvider / WorldMarker / WorldMarkerProvider / WorldIcon / Spin（扩展接口/便捷API）
│   ├── lang                       AetherUILang（翻译键）
│   ├── layout                     Size / Alignment / FitMode / MeasureContext 等布局类型
│   ├── paint                      CanvasContext / CanvasPainter
│   ├── snapshot                   ElementSnapshot
│   └── target                     RenderTarget / ScreenRenderTarget / ScreenAnchor / WorldRenderTarget / Anchor / Orientation
├── core
│   ├── hud                        VanillaHudHandler / WorldHealthBarRenderer / WorldMarkerRenderer
│   ├── layout                     LayoutEngine / MeasureContext / DefaultMeasureContext
│   ├── registry                   ScreenUIRegistry / WorldUIRegistry / HudBarRegistry / BuffIconRegistry / BlockInfoRegistry / RecipeInfoRegistry / WorldMarkerRegistry
│   ├── render                     ScreenRenderHandler / WorldRenderHandler / SnapshotRenderer / GuiCanvasContext / OffsetCanvasContext
│   └── tick                       ClientTickHandler（定时销毁 UITree）
├── datagen                        ModDataGen / ModLanguageProvider
└── demo                           CustomHud / TargetInfoHud / BuffHud / DemoUI / DemoFurnaceRecipe / BossBarDemo / SmeltingIconDemo
```

---

## 10. 常见问题

**Q：UI 不显示？**
确认调用了 `AetherUI.register(tree)`，且 `tree.isVisible()` 的谓词返回 true；屏幕 UI 由 GUI layer 渲染，需处于客户端环境。

**Q：世界空间 UI 不显示？**
确认用了 `WorldRenderTarget` 并调用 `AetherUI.register(tree)`（或 `buildAndRegisterBoth`）；
检查 `seeThrough=false` 时是否被方块遮挡、`maxRenderDistance` 是否过近、锚点实体是否存在。
世界 UI 当前为 billboard（面向摄像机）投影渲染，`orientation` 参数暂不生效。

**Q：3D 血条被方块挡住？**
这是预期行为：血条用 `ClipContext.Block.VISUAL` 做遮挡检测，被实体方块挡住时不渲染。

**Q：创造/旁观模式血条不显示？**
左下角状态 HUD 在创造和旁观模式自动隐藏；3D 血条和右下角瞄准信息不受模式限制。

**Q：扩展接口重复 id 报错？**
每个扩展接口用 `ResourceLocation` 作为唯一标识，请用你自己 mod 的 namespace，避免与其他 mod 冲突。
