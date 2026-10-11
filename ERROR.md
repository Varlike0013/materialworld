## 排查记录 / Troubleshooting

问题: 游戏启动时 Mod 加载失败。
    错误: class com.varlike.materialworld.network.ClientPayloadHandler has no @SubscribeEvent methods, but register was called anyway.
    原因: ClientPayloadHandler 是一个纯静态方法容器，不应加 @EventBusSubscriber 注解。
          NeoForge 的 AutomaticEventSubscriber 看到注解会尝试注册，但类里没有 @SubscribeEvent 方法，于是抛异常。
    修复: 删除 ClientPayloadHandler 上的 @EventBusSubscriber 注解及相关的 Dist / EventBusSubscriber import。
    规则: 只有包含 @SubscribeEvent 方法的类才需要 @EventBusSubscriber。

## 排查记录 / Troubleshooting

问题: 游戏启动时 Mod 加载失败。
    错误: java.lang.IllegalStateException: Cannot get config value before config is loaded.
    位置: materialworld Config.onLoad(Config.java:57)
    原因: NeoForge 21.1.257 中 ModConfigEvent.Loading 事件在配置值可读之前触发，
          此时 ModConfig.isLoaded() 返回 false，任何 .get() 都会抛异常。
    修复:
        1. 删除 Config 中对 ModConfigEvent.Loading 的监听。
        2. 新增 Config.loadValues() 方法，集中读取所有配置值。
        3. 在 MaterialWorld.commonSetup(FMLCommonSetupEvent) 中调用 Config.loadValues() 完成首次加载。
        4. 新增对 ModConfigEvent.Reloading 的监听，处理游戏内重载。
    规则: 
        - 首次配置加载 → FMLCommonSetupEvent
        - 游戏内重载 → ModConfigEvent.Reloading
        - 不要使用 ModConfigEvent.Loading 读取配置值

## 排查记录 / Troubleshooting

问题: 从物质世界传导过来的配置加载问题。
    错误: Cannot get config value before config is loaded.
    修复:
        1. Config 和 ClientConfig 均改用 ModConfigEvent.Reloading + 手动 loadValues()。
        2. SpiritWorld.commonSetup 中调用 Config.loadValues()。
        3. SpiritWorldClient.onClientSetup 中调用 ClientConfig.loadValues()。

Config src/main/java/com/varlike/spiritworld/Config.java:
    变更: 删除 ModConfigEvent.Loading 处理器，新增 loadValues() 方法和 ModConfigEvent.Reloading 处理器。

ClientConfig src/main/java/com/varlike/spiritworld/ClientConfig.java:
    变更: 同上。

SpiritWorld src/main/java/com/varlike/spiritworld/SpiritWorld.java:
    变更: 新增 commonSetup(FMLCommonSetupEvent) 方法，调用 Config.loadValues()。

SpiritWorldClient src/main/java/com/varlike/spiritworld/SpiritWorldClient.java:
    变更: onClientSetup 中调用 ClientConfig.loadValues()。