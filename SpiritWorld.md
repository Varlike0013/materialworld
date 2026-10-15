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

数据生成命令: ./gradlew runData
输入（手写）:
    src/main/resources/assets/spiritworld/textures/item/    ⬅️ 纹理
输出（自动生成）:
    src/generated/resources/assets/spiritworld/models/item/
    src/generated/resources/assets/spiritworld/lang/
已删除的手写文件:
    src/main/resources/assets/spiritworld/lang/en_us.json
    src/main/resources/assets/spiritworld/lang/zh_cn.json
    src/main/resources/assets/spiritworld/models/item/bottled_spirit.json

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

## 物品设计 / Item Design

瓶装精神力 / Bottled Spirit:
    定位: 类似原版水瓶的可饮用道具，恢复精神力。
    堆叠: 8（。
    使用动画: UseAnim.DRINK，时长 32 tick。
    恢复量: Config.bottledSpiritRestoreAmount，默认 50。
    返还: 饮用后返还玻璃瓶。
    边界: 精神力已满时仍消耗瓶子。
    创造模式: 不消耗瓶子也不返还玻璃瓶。
    创造标签: 通过 MaterialWorldAPI.addToAddonTab 添加到物质世界的附属标签，
              分组名 materialworld.addon_group.spirit_world。

精神秘戒 / Spirit Rings:
    种类: 精神铁秘戒(100)、精神金秘戒(300)、精神钻秘戒(500)。
    堆叠: 1。
    数据存储: SpiritStored 数据组件，记录已存精神力量。
    右键: 从玩家存入精神力到戒指。
    潜行右键: 从戒指取出精神力给玩家。
    右键含水炼药锅: 消耗 100 精神力，将水炼药锅转化为精神药水炼药锅。
    稀有度: 铁-UNCOMMON，金-RARE，钻-EPIC。

精神药水炼药锅 / Spirit Cauldron:
    定义: 精神药水的载体，水位 1-3。
    玻璃瓶右键: 取出瓶装精神力，水位 -1。
    水位归零: 变回空炼药锅。
    视觉: 暂时使用原版水炼药锅模型。

## 资源文件 / Resources

models/item/bottled_spirit.json:
    物品模型，parent 为 item/generated，纹理指向 spiritworld:item/bottled_spirit。

textures/item/bottled_spirit.png:
    16×16 物品纹理，可暂时使用原版 glass_bottle.png 占位。

## 数据组件 / Data Components

ModDataComponents src/main/java/com/varlike/spiritworld/component/ModDataComponents.java:
    SPIRIT_STORED: DataComponentType<SpiritStored> 静态实例。
    register(IEventBus modEventBus): 注册到 DATA_COMPONENT_TYPE 注册表。

SpiritStored src/main/java/com/varlike/spiritworld/component/SpiritStored.java:
    amount(): 存储的精神力量。
    CODEC: 持久化编解码器。
    STREAM_CODEC: 网络同步编解码器。

## 按键与界面入口 / Keybind & Screen Entry

SpiritKeyBindings src/main/java/com/varlike/spiritworld/client/SpiritKeyBindings.java:
    CATEGORY: "key.categories.materialworld"，使用物质世界的按键分类，
              在控制设置界面中显示在物质世界标题下方。
    OPEN_SKILL_TREE: KeyMapping，默认按键 K，用于打开技能树界面。

SpiritClientEvents src/main/java/com/varlike/spiritworld/client/SpiritClientEvents.java:
    ModBus.onRegisterKeys(RegisterKeyMappingsEvent): 在 mod 总线注册按键映射。
    GameBus.onClientTick(ClientTickEvent.Post): 每 tick 检测按键，在无界面时打开 SkillTreeScreen。

## 内部退点接口 / Skill Refund Helper

SkillRefundHelper src/main/java/com/varlike/spiritworld/skill/SkillRefundHelper.java:
    resetAllSkills(Player): 重置全部技能，返还所有已消耗灵视点，返回是否成功。
    resetSkill(Player, ResourceLocation): 重置单个技能，返还该技能已消耗灵视点。
    用途: 供精神世界自己的物品（如退点道具）调用，不对外暴露。

## 本地化 / Localization

ModLanguageProvider / ModLanguageProviderZH 新增:
    按键: key.spiritworld.open_skill_tree
    技能树界面: screen.spiritworld.skill_tree.*
    技能类型: skill.type.*
    技能提示: skill.tooltip.*
    示例技能: skill.spiritworld.spirit_sense / spirit_burst / spirit_focus / spirit_vision / spirit_storm / spirit_echo

变更:
    所有语言键统一在 datagen 中维护，不再手写 lang/en_us.json 和 lang/zh_cn.json。
    按键分类使用物质世界的分类键，在控制设置中显示在物质世界下方。
    
## 数据生成 / Data Generation

ModItemModelProvider src/main/java/com/varlike/spiritworld/datagen/ModItemModelProvider.java:
    ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper): 构造函数。
    registerModels(): 为 BOTTLED_SPIRIT 生成基础 2D 模型。
    输出: src/generated/resources/assets/spiritworld/models/item/bottled_spirit.json

ModLanguageProvider src/main/java/com/varlike/spiritworld/datagen/ModLanguageProvider.java:
    ModLanguageProvider(PackOutput output): 构造函数，locale 固定为 en_us。
    addTranslations(): 添加物品名、消息、配置项的英文翻译。
    输出: src/generated/resources/assets/spiritworld/lang/en_us.json

ModLanguageProviderZH src/main/java/com/varlike/spiritworld/datagen/ModLanguageProviderZH.java:
    ModLanguageProviderZH(PackOutput output): 构造函数，locale 固定为 zh_cn。
    addTranslations(): 添加中文翻译。
    输出: src/generated/resources/assets/spiritworld/lang/zh_cn.json

DataGenerators src/main/java/com/varlike/spiritworld/datagen/DataGenerators.java:
    gatherData(GatherDataEvent event): 注册三个 DataProvider（物品模型、英文、中文）。ModRecipeProvider追加配方。
        使用 event.includeClient() 限制为客户端资源生成。gatherData 中追加 ModBlockStateProvider 注册，使用 event.includeClient() 归类为客户端资源。

ModRecipeProvider src/main/java/com/varlike/spiritworld/datagen/ModRecipeProvider.java:
    buildRecipes(RecipeOutput output): 生成三种戒指的合成配方。
        精神铁秘戒: 4 铁锭菱形图案。
        精神金秘戒: 4 金锭菱形图案。
        精神钻秘戒: 4 钻石菱形图案。

ModBlockStateProvider src/main/java/com/varlike/spiritworld/datagen/ModBlockStateProvider.java:
    ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper): 构造函数。
    registerStatesAndModels(): 为 SPIRIT_CAULDRON 生成 blockstate JSON，
        引用原版 minecraft:block/water_cauldron_level1/level2/full 三个模型。
    输出: src/generated/resources/assets/spiritworld/blockstates/spirit_cauldron.json

## 技能树设计 / Skill Tree Design

图形与类型:
    ACTIVE（主动）: 正方形
    PASSIVE（被动）: 圆形
    SPECIAL（特殊）: 六边形
    形状表示类型，颜色表示状态。

状态配色:
    LEARNED: 绿边框 + 深绿填充
    LEARNABLE: 金边框 + 深金填充 + 呼吸光晕
    LOCKED: 灰边框 + 深灰填充（前置未满足或灵视等级不足）
    NO_POINTS: 红边框 + 深红填充
    MAX: 蓝边框 + 深蓝填充

灵视等级计算:
    公式: n(n+1)/2*10 ≤ 已消耗灵视点总数
    Lv1=10, Lv2=30, Lv3=60, Lv4=100, Lv5=150...

学习判定:
    1. 节点等级 < maxLevel
    2. 所有前置节点等级 ≥ 1
    3. 玩家灵视等级 ≥ node.requiredVisionLevel
    4. 可用灵视点 ≥ node.costPerLevel

滚动与拖动:
    滚轮: 垂直滚动
    Shift + 滚轮: 水平滚动
    拖动空白区: 自由平移
    无可见滚动条

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
    变更: 取消 ModItems.register 注释，新增 MaterialWorldAPI.addToAddonTab 调用，
          将 BOTTLED_SPIRIT 添加到物质世界的附属创造标签，分组名 materialworld.addon_group.spirit_world。

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
    新增 BOTTLED_SPIRIT_RESTORE_AMOUNT: 瓶装精神力恢复量，默认 20，范围 [1, 10000]。
    变更: onLoad 中同步 bottledSpiritRestoreAmount 到静态字段。

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

ModItems src/main/java/com/varlike/spiritworld/registry/ModItems.java:
    ITEMS: DeferredRegister.Items 静态实例，用于注册精神世界的物品。
    BOTTLED_SPIRIT: 瓶装精神力物品的 DeferredItem，默认堆叠数为 1。
    register(IEventBus modEventBus): 将 ITEMS 注册器绑定到 mod 事件总线。

BottledSpiritItem src/main/java/com/varlike/spiritworld/item/BottledSpiritItem.java:
    BottledSpiritItem(Properties properties): 构造函数。
    use(Level level, Player player, InteractionHand hand): 开始饮用动画。
    getUseAnimation(ItemStack stack): 返回 UseAnim.DRINK。
    getUseDuration(ItemStack stack, LivingEntity entity): 返回 32 tick。
    finishUsingItem(ItemStack stack, Level level, LivingEntity entity): 服务端恢复 Config.bottledSpiritRestoreAmount 点精神力，
        同步客户端并发送提示；消耗瓶子并返还玻璃瓶；精神力已满时仍消耗瓶子但发送「已满」提示。

SpiritRingItem src/main/java/com/varlike/spiritworld/item/SpiritRingItem.java:
    SpiritRingItem(long capacity, Properties properties): 构造函数，指定容量。
    getCapacity(): 返回容量。
    getStored(ItemStack stack): 静态，读取已存精神力量。
    setStored(ItemStack stack, long amount): 静态，设置已存精神力量。
    use(Level, Player, InteractionHand): 右键逻辑，存入/取出精神力。
    useOn(UseOnContext): 右键含水炼药锅转化为精神药水炼药锅。
    appendHoverText(...): 显示已存精神力量和用法。

SpiritCauldronBlock src/main/java/com/varlike/spiritworld/block/SpiritCauldronBlock.java:
    SpiritCauldronBlock(Properties properties): 构造函数，降水类型为 NONE。
    useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult):
        玻璃瓶右键取出瓶装精神力，水位 -1，减到 0 变回空炼药锅。
        变更: 返回值类型为 ItemInteractionResult（非 InteractionResult），
              PASS 对应 PASS_TO_DEFAULT_BLOCK_INTERACTION。
    getInteractionShape(...): 返回完整碰撞形状。

ModBlocks src/main/java/com/varlike/spiritworld/registry/ModBlocks.java:
    BLOCKS: DeferredRegister.Blocks 静态实例。
    SPIRIT_CAULDRON: 精神药水炼药锅的 DeferredBlock。
    register(IEventBus modEventBus): 绑定 BLOCKS 到 mod 事件总线。

SkillType src/main/java/com/varlike/spiritworld/skill/SkillType.java:
    ACTIVE / PASSIVE / SPECIAL 三种技能类型枚举。

SkillNode src/main/java/com/varlike/spiritworld/skill/SkillNode.java:
    数据类，包含 id、type、gridX、gridY、maxLevel、costPerLevel、requiredVisionLevel、prerequisites、icon、nameKey、descKey。
    builder(ResourceLocation): 构建器入口。

SkillTree src/main/java/com/varlike/spiritworld/skill/SkillTree.java:
    register(SkillNode): 注册技能节点。
    get(ResourceLocation): 按 id 获取节点。
    getAll(): 返回全部节点集合。
    exists(ResourceLocation): 判断节点是否存在。

ModSkills src/main/java/com/varlike/spiritworld/skill/ModSkills.java:
    registerAll(): 注册内置示例技能。

ServerboundLearnSkillPacket src/main/java/com/varlike/spiritworld/network/ServerboundLearnSkillPacket.java:
    handle(...): 服务端处理加点请求，校验上限/前置/灵视等级/点数后应用并同步。

SkillRenderer src/main/java/com/varlike/spiritworld/client/skill/SkillRenderer.java:
    fillSquare / fillCircle / fillHexagon: 绘制三种形状。
    fillShape: 组合边框色与填充色。
    fillGlow: 绘制呼吸光晕。

SkillTreeScreen src/main/java/com/varlike/spiritworld/client/skill/SkillTreeScreen.java:
    init(): 计算网格边界、居中视图。
    render(...): 渲染背景、连线、节点、顶栏、Tooltip。
    getState(SkillNode): 判定节点状态。
    renderConnections / renderNodes / renderTopBar / renderTooltip: 各层渲染。
    mouseClicked / mouseReleased / mouseDragged / mouseScrolled: 交互处理。

SpiritWorldAPI 新增:
    resetAllSkills(Player): 重置全部技能。
    resetSkill(Player, ResourceLocation): 重置单个技能。

PlayerSpirit 变更:
    移除 visionLevel 字段（改为动态计算）。
    新增 learnedSkills、availableVisionPoints、totalSpentVisionPoints。
    新增 getVisionLevel() 根据 totalSpentVisionPoints 动态计算。

ClientboundSyncSpiritPacket 变更:
    新增 availableVisionPoints、totalSpentVisionPoints、learnedSkills 字段。

ClientSpiritData 变更:
    同步新字段，并提供 getVisionLevel()、getSkillLevel(ResourceLocation)。

## 技能系统 v2 / Skill System v2

技能行为标记（SkillNode）:
    equippable: 是否可放入技能栏
    toggleable: 是否可开关（被动）
    alwaysOn: 是否常驻激活

Builder 快捷方法:
    passiveAlways()      常驻被动
    passiveToggle()      装配页开关被动
    passiveEquippable()  装配页开关 + 可装备被动
    active()             主动技能
    specialEquippable()  特殊技能（可装备）
    specialDisplay()     特殊技能（仅展示）

技能栏与被动:
    PlayerSpirit.loadout: ResourceLocation[6]，6 个槽位
    PlayerSpirit.selectedSlot: 当前选中槽位
    PlayerSpirit.activePassives: Set<ResourceLocation>，开关被动激活集合
    PlayerSpirit.isPassiveActive(id): 判断被动是否生效，常驻直接返回 true

按键（SpiritKeyBindings，分类 key.categories.materialworld）:
    K: 打开技能树
    L: 打开技能装配页面
    G: 打开技能选盘（按下立刻打开，松开确认）
    V: 使用当前槽位技能

界面:
    SkillTreeScreen: 学习加点
    SkillLoadoutScreen: 6 槽环形 + 技能列表 + 被动开关
    RadialMenuScreen: 圆环选盘，中心镂空，不遮挡游戏画面

HUD（SpiritHudOverlay）:
    精神力条上方: 已激活被动图标（行列可配置）
    精神力条右侧: 当前装备技能图标 + 槽位编号
    ClientConfig:
        hudPassiveMaxRows: 被动图标最多行数（默认 1）
        hudPassivePerRow: 每行数量（默认 8）
        hudPassiveIconSize: 图标尺寸（默认 12）

EMC 转化技能:
    ID: spiritworld:emc_conversion
    类型: passiveEquippable
    初始等级: 1（玩家创建时自动获得并装配到 0 号槽位）
    效果: 激活时持续消耗 EMC 转化为灵视点
    参数公式与配置项同前。

## 网络包 / Network Packets

ServerboundLearnSkillPacket: 加点
ServerboundSetLoadoutPacket: 设置槽位技能
ServerboundSetSelectedSlotPacket: 切换选中槽位
ServerboundTogglePassivePacket: 开关被动
ServerboundUseSkillPacket: 使用当前槽位技能

ClientboundSyncSpiritPacket 新增字段:
    loadout: 6 个槽位
    selectedSlot: 当前选中槽位
    activePassives: 激活被动集合

## 类与函数记录 / Class & Function Log

SkillNode 变更:
    新增 equippable、toggleable、alwaysOn 字段及 Builder 快捷方法。

ModSkills 变更:
    新增 EMC_CONVERSION_ID。
    registerAll() 中 6 个示例技能：spirit_sense（常驻被动）、emc_conversion（可装备被动）、
    spirit_focus（开关被动）、spirit_burst（主动）、spirit_vision（特殊可装备）、
    spirit_eye_lore（特殊仅展示）。

PlayerSpirit 变更:
    新增 loadout[6]、selectedSlot、activePassives 字段。
    新增 setLoadoutSlot / getLoadoutSlot / getSelectedSlot / setSelectedSlot / getEquippedSkill /
         getActivePassives / addPassive / removePassive / isPassiveActive 方法。
    save/load/copyFrom 更新。
    setSkillLevel 中追加清理技能栏和被动状态。

PlayerSpiritProvider 变更:
    首次创建玩家数据时初始化 EMC_CONVERSION_ID 技能 lv1 并装配到 0 号槽位。

Config 变更:
    新增 5 个 EMC 转化配置项。

ClientConfig 变更:
    新增 3 个 HUD 被动图标显示配置项。

SpiritKeyBindings 变更:
    新增 OPEN_LOADOUT(L)、OPEN_RADIAL(G)、USE_SKILL(V)。

ServerboundSetLoadoutPacket / ServerboundSetSelectedSlotPacket /
ServerboundTogglePassivePacket / ServerboundUseSkillPacket:
    新增或重写，服务端处理对应操作并同步。

ClientboundSyncSpiritPacket 变更:
    新增 loadout、selectedSlot、activePassives 字段。

ClientSpiritData 变更:
    同步新字段，新增 getLoadoutSlot / getSelectedSlot / getEquippedSkill / isPassiveActive。

SkillLoadoutScreen src/main/java/com/varlike/spiritworld/client/skill/SkillLoadoutScreen.java:
    新增装配页面，6 槽环形 + 已学技能列表 + 被动开关区。

RadialMenuScreen src/main/java/com/varlike/spiritworld/client/skill/RadialMenuScreen.java:
    新增选盘页面，圆环 6 扇区，中心镂空，不遮挡游戏画面。

SpiritHudOverlay 变更:
    新增被动图标横排显示（可配置行列）和当前装备技能图标。

SkillTickHandler 重写:
    处理 EMC 转化技能的激活逻辑，EMC 不足时自动关闭。

## 物品设计 / Item Design

精神盐 / Spirit Salt:
    来源: 熔炼瓶装精神力。
    用途: 待定（后续作为合成材料）。

## 配方 / Recipes

ModRecipeProvider 新增:
    spirit_salt_from_smelting: 熔炉，瓶装精神力 → 精神盐，经验 0.3，时间 200 tick。
    可选: 已添加spirit_salt_from_blasting: 高炉，时间 100 tick。

## 类与函数记录 / Class & Function Log

ModItems 变更:
    SPIRIT_SALT: 精神盐的 DeferredItem，普通 Item。

ModItemModelProvider 变更:
    registerModels 中追加 spirit_salt 的 basicItem 生成。

ModLanguageProvider / ModLanguageProviderZH 变更:
    新增 item.spiritworld.spirit_salt 翻译。

## 资源文件 / Resources

textures/item/spirit_salt.png:
    16×16 物品纹理，可暂时用原版 sugar.png 或 gunpowder.png 改色占位。