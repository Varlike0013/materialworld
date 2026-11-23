# AetherUI

AetherUI 是一个基于 **NeoForge 1.21.1 / Java 21** 的声明式 UI 框架库（mod id：`aetherui`，包名 `com.varlike.aetherui`）。

它本身不提供游戏内容，而是为其它 Mod 提供一套可复用的 UI 与 HUD 能力：

- 声明式组件树构建 + 自动布局 + 屏幕空间渲染
- 左下角状态 HUD（耐久 / 经验 / 饥饿 / 血量 / 护甲）
- 右下角瞄准信息 HUD（实体 / 方块 / 扩展信息）
- 物品栏上方的 Buff 图标 HUD
- 3D 世界空间生物血条（面向玩家、遮挡检测）
- 3D 世界标记（图标 + 多行文字，支持穿墙与持续时间）
- 中英双语言（en_us / zh_cn）
- 多个扩展接口，允许其它 Mod 注入自定义内容

完整的 API 说明与集成指南见 **[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)**。

---

## 功能一览

| 能力 | 说明 |
|------|------|
| 组件与布局 | `UIBuilder` 提供 `panel` / `row` / `column` / `table` / `canvas` / `label` / `icon` 工厂；`LayoutEngine` 执行 measure → place → emit |
| 动态数据 | `Bindable<T>` 惰性取值，支持 `Bindable.supplier(...)` 每帧刷新 |
| 屏幕 UI | `ScreenRenderTarget` + 多种 `ScreenAnchor` 锚点，支持对齐、缩放 |
| 自定义绘制 | `canvas()` + `CanvasContext`，提供矩形填充、纹理、图集 sprite、文字、物品图标等原语 |
| 扩展 HUD | `HudBar`、`BuffIcon`、`BlockInfoProvider`、`WorldMarkerProvider` 四类注入接口 |
| 内置 HUD | 状态 HUD、瞄准信息 HUD、Buff HUD、3D 血条，均可独立配置开关 |
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
| `enableCustomHud` | true | 启用左下角状态 HUD（创造 / 旁观者自动隐藏） |
| `enableTargetInfo` | true | 启用右下角瞄准信息 |
| `showArmorDurability` | true | 显示盔甲耐久 |
| `showHeldDurability` | true | 显示手持物品耐久 |
| `enableWorldHealthBar` | true | 启用 3D 血条 |
| `showNeutralHealthBar` | true | 显示中立生物血条 |
| `hideVanillaBars` | true | 隐藏原版状态条 |
| `showDemo` | false | 显示左上角 demo 面板 |

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
