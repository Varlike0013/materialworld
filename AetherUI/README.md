# AetherUI

AetherUI 是一个基于 **NeoForge 1.21.1 / Java 21** 的声明式 UI 框架库（mod id：`aetherui`，包名 `com.varlike.aetherui`）。

它本身不提供游戏内容，而是为其它 Mod 提供一套可复用的 UI 与 HUD 能力：

- 声明式组件树构建 + 自动布局 + 屏幕空间渲染
- 一次声明组件树，同步绘制到 HUD 与世界空间（Boss 血条等）
- 方块上方的边框 + 背景 + 图片便捷 API（支持浮动、三轴旋转、朝向与定时销毁）
- 左下角状态 HUD（耐久 / 经验 / 饥饿 / 血量 / 护甲）
- 右下角瞄准信息 HUD（实体 / 方块 / 扩展信息）
- 物品栏上方的 Buff 图标 HUD（右下角罗马数字层数；提供 17×17 文字/PNG 徽章一行注册 API）
- 3D 世界空间生物血条（面向玩家、遮挡检测）
- 3D 世界标记（图标 + 多行文字，支持穿墙与持续时间）
- 中英双语言（en_us / zh_cn）
- 方块可直接实现 `BlockInfoSource`，瞄准时自动显示自己的保存信息；另提供多个注册式扩展接口
- 内置 Demo 开关即时生效；熔炼演示自动追踪附近活跃熔炉并显示真实产物

完整的 API 说明与集成指南见 **[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)**。

---

## 功能一览

| 能力 | 说明 |
|------|------|
| 组件与布局 | `UIBuilder` 提供 `panel` / `row` / `column` / `table` / `canvas` / `label` / `icon` 工厂；`table(columns)` 按添加顺序自动换行对齐；`LayoutEngine` 执行 measure → place → emit |
| 动态数据 | `Bindable<T>` 惰性取值，支持 `Bindable.supplier(...)` 每帧刷新 |
| 屏幕 UI | `ScreenRenderTarget` + 多种 `ScreenAnchor` 锚点，支持对齐、缩放 |
| 双端世界 UI | `buildAndRegisterBoth(...)` 用一棵组件树同时注册屏幕与 `WorldRenderTarget`；`DualTree.destroy()` 一次清理 |
| 自定义绘制 | `canvas()` + `CanvasContext`，提供矩形填充、纹理、图集 sprite、文字、物品图标等原语 |
| 扩展 HUD | `HudBar.of(...)` 一行注册自定义条；`UIBuilder.barCanvas(...)` / `barWithValueCanvas(...)` 画动态进度条与数值条，支持 `BarColor` 单色/渐变；另有 `BuffIcon`、`BlockInfoProvider`、`BlockInfoSource`、`WorldMarkerProvider` |
| 内置 HUD | 状态 HUD、瞄准信息 HUD、Buff HUD、3D 血条，均可独立配置开关 |
| 权威状态同步 | 内置网络查询让容器格子数（含大箱子合并统计）与熔炉产物显示不依赖客户端未同步的 BlockEntity 数据 |
| 双语言 | 翻译键集中在 `AetherUILang`，由数据生成器产出 `en_us.json` / `zh_cn.json` |

---

## 集成到你的 Mod

### 1. 添加依赖

在 `build.gradle` 中添加（按实际发布渠道调整坐标）：

```groovy
dependencies {
    implementation "net.neoforged:neoforge:21.1.256"
    // 本地开发或发布后的 Maven 坐标
    implementation files("libs/aetherui-1.0.0.jar")
}
```

在 `neoforge.mods.toml` 中声明依赖：

```toml
[[dependencies.aetherui]]
modId = "aetherui"
type = "required"
versionRange = "[1.0.0,)"
ordering = "NONE"
side = "CLIENT"
```

### 2. 构建并显示一个屏幕 UI

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

更多示例（锚点对齐、动态数据、Canvas 自定义绘制、扩展接口注册）请参考 [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)。

---

## 配置项

客户端配置（Mods → AetherUI → Config），修改后即时生效：

| 键 | 默认 | 说明 |
|----|------|------|
| `enableAllAssists` | true | **总开关**：关闭后本 Mod 提供的所有辅助全部停用 |
| `enableCustomHud` | true | 启用左下角状态 HUD（创造 / 旁观者自动隐藏） |
| `customHudAnchorX` | 10 | 左下角 HUD 锚点 X 偏移（正值向右） |
| `customHudAnchorY` | -12 | 左下角 HUD 锚点 Y 偏移（正值向上） |
| `customHudBarHeight` | 12 | 内置经验/饥饿/血量条高度 |
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
| `enableWorldUI` | true | 启用世界空间 UI（`WorldRenderTarget` / `WorldIcon`） |
| `hideVanillaBars` | true | 隐藏原版状态条 |
| `showDemo` | false | 显示左上角 demo 面板（演示总开关） |
| `showBossBarDemo` | false | 显示 Boss 血条双端绘制演示（需 `showDemo` 同时开启） |
| `showSmeltingIconDemo` | false | 显示熔炼产物浮动旋转图标演示 |

把 `enableAllAssists` 设为 `false` 即可完全关闭本 Mod 的内置辅助；其它 Mod 通过 AetherUI API 注册的内容不受该总开关影响。所有子开关均受总开关约束，修改后即时生效。

---

## 构建

项目使用 NeoForge ModDevGradle，要求 Java 21。

```bash
./gradlew build          # 编译并打包
./gradlew runClient      # 启动客户端
./gradlew runData        # 生成双语语言文件
```

生成产物位于 `build/libs/`。语言文件由 `runData` 输出到 `src/generated/resources/`，构建时自动纳入。

---

## 许可

见 [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt)。
