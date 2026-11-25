# 客户端与 HUD 设计

## 1. 客户端数据层

- `ClientSpiritData` 是服务端 `PlayerSpirit` 的只读镜像，由 `ClientboundSyncSpiritPacket.handle()` 调用 `update(...)` 刷新。
- 提供 `getSpirit/getMaxSpirit/getVisionLevel/getSkillLevel/isPassiveActive/getEquippedSkill` 等查询接口。
- 退服或切换世界时 `reset()` 清空，避免残留上一个存档的数据。
- `ClientScanData` 缓存扫描结果的图标、数量与淡出比例。

## 2. AetherUI 集成

`client/aether/SpiritAetherIntegration.init()` 注册三类内容：

| 组件 | 类 | 作用 |
|---|---|---|
| HUD 条 | `SpiritHudBar` | 左下角精神力条，含魔力数值与灵视等级 |
| Buff 图标 | `SpiritPassiveIcon` | 物品栏上方的被动技能图标 |
| 方块信息 | `SpiritBlockInfoProvider` | 按灵视等级显示方块/BlockEntity 附加信息 |

`CanvasShapes` 提供圆、方、六边形绘制，供 HUD 与技能图标复用。AetherUI 的统一 HUD 树负责所有条目的纵向排布，Spirit World 不直接监听原版 RenderGui 事件。

## 3. 精神力条布局

HUD Canvas 尺寸为 `178 × 12` 逻辑像素。前 `142 × 12` 与 AetherUI 血条使用同一套 `CanvasStyles.barWithValue`，布局如下：

| 元素 | 坐标 (x, y) | 尺寸 | 说明 |
|---|---|---|---|
| 蓝条主体 | (0, 0) | 142 × 12 | 与血条同宽同高、同深色槽、同条内文字样式 |
| 魔力数值 | (4, 1) | — | 文本 `魔力 当前 / 最大`，白色描边，受 `showSpiritText` 控制 |
| 灵视等级方块 | (118, 0) | 24 × 12 | 与血条护甲数值方块同位置、同样式，数字为蓝色 |
| 飞行状态图标 | (154, 3.5) | 半径 3.5 | 仅飞行被动激活时显示 |
| 装配技能图标 | (154, 8.5) | 半径 3.5 | 当前选中槽技能，位于灵视等级右侧下方 |
| 槽位编号 | (165, 2) | — | 当前技能栏编号 |

设计要点：

- 蓝条直接调用 `CanvasStyles.barWithValue`，不再自绘背景和文字，因此与血条外观一致。
- 前 142 像素与血条完全对齐；额外 36 像素只承载状态/技能图标，不参与条体。
- 填充色为蓝色 `0xFF3399FF`，数值方块文字同色。
- 当 `baseMaxSpirit > maxSpirit` 时，条尾用红色段表示被扣掉的上限。

## 4. 扫描矿物的世界标记

`client/skill/SpiritScanRenderer` 使用 AetherUI `WorldRenderTarget` 把矿物图标投影到屏幕空间。

### 4.1 生命周期

1. `ClientboundScanResultPacket` 写入 `ClientScanData`。
2. `onClientTick(ClientTickEvent.Post)` 读取激活图标，按坐标字符串作为 key 复用/创建 UITree。
3. 维度切换、Config 关闭世界 UI、退出世界时清空全部图标。
4. 超过 `MAX_DISTANCE=56` 的图标不渲染；图标按 `RISE=1.6` 抬升到方块中心上方。

### 4.2 缩放策略

AetherUI 默认按“世界中 1 格在屏幕上的投影长度 / pixelsPerBlock”计算缩放。该值同时受距离和视线夹角影响，正对与斜看时大小不同。

Spirit World 每帧按相机方向与图标方向的夹角 `cosθ` 反补偿，并叠加 GUI 像素焦距 `f = guiHeight / (2 tan(fovY/2))`：

\[
\text{distanceScale} = \frac{f}{\text{distance} \times \text{BASE\_PIXELS\_PER\_BLOCK}}
\]

\[
\text{desiredScale} = \text{clamp}(\text{distanceScale},\ \text{scanIconMinScale},\ \text{scanIconMaxScale})
\]

\[
\text{pixelsPerBlock} = \frac{f / (\text{distance} \times \max(0.05,\cos\theta))}{\text{desiredScale}}
\]

因此最终缩放只随距离衰减，并在客户端配置 `scanIconMinScale` / `scanIconMaxScale` 之间钳制：距离很远的图标被补偿到最小尺寸，不会缩到看不见；很近的图标也不会无限放大。图标锚点带轻微正弦浮动，标签显示矿物簇数量与玩家到矿物的距离。

## 5. GUI 界面

| 界面 | 类 | 用途 |
|---|---|---|
| 精神核心 | `SpiritCoreScreen` | 魔力存储、属性明细、配方合成 |
| 核心网络 | `SpiritCoreNetworkScreen` | 完整节点树、段效率/总效率、断链原因、离线收益与抽取明细 |
| 技能树 | `SkillTreeScreen` | 技能解锁、等级、前置与消耗展示 |
| 技能装配 | `SkillLoadoutScreen` | 6 槽装配、被动开关、筛选 |
| 技能选盘 | `RadialMenuScreen` | 环形快捷选择当前技能 |
| 扫描配置 | `ScanConfigScreen` | 扫描范围与目标矿物设置 |

按键由 `SpiritKeyBindings` 定义，`SpiritClientEvents` 负责注册渲染器、按键与缓存重置。
