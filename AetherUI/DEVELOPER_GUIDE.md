# AetherUI 开发者指南

AetherUI 是一个基于 NeoForge 1.21.1 / Java 21 的声明式 UI 框架库（mod id: `aetherui`，包名 `com.varlike.aetherui`）。它本身不提供游戏内容，而是给其它 Mod 提供：

- 声明式 UI 构建 + 自动布局 + 屏幕空间渲染
- 左下角状态 HUD、右下角瞄准信息 HUD、物品栏上方 Buff 图标 HUD
- 3D 世界空间生物血条（面向玩家、遮挡检测）
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
- `UIBuilder`：一次性构建器，提供 `panel()` / `row()` / `column()` / `table()` / `canvas()` / `label()` / `icon()` 工厂
- `UITree`：`UIComponent` 根节点 + `RenderTarget` + 可见性谓词
- `Bindable<T>`：惰性取值包装，`Bindable.supplier(...)` 可实现每帧动态数据
- `RenderTarget`：当前实现为 `ScreenRenderTarget`（屏幕空间）
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
    .layer(0);                           // 预留的层级控制
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
AetherUI.register(tree);      // 注册，参与每帧渲染
AetherUI.destroy(tree);       // 注销
AetherUI.destroyAllScreenUIs();// 清空所有屏幕 UI（测试/热重载用）
```

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

---

## 5. 扩展点

所有扩展接口都通过 `AetherUI` 的静态方法注册，注册表按注册顺序查询，重复 `id` 会抛异常。建议在你的客户端初始化阶段（如 `FMLClientSetupEvent`）注册。

### 5.1 自定义 HUD 条（`HudBar`）

在左下角 HUD 的饥饿条下方区域显示你自己的条（如魔力条）：

```java
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.paint.CanvasContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.LocalPlayer;

ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mymod", "mana");

AetherUI.registerHudBar(new HudBar() {
    @Override public ResourceLocation id() { return ID; }
    @Override public float width()  { return 142f; }  // 可选，默认 142
    @Override public float height() { return 12f; }   // 可选，默认 12
    @Override public boolean visible(LocalPlayer p) { return true; } // 可选
    @Override public void paint(CanvasContext ctx, LocalPlayer p) {
        float ratio = ManaProvider.get(p) / (float) ManaProvider.getMax(p);
        ctx.fill(0f, 0f, ctx.width(), ctx.height(), 0x99000000);
        ctx.fill(0f, 0f, ctx.width() * ratio, ctx.height(), 0xFF3388FF);
        ctx.drawString(Component.literal("Mana"), 2f, 1f, 0xFFFFFFFF, false);
    }
});
```

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

当玩家瞄准方块时，在右下角 HUD 的挖掘信息下方追加你的信息。返回 `List<Component>`，**每行一个 Component，Component 的 Style 决定颜色/粗体等**：

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

注销：`AetherUI.unregisterBlockInfoProvider(id)` / `clearBlockInfoProviders()`。

> 提示：把数据保存在你的 `BlockEntity` 中，provider 从 `be` 参数读取即可。客户端需通过同步机制（如 `getUpdateTag` / `onDataPacket`）把数据同步到客户端。

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
| 瞄准信息 HUD | 右下角 | 实体名/HP/护甲/距离；方块名/工具/等级/可否挖掘；扩展方块信息 | `enableTargetInfo` |
| Buff HUD | 物品栏上方 | 原版状态效果 + 注册的 `BuffIcon` | `enableCustomHud`（旁观者不显示） |
| 3D 血条 | 生物头顶 | 名字 + 血条 + 护甲方块（仅有护甲时） | `enableWorldHealthBar`、`showNeutralHealthBar` |

原版状态条（血量、护甲、饥饿、空气、经验、坐骑血量、跳跃条）与状态效果图标会被隐藏，物品栏保留。

---

## 7. 配置项

客户端配置（Mods → AetherUI → Config）：

| 键 | 默认 | 说明 |
|----|------|------|
| `enableCustomHud` | true | 启用左下角状态 HUD（创造/旁观者自动隐藏） |
| `enableTargetInfo` | true | 启用右下角瞄准信息 |
| `showArmorDurability` | true | 显示盔甲耐久 |
| `showHeldDurability` | true | 显示手持物品耐久 |
| `enableWorldHealthBar` | true | 启用 3D 血条 |
| `showNeutralHealthBar` | true | 显示中立生物血条 |
| `hideVanillaBars` | true | 隐藏原版状态条 |
| `showDemo` | false | 显示左上角 demo 面板 |

所有开关通过 `UITree` 可见性谓词或渲染检查实现，**修改后即时生效，无需重启**。

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
│   ├── UIBuilder / UIComponent / UITree / Bindable
│   ├── binding                    Bindable
│   ├── hud                        HudBar / BuffIcon / BlockInfoProvider / WorldMarker / WorldMarkerProvider（扩展接口）
│   ├── lang                       AetherUILang（翻译键）
│   ├── layout                     Size / Alignment / FitMode / 布局类型
│   ├── paint                      CanvasContext / CanvasPainter
│   ├── snapshot                   ElementSnapshot
│   └── target                     ScreenRenderTarget / ScreenAnchor
├── core
│   ├── hud                        VanillaHudHandler / WorldHealthBarRenderer
│   ├── layout                     LayoutEngine / MeasureContext
│   ├── registry                   ScreenUIRegistry / HudBarRegistry / BuffIconRegistry / BlockInfoRegistry / WorldMarkerRegistry
│   └── render                     ScreenRenderHandler / SnapshotRenderer / GuiCanvasContext / OffsetCanvasContext
│   ├── hud                        VanillaHudHandler / WorldHealthBarRenderer / WorldMarkerRenderer
├── datagen                        ModDataGen / ModLanguageProvider
└── demo                           CustomHud / TargetInfoHud / BuffHud / DemoUI
```

---

## 10. 常见问题

**Q：UI 不显示？**
确认调用了 `AetherUI.register(tree)`，且 `tree.isVisible()` 的谓词返回 true；屏幕 UI 由 GUI layer 渲染，需处于客户端环境。

**Q：3D 血条被方块挡住？**
这是预期行为：血条用 `ClipContext.Block.VISUAL` 做遮挡检测，被实体方块挡住时不渲染。

**Q：创造/旁观模式血条不显示？**
左下角状态 HUD 在创造和旁观模式自动隐藏；3D 血条和右下角瞄准信息不受模式限制。

**Q：扩展接口重复 id 报错？**
每个扩展接口用 `ResourceLocation` 作为唯一标识，请用你自己 mod 的 namespace，避免与其他 mod 冲突。
