# NeoForge Mod 迁移与开发记录

> 规则 / Rules：
> 1. 每创建或迁移一个类，必须记录类名、位置、函数名、函数作用。  
> 2. 位置使用相对源码路径。  

## 项目信息

| 项目 | 值 |
|---|---|
| Minecraft 版本 | 1.21.1 |
| NeoForge 版本 | 21.1.256 |
| 模组 ID | materialworld(com.varlike.materialworld) |
| 模组名称 | MaterialWorld(物质世界) |
| 描述 | 一个类似ProjectE 的物品转换mod，是其他如精神世界的前置。|

## 构建与元数据 / Build & Metadata

./gradlew publishToMavenLocal
./gradlew clean build

neoforge.mods.toml src/main/resources/META-INF/neoforge.mods.toml:
    modLoader="javafml": 声明使用 Java 版 mod 加载器。
    [[mods]]: 声明 modid=materialworld，版本、名称、描述等元数据。
    [[dependencies.materialworld]] neoforge: 必需依赖，版本范围 [21.1.256,)，BOTH。
    [[dependencies.materialworld]] minecraft: 必需依赖，版本范围 [1.21.1,1.22)，BOTH。
    [[dependencies.materialworld]] jei: 可选依赖，版本范围 [19.0.0,)，AFTER，CLIENT。

build.gradle src/build.gradle:
    仓库配置: 添加 Jared's maven (https://maven.blamejared.com/) 和 ModMaven 镜像，用于解析 JEI 依赖。
    依赖配置: compileOnly 添加 mezz.jei:jei-1.21.1-neoforge-api:19.51.0.418，localRuntime 添加 mezz.jei:jei-1.21.1-neoforge:19.51.0.418。

gradle.properties gradle.properties:
    mc_version=1.21.1: 目标 Minecraft 版本。
    jei_version=19.51.0.418: 目标 JEI 版本，与 Minecraft 1.21.1 和 NeoForge 21.1.256 兼容。

变更: 修正 JEI 依赖配置，从错误的 Forge 版本改为正确的 NeoForge 版本，并添加必需的 Maven 仓库。

## 配置 / Config

ClientConfig src/main/java/com/varlike/materialworld/ClientConfig.java:
    SPEC: CLIENT 类型配置规格。
    transmutationDisplayMode: 客户端缓存的显示模式枚举值。
    onLoad(ModConfigEvent event): 配置加载或重载时同步显示模式到静态字段。
    TransmutationDisplayMode.ALL: 显示所有有 EMC 的物品。
    TransmutationDisplayMode.LEARNED_ONLY: 只显示已学习的物品（默认）。

MaterialWorldClient src/main/java/com/varlike/materialworld/MaterialWorldClient.java:
    MaterialWorldClient(ModContainer container): 客户端构造函数。
        变更: 新增 container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC)，注册客户端配置。

TransmutationScreen src/main/java/com/varlike/materialworld/client/TransmutationScreen.java:
    init(): 初始化界面。
        变更: 物品收集时根据 ClientConfig.transmutationDisplayMode 过滤：
              LEARNED_ONLY 模式只保留已学习的物品；ALL 模式保留全部。

## 本地化 / Localization

en_us.json src/main/resources/assets/materialworld/lang/en_us.json:
    英文语言文件，包含按键、tooltip、GUI、消息、配置界面的全部翻译键。

zh_cn.json src/main/resources/assets/materialworld/lang/zh_cn.json:
    中文语言文件，与 en_us.json 键名一一对应。

变更: mod 中所有硬编码提示改为 Component.translatable(key, args...)，实现根据客户端语言自动切换。
    涉及文件: ServerboundTransmutePacket, ServerboundRecyclePacket, ServerboundEMCCraftPacket,
             MaterialWorld.ForgeEvents, TransmutationScreen, EMCRecipeTransferHandler

## 附属创造标签设计 / Addon Creative Tab Design

行为:
    无附属 mod 添加物品 -> 标签不注册 -> 界面上看不到。
    有附属 mod 添加物品 -> 标签注册 -> 显示所有附属物品。
关键实现:
    1. 附属 mod 在构造函数中调用 MaterialWorldAPI.addToAddonTab(...)。
    2. 主 mod 在 RegisterEvent 中判断 AddonCreativeTab.isEmpty()。
    3. 非空时注册 CreativeModeTab，displayItems 动态读取物品列表。
时间顺序要求:
    附属 mod 构造函数 早于 主 mod 的 RegisterEvent，因此必须在构造函数中调用 API。

## 公开 API / Public API

MaterialWorldAPI src/main/java/com/varlike/materialworld/api/MaterialWorldAPI.java:
    MOD_ID: 模组 ID 常量。
    API_VERSION: API 版本号，供附属 mod 做兼容性判断。
    getEMC(Item item): 获取物品 EMC 值。
    setEMC(Item item, long value): 设置物品 EMC 值。
    hasEMC(Item item): 判断物品是否已定义 EMC。
    getEMC(ResourceLocation itemId): 通过注册名获取 EMC。
    getPlayerEMC(Player player): 获取玩家 EMC 数据接口，未初始化时返回 null。
    addToAddonTab(Supplier<ItemStack> stackSupplier): 新增，向物质世界的附属创造标签添加物品。必须在附属 mod 的构造函数中调用（RegisterEvent 之前）。
    addToAddonTab(String groupName, Supplier<ItemStack> stackSupplier): 新增，向附属创造标签添加带分组的物品。
        分组名含 "." 时视为翻译键，否则为字面文本。 ->"materialworld.addon_group.spirit_world",

IPlayerEMC src/main/java/com/varlike/materialworld/api/IPlayerEMC.java:
    getBalance(): 获取 EMC 余额。
    setBalance(long value): 设置 EMC 余额。
    addBalance(long amount): 增加 EMC 余额。
    consumeBalance(long amount): 尝试扣除 EMC。
    isLearned(Item item): 判断物品是否已学习。
    learn(Item item): 学习物品，返回 true 表示首次学习。
    getLearnedItems(): 获取不可修改的已学习物品集合。

EMCRegistry src/main/java/com/varlike/materialworld/api/EMCRegistry.java:
    getEMC(Item item): 查询物品 EMC 值。
    setEMC(Item item, long value): 设置物品 EMC 值。
    getEMC(ResourceLocation itemId): 通过注册名查询。
    setEMC(ResourceLocation itemId, long value): 通过注册名设置。
    hasEMC(Item item): 判断是否已定义。
    size(): 返回已定义 EMC 的物品数量。

EMCChangeEvent src/main/java/com/varlike/materialworld/api/event/EMCChangeEvent.java:
    getPlayer(): 返回触发事件的玩家。
    getOldBalance(): 返回变化前的余额。
    getNewBalance(): 返回变化后的余额。
    getDelta(): 返回余额变化量。

ItemLearnedEvent src/main/java/com/varlike/materialworld/api/event/ItemLearnedEvent.java:
    getPlayer(): 返回触发事件的玩家。
    getItem(): 返回被学习的物品。

变更: 新增 api 包，作为附属 mod 的稳定访问契约；内部实现类不被附属 mod 直接引用。
    事件在 ServerboundRecyclePacket 和 ServerboundTransmutePacket 中发布到 NeoForge.EVENT_BUS。

## 类与函数记录 / Class & Function Log

| 类名 / Class | 位置 / Location | 函数名 / Function | 函数作用 / Purpose |

MaterialWorld src/main/java/com/varlike/materialworld/MaterialWorld.java:
    MaterialWorld(IEventBus modEventBus, ModContainer modContainer): 主构造函数，注册物品与创造模式标签注册器、配置和网络通道。
    commonSetup(FMLCommonSetupEvent event): 模组初始化，注册默认 EMC 值，初始化存储并加载。
    onServerStarting(ServerStartingEvent event): 服务器启动时输出日志。
    ForgeEvents.onServerStarted(ServerStartedEvent event): 服务器启动完成后计算 EMC 配方并保存。
    ForgeEvents.onRegisterCommands(RegisterCommandsEvent event): 注册 /emc 命令及其子命令。
    ForgeEvents.onItemTooltip(ItemTooltipEvent event): 为有 EMC 值的物品添加工具提示。

Config src/main/java/com/varlike/materialworld/Config.java:
    validateItemName(Object obj): 验证物品名称是否存在于物品注册表中。
    onLoad(ModConfigEvent event): 配置加载时读取配置值到静态字段。

MaterialWorldClient src/main/java/com/varlike/materialworld/MaterialWorldClient.java:
    MaterialWorldClient(ModContainer container): 客户端模组构造函数，注册配置界面扩展点，使模组列表中的 Config 按钮可用。
    onClientSetup(FMLClientSetupEvent event): 客户端初始化时输出日志和当前玩家名。

EMCManager src/main/java/com/varlike/materialworld/emc/EMCManager.java:
    getEMC(Item item): 获取物品的 EMC 值，未定义时返回 0。
    setEMC(Item item, long value): 设置或更新物品的 EMC 值，负值归零。
    setEMC(String itemRegistryName, long value): 通过注册名设置 EMC，使用 BuiltInRegistries.ITEM 查找物品。
    hasEMC(Item item): 判断物品是否已定义 EMC。
    size(): 返回已注册 EMC 的物品数量。
    clear(): 清空所有 EMC 值，用于重载。
    forEach(BiConsumer<Item, Long> action): 遍历所有已注册的 EMC 值。
    remove(Item item): 移除某个物品的 EMC 定义。

EMCStorage src/main/java/com/varlike/materialworld/emc/EMCStorage.java:
    init(Path configDir): 初始化存储目录和文件路径 config/materialworld/emc.json。
    load(): 从 JSON 文件加载自定义 EMC 值，覆盖默认值。
    save(): 将当前 EMCManager 中的所有值写回 JSON 文件。

EMCValues src/main/java/com/varlike/materialworld/emc/EMCValues.java:
    define(Item item, long value): 向默认值快照中添加一条物品 EMC 定义。
    registerDefaults(): 将所有默认值写入 EMCManager。
    getDefault(Item item): 获取物品的默认 EMC 值。
    hasDefault(Item item): 判断物品是否有默认 EMC 值。

EMCRecipeCalculator src/main/java/com/varlike/materialworld/emc/EMCRecipeCalculator.java:
    calculate(MinecraftServer server): 从服务器所有配方中多轮迭代推导 EMC 值，返回新增数量。
    tryCalculate(Recipe<?> recipe, RegistryAccess registryAccess): 尝试为单个配方推导结果物品的 EMC，所有材料需已有 EMC。

NetworkHandler src/main/java/com/varlike/materialworld/network/NetworkHandler.java:
    register(RegisterPayloadHandlersEvent event): 监听网络注册事件，向 PayloadRegistrar 注册全部 5 个网络包的方向、编解码器和处理器。
    syncTo(ServerPlayer player): 通过 CapabilityHandler.PLAYER_EMC 获取玩家 EMC 数据并向该玩家发送同步包，数据为空时直接返回。

ClientboundSyncEMCPacket src/main/java/com/varlike/materialworld/network/ClientboundSyncEMCPacket.java:
    encode(RegistryFriendlyByteBuf buf): 将余额和已学习物品集合写入缓冲区。
    ClientboundSyncEMCPacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造包实例。
    readLearned(RegistryFriendlyByteBuf buf): 静态工具方法，从缓冲区读取 ResourceLocation 集合。
    handle(ClientboundSyncEMCPacket pkt, IPayloadContext context): 在主线程调用 ClientEMCData.update 更新客户端数据。
    type(): 返回该载荷的唯一类型标识。

ServerboundEMCCraftPacket src/main/java/com/varlike/materialworld/network/ServerboundEMCCraftPacket.java:
    encode(RegistryFriendlyByteBuf buf): 将槽位索引与对应 ItemStack 写入缓冲区。
    ServerboundEMCCraftPacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造包实例。
    readMap(RegistryFriendlyByteBuf buf): 静态工具方法，从缓冲区读取槽位到物品的映射。
    handle(ServerboundEMCCraftPacket pkt, IPayloadContext context): 服务端处理 EMC 合成请求，校验并扣除 EMC，向合成网格放置物品，最后同步最新数据给客户端。
    type(): 返回该载荷的唯一类型标识。

ServerboundRecyclePacket src/main/java/com/varlike/materialworld/network/ServerboundRecyclePacket.java:
    encode(RegistryFriendlyByteBuf buf): 将背包槽位索引写入缓冲区。
    ServerboundRecyclePacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造包实例。
    handle(ServerboundRecyclePacket pkt, IPayloadContext context): 服务端处理回收请求，学习物品并增加 EMC，清空原槽位，最后同步最新数据给客户端,统一事件发布顺序为先记录 oldBalance 再修改余额,首次学习时发布 ItemLearnedEvent(player, item)。
    type(): 返回该载荷的唯一类型标识。

ServerboundRequestSyncPacket src/main/java/com/varlike/materialworld/network/ServerboundRequestSyncPacket.java:
    encode(RegistryFriendlyByteBuf buf): 空实现，该包不携带数据。
    ServerboundRequestSyncPacket(RegistryFriendlyByteBuf buf): 从缓冲区解码构造包实例。
    handle(ServerboundRequestSyncPacket pkt, IPayloadContext context): 服务端收到请求后向该玩家发送一次 EMC 同步包。
    type(): 返回该载荷的唯一类型标识。

ServerboundTransmutePacket src/main/java/com/varlike/materialworld/network/ServerboundTransmutePacket.java:
    encode(RegistryFriendlyByteBuf buf): 将目标物品的注册名写入缓冲区。
    ServerboundTransmutePacket(RegistryFriendlyByteBuf buf): 从缓冲区解码物品并构造包实例。
    handle(ServerboundTransmutePacket pkt, IPayloadContext context): 服务端处理转换请求，校验已学习与 EMC 余额，扣除后发放物品修正事件发布顺序为「先记录 oldBalance → 再 consumeEmc → 再发布 EMCChangeEvent。
    type(): 返回该载荷的唯一类型标识。

ClientPayloadHandler src/main/java/com/varlike/materialworld/network/ClientPayloadHandler.java:
    handleSyncEMC(ClientboundSyncEMCPacket pkt, IPayloadContext context): 客户端专用处理函数，接收 EMC 同步包并更新 ClientEMCData，避免服务端类直接引用客户端代码。

ModCapabilities src/main/java/com/varlike/materialworld/capability/ModCapabilities.java:
    PLAYER_EMC: EntityCapability<PlayerEMC, Void> 静态实例，用于查询玩家的 EMC 能力。
    PROVIDER: 静态 PlayerEMCProvider 实例，供能力注册和事件处理器引用。
    register(RegisterCapabilitiesEvent event): 使用 event.registerEntity 注册玩家实体的 EMC 能力，参数为 PLAYER_EMC、EntityType.PLAYER 和 PROVIDER。

变更: 修正方法名从 register 为 registerEntity，这是 NeoForge 21.1 中实体能力注册的正确 API。

PlayerEMC src/main/java/com/varlike/materialworld/capability/PlayerEMC.java:
    getEmcBalance(): 返回当前 EMC 余额。
    setEmcBalance(long value): 设置 EMC 余额，负值归零。
    addEmc(long amount): 增加 EMC 余额。
    consumeEmc(long amount): 尝试扣除 EMC，余额不足返回 false。
    isLearned(Item item): 判断物品是否已被学习。
    learn(Item item): 学习物品，返回 true 表示首次学习。
    getLearnedItems(): 返回不可修改的已学习物品集合。
    copyFrom(PlayerEMC other): 从另一个 PlayerEMC 复制余额和已学习集合。
    save(): 将数据序列化为 CompoundTag。
    load(CompoundTag tag): 从 CompoundTag 反序列化数据。

PlayerEMCProvider src/main/java/com/varlike/materialworld/capability/PlayerEMCProvider.java:
    getCapability(Entity entity, Void context): 返回该实体的 PlayerEMC 实例，Void 表示不区分方向。
    serializeNBT(): 将持有的 PlayerEMC 数据序列化为 CompoundTag。
    deserializeNBT(CompoundTag nbt): 从 CompoundTag 反序列化数据到持有的 PlayerEMC。

CapabilityHandler src/main/java/com/varlike/materialworld/capability/CapabilityHandler.java:
    PLAYER_EMC: 静态字段，NeoForge EntityCapability 实例，用于查询玩家的 PlayerEMC。
    onRegisterCapabilities(RegisterCapabilitiesEvent event): 向 NeoForge 注册玩家实体能力，为每个玩家创建并缓存 PlayerEMCProvider。

CapabilityGameHandler src/main/java/com/varlike/materialworld/capability/CapabilityGameHandler.java:
    onPlayerClone(PlayerEvent.Clone event): 玩家死亡重生时，将旧 PlayerEMC 数据复制到新 PlayerEMC。
    onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event): 玩家登录时向该玩家发送 EMC 同步包。

ClientEMCData src/main/java/com/varlike/materialworld/client/ClientEMCData.java:
    getBalance(): 返回客户端缓存的 EMC 余额。
    isLearned(Item item): 判断物品是否已被学习，使用 BuiltInRegistries.ITEM 获取注册名。
    update(long newBalance, Collection<ResourceLocation> newLearned): 用服务端同步包中的数据更新客户端缓存。

KeyBindings src/main/java/com/varlike/materialworld/client/KeyBindings.java:
    OPEN_TRANSMUTATION: 静态字段，注册默认按键 P，用于打开物质转换界面。

ClientEvents src/main/java/com/varlike/materialworld/client/ClientEvents.java:
    ModBus.onRegisterKeys(RegisterKeyMappingsEvent event): 在 mod 总线注册按键映射。
    GameBus.onClientTick(ClientTickEvent.Post event): 在每个客户端 tick 结束时检测按键并打开物质转换界面。

TransmutationScreen src/main/java/com/varlike/materialworld/client/TransmutationScreen.java:
    TransmutationScreen(): 构造函数，设置界面标题。
    init(): 初始化界面，请求服务端同步 EMC 数据并构建物品网格列表。
    render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick): 渲染 EMC 网格、玩家背包、悬停提示。
    drawInvCell(GuiGraphics graphics, Inventory inv, int invIndex, int cx, int cy, int mouseX, int mouseY): 绘制单个背包格子及其物品。
    renderGridTooltip(GuiGraphics graphics, int idx, int mouseX, int mouseY): 绘制 EMC 网格物品的悬停提示。
    inside(int mx, int my, int x, int y): 判断鼠标是否位于指定格子内。
    getHoveredGridIndex(int mx, int my): 返回鼠标悬停的 EMC 网格物品索引，未命中返回 -1。
    getHoveredInvIndex(int mx, int my): 返回鼠标悬停的背包槽位索引，未命中返回 -1。
    mouseClicked(double mouseX, double mouseY, int button): 处理鼠标点击，左键提取 EMC 网格物品或回收背包物品。
    mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY): 处理滚轮滚动，切换 EMC 网格显示行。
    isPauseScreen(): 返回 false，使界面不暂停游戏。

EMCRecipeTransferHandler src/main/java/com/varlike/materialworld/compat/jei/EMCRecipeTransferHandler.java:
    EMCRecipeTransferHandler(IRecipeTransferHandlerHelper helper): 构造函数，保存 JEI 提供的转移辅助工具。
    getContainerClass(): 返回该处理器适用的容器类 CraftingMenu。
    getMenuType(): 返回该处理器适用的菜单类型 MenuType.CRAFTING。
    getRecipeType(): 返回 RecipeType<RecipeHolder<CraftingRecipe>>，对应 JEI 19.x 中包装后的配方类型。
    transferRecipe(CraftingMenu container, RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer): 从 RecipeHolder 取出 CraftingRecipe，统计背包已有材料，不足部分通过 EMC 合成补齐，发送 ServerboundEMCCraftPacket 到服务端或进行预检查。

MaterialWorldJEIPlugin src/main/java/com/varlike/materialworld/compat/jei/MaterialWorldJEIPlugin.java:
    getPluginUid(): 返回该 JEI 插件的唯一标识 materialworld:jei_plugin。
    registerRecipeTransferHandlers(IRecipeTransferRegistration registration): 向 JEI 注册 EMCRecipeTransferHandler，用于原版工作台配方。

AddonCreativeTab src/main/java/com/varlike/materialworld/registry/AddonCreativeTab.java:
    Entry(String groupName, Supplier<ItemStack> supplier): 内部记录类，携带可空分组名和物品供应器。
    add(Supplier<ItemStack> supplier): 无分组添加物品。
    add(String groupName, Supplier<ItemStack> supplier): 带分组添加物品。
    getAll(): 返回所有条目的不可修改列表。
    isEmpty(): 判断是否为空。
    getIconSupplier(): 返回第一个物品作为标签图标。
    变更: 数据结构从 List<Supplier<ItemStack>> 改为 List<Entry>，支持分组。

CreativeTabRegistration src/main/java/com/varlike/materialworld/registry/CreativeTabRegistration.java:
    onRegister(RegisterEvent event): 条件注册附属创造标签，AddonCreativeTab 为空时跳过。
    createGroupHeader(String groupName): 生成分组标题锚点（金色加粗名称的纸张）。
    变更: displayItems 支持分组，分组切换时插入标题锚点。

