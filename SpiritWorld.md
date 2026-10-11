# SpiritWorld(精神世界) 开发记录

> 规则 / Rules：
> 1. 每创建或迁移一个类，必须记录类名、位置、函数名、函数作用。  
> 2. 位置使用相对源码路径。  

## 项目信息

| 项目 | 值 |
|---|---|
| Minecraft 版本 | 1.21.1 |
| NeoForge 版本 | 21.1.256 |
| 模组 ID | spiritworld(com.varlike.spiritworld) |
| 模组名称 | SpiritWorld(精神世界) |
| 前置模组 | materialworld (物质世界) |
| 描述 | 物质世界的附属模组，为玩家引入精神力与灵视系统。|

## 构建与元数据 / Build & Metadata

./gradlew clean build
./gradlew runClient

neoforge.mods.toml src/main/resources/META-INF/neoforge.mods.toml:
    modLoader="javafml": 声明使用 Java 版 mod 加载器。
    [[mods]]: 声明 modid=spiritworld，版本、名称、描述等元数据。
    [[dependencies.spiritworld]] neoforge: 必需依赖，版本范围 [21.1.256,)，BOTH。
    [[dependencies.spiritworld]] minecraft: 必需依赖，版本范围 [1.21.1,1.22)，BOTH。
    [[dependencies.spiritworld]] materialworld: 必需前置，版本范围 [1.0.0,)，AFTER，BOTH。

build.gradle build.gradle:
    仓库配置: mavenLocal() 用于解析 materialworld 依赖。
    依赖配置: compileOnly + runtimeOnly com.varlike.materialworld:materialworld:1.0.0。

## 玩家属性系统设计 / Player Attribute Design

精神力 / Spirit:
    定义: 类似其他游戏蓝量的资源池，用于驱动精神世界相关能力。
    范围: 0 到 maxSpirit，默认上限 100。
    消耗: 使用精神世界能力时扣除。
    恢复: 使用瓶装精神力或相关道具恢复。
    同步: 服务端 → 客户端单向同步，客户端只读。

灵视 / Spirit Vision:
    定义: 类似挖掘等级的能力等级，决定玩家能看到和挖掘的精神世界方块。
    范围: 0 到 MAX_VISION_LEVEL，默认 0。
    作用: 控制精神世界相关方块的可见性和可交互性。
    同步: 与精神力一同同步到客户端。

精神力恢复机制:
    间隔: Config.spiritRegenInterval（默认 40 tick = 2 秒）
    量: Config.spiritRegenAmount（默认 1）
    条件: spirit < maxSpirit
    同步节流: Config.syncInterval（默认 10 tick = 0.5 秒）

精神力上限修改:
    方法: PlayerSpirit.setMaxSpirit(long)
    特性: 自动将当前 spirit 夹到新上限，不会超过。
    场景: 升级奖励、装备加成、buff 等。

数据流向:
    服务端 tick → 恢复 → markDirty
    服务端 tick → 节流同步 → syncTo(player)
    客户端 → ClientSpiritData.update → HUD 读取

临时上限加成机制:
    存储: PlayerSpirit.maxSpiritModifiers，Map<ResourceLocation, Long>，transient，不序列化。
    生效: getEffectiveMaxSpirit() = maxSpirit + sum(modifiers)，最小值 0。
    添加: addMaxSpiritModifier(id, amount)，同 id 覆盖。
    移除: removeMaxSpiritModifier(id)，自动夹当前精神力。
    清除: clearMaxSpiritModifiers()，用于死亡重生。
    用途: buff、装备加成、药水效果等临时性上限变化。

死亡重生处理:
    复制数据: 保留基础上限、灵视等级、当前精神力。
    清空 modifier: 死亡后 buff 效果不保留。
    清零开关: Config.spiritClearOnDeath，默认 true。

首次加入初始化:
    触发: PlayerSpiritProvider.getCapability 首次为玩家创建数据时。
    行为: 上限使用 Config.defaultMaxSpirit，当前精神力初始化为 0，标记 initialized。
    Fallback: 配置未加载时使用 PlayerSpirit.DEFAULT_MAX_SPIRIT。

登出清理:
    触发: PlayerEvent.PlayerLoggedOutEvent。
    行为: 从 PlayerSpiritProvider.dataMap 移除该 UUID，防止内存泄漏。

精神力生命周期:
    首次创建: spirit = 0, maxSpirit = 配置值, visionLevel = 0
    登录: spirit = 0（可通过 Config.spiritClearOnLogin 关闭）
    存活期间: 每 Config.spiritRegenInterval tick 恢复 Config.spiritRegenAmount
    死亡重生: spirit = 0（可通过 Config.spiritClearOnDeath 关闭），modifier 清空
    登出: 内存缓存移除

清空开关:
    spiritClearOnLogin: 默认 true，控制登录时清零。
    spiritClearOnDeath: 默认 true，控制死亡重生时清零。
    两者均可通过配置关闭以方便调试。
## HUD 设计 / HUD Design

精神力条:
    位置: 屏幕右下角，右对齐饥饿条，位于饥饿条正上方。
    尺寸: 宽度 81 像素（与 9 个饥饿图标对齐），高度 5 像素。
    颜色: 深蓝底 + 亮蓝上半 + 深蓝下半，视觉立体。
    文本: 可选显示 "当前值 / 上限"。
    显示条件: 非创造模式、非旁观模式、未隐藏 GUI、showSpiritBar 为 true。
    Y 轴偏移: 由 ClientConfig.spiritBarYOffset 控制，正值向上。

## 本地化 / Localization

en_us.json src/main/resources/assets/spiritworld/lang/en_us.json:
    英文语言文件，包含通用配置、客户端配置的翻译键。

zh_cn.json src/main/resources/assets/spiritworld/lang/zh_cn.json:
    中文语言文件，与 en_us.json 键名一一对应。


## 公开 API / Public API

SpiritWorldAPI src/main/java/com/varlike/spiritworld/api/SpiritWorldAPI.java:
    MOD_ID: 模组 ID 常量。
    API_VERSION: API 版本号。
    getPlayerSpirit(Player player): 获取玩家精神力数据接口，未初始化时返回 null。
    getSpirit(Player player): 快捷方法，返回当前精神力，无数据时返回 0。
    getMaxSpirit(Player player): 快捷方法，返回精神力上限。
    getVisionLevel(Player player): 快捷方法，返回灵视等级。

IPlayerSpirit src/main/java/com/varlike/spiritworld/api/IPlayerSpirit.java:
    getSpirit(): 返回当前精神力。
    setSpirit(long value): 设置精神力，自动夹在 [0, maxSpirit] 范围。
    getMaxSpirit(): 返回精神力上限。
    setMaxSpirit(long value): 设置精神力上限。
    addSpirit(long amount): 增加精神力。
    consumeSpirit(long amount): 尝试扣除精神力，不足返回 false。
    getVisionLevel(): 返回灵视等级。
    setVisionLevel(int level): 设置灵视等级。
    isVisionAtLeast(int level): 判断灵视等级是否达到指定值。

## 类与函数记录 / Class & Function Log

SpiritWorld src/main/java/com/varlike/spiritworld/SpiritWorld.java:
    SpiritWorld(IEventBus modEventBus, ModContainer modContainer): 主构造函数，注册能力、物品、创造标签和配置。

PlayerSpirit src/main/java/com/varlike/spiritworld/capability/PlayerSpirit.java:
    maxSpiritModifiers: Map<ResourceLocation, Long>，存储临时上限加成（不序列化）。
    getEffectiveMaxSpirit(): 返回基础上限 + 所有临时加成之和，含 Math.max(0, ...) 保护。
    addMaxSpiritModifier(ResourceLocation id, long amount): 添加或覆盖临时上限加成，自动夹当前精神力。
    removeMaxSpiritModifier(ResourceLocation id): 移除指定来源的临时加成，自动夹当前精神力。
    clearMaxSpiritModifiers(): 清空所有临时加成。
    getMaxSpiritModifiers(): 返回不可修改的加成映射。
    setSpirit / addSpirit / consumeSpirit / setMaxSpirit: 全部改用 getEffectiveMaxSpirit() 作为上限判断。
    isInitialized(): 判断数据是否已初始化。
    markInitialized(): 标记为已初始化。

PlayerSpiritProvider src/main/java/com/varlike/spiritworld/capability/PlayerSpiritProvider.java:
    getCapability(Entity entity, Void context): 首次创建玩家数据时，上限按配置设置，但精神力初始化为 0。
        变更: data.setSpirit(defaultMax) → data.setSpirit(0L)。
    remove(UUID uuid): 移除指定玩家数据，用于登出清理

ModCapabilities src/main/java/com/varlike/spiritworld/capability/ModCapabilities.java:
    PROVIDER: PlayerSpiritProvider 静态实例。
    PLAYER_SPIRIT: EntityCapability<PlayerSpirit, Void> 静态实例。
    register(RegisterCapabilitiesEvent event): 注册玩家实体能力。

SpiritGameHandler src/main/java/com/varlike/spiritworld/capability/SpiritGameHandler.java:
    onPlayerClone(PlayerEvent.Clone event): 死亡重生时复制数据、清空 modifier，并按 Config.spiritClearOnDeath 清零精神力。
    onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event): 玩家登录时按 Config.spiritClearOnLogin 清零精神力，然后同步客户端。
    onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event): 玩家登出时移除内存缓存。

SpiritTickHandler src/main/java/com/varlike/spiritworld/capability/SpiritTickHandler.java:
    onPlayerTick(PlayerTickEvent.Post event): 服务端每 tick 检查玩家精神力，按间隔恢复（判断使用有效上限），按节流间隔同步客户端。

NetworkHandler src/main/java/com/varlike/spiritworld/network/NetworkHandler.java:
    syncTo(ServerPlayer player): 同步 spirit、effectiveMaxSpirit、visionLevel 到客户端。
        变更: 同步字段从 getMaxSpirit() 改为 getEffectiveMaxSpirit()。

ClientboundSyncSpiritPacket src/main/java/com/varlike/spiritworld/network/ClientboundSyncSpiritPacket.java:
    encode(RegistryFriendlyByteBuf buf): 写入精神力、上限和灵视等级。
    ClientboundSyncSpiritPacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造。
    type(): 返回载荷类型标识。

ServerboundRequestSyncPacket src/main/java/com/varlike/spiritworld/network/ServerboundRequestSyncPacket.java:
    encode(RegistryFriendlyByteBuf buf): 空实现。
    ServerboundRequestSyncPacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造。
    handle(ServerboundRequestSyncPacket pkt, IPayloadContext context): 服务端收到请求后回发同步包。
    type(): 返回载荷类型标识。

ClientPayloadHandler src/main/java/com/varlike/spiritworld/network/ClientPayloadHandler.java:
    handleSyncSpirit(ClientboundSyncSpiritPacket pkt, IPayloadContext context): 客户端专用处理器，更新 ClientSpiritData。

ClientSpiritData src/main/java/com/varlike/spiritworld/client/ClientSpiritData.java:
    getSpirit(): 返回客户端缓存的精神力。
    getMaxSpirit(): 返回客户端缓存的精神力上限。
    getVisionLevel(): 返回客户端缓存的灵视等级。
    update(long newSpirit, long newMaxSpirit, int newVisionLevel): 用同步包数据更新缓存。

Config src/main/java/com/varlike/spiritworld/Config.java:
    SPIRIT_REGEN_AMOUNT: 每次恢复的精神力量。
    SPIRIT_REGEN_INTERVAL: 恢复间隔（tick）。
    DEFAULT_MAX_SPIRIT: 新玩家默认精神力上限。
    SYNC_INTERVAL: 客户端同步间隔（tick）。
    SPIRIT_CLEAR_ON_LOGIN: 玩家登录时是否清空精神力，默认 true。
    SPIRIT_CLEAR_ON_DEATH: 玩家死亡重生时是否清空精神力，默认 true。
    onLoad(ModConfigEvent event): 配置加载/重载时同步到静态字段。

SpiritWorldClient src/main/java/com/varlike/spiritworld/SpiritWorldClient.java:
    SpiritWorldClient(ModContainer container): 客户端模组构造函数，注册配置界面扩展点和 CLIENT 配置。
    onClientSetup(FMLClientSetupEvent event): 客户端初始化时输出日志和当前玩家名。

ClientConfig src/main/java/com/varlike/spiritworld/ClientConfig.java:
    SPEC: CLIENT 类型配置规格。
    spiritBarYOffset: 精神力条相对饥饿条的 Y 轴偏移（像素）。
    showSpiritBar: 是否显示精神力条。
    showSpiritText: 是否显示精神力数值。
    onLoad(ModConfigEvent event): 配置加载/重载时同步到静态字段。

SpiritHudOverlay src/main/java/com/varlike/spiritworld/client/SpiritHudOverlay.java:
    BAR_WIDTH: 精神力条宽度（81 像素，与饥饿条一致）。
    BAR_HEIGHT: 精神力条高度（5 像素）。
    FOOD_BAR_BOTTOM_OFFSET: 饥饿条顶部距屏幕底部的距离（39 像素）。
    GAP: 精神力条与饥饿条的间距（2 像素）。
    LAYER_ID: GUI 层的 ResourceLocation (spiritworld:spirit_bar)。
    onRegisterGuiLayers(RegisterGuiLayersEvent event): 在饥饿条之上注册精神力条 GUI 层。
    renderSpiritBar(GuiGraphics graphics, DeltaTracker deltaTracker): 渲染精神力条，
        包括背景、空槽、蓝色渐变填充和可选文本，位置右对齐饥饿条并支持 Y 轴偏移。