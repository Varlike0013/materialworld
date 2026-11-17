# 代码类与函数索引（CLASS_INDEX）

> 用途：开发时快速定位类、字段、方法、注册入口与数据契约。
> 代码根：`src/main/java`，包名 `com.varlike.oathbreaker`。
> 核对时间：2026-11-17；对应 `mod_version=1.0.0`、NeoForge `21.1.256`、Minecraft `1.21.1`。
> 当前阶段：P0/P1/P2 完成；本轮新增数值放大、多段伤害、命中效果接口、武器池。
> 维护规则：新增/重命名/删除类或方法后同步本文件，见文末「维护清单」。

## 1. 当前代码概况

| 项 | 数量 | 说明 |
|---|---|---|
| Java 文件 | 37 | 37 个顶层类，另含 `ModTags.Items` / `EntityTypes` / `DamageTypes` 三个嵌套类 |
| 公开方法/构造 | 约 80 | 不含 record 自动 accessor 与私有 HUD 辅助方法 |
| 自定义属性 | 15 | AD/生命等复用原版；AP、魔抗、暴击、穿透、法力、吸血等为本 Mod |
| DataMap | 2 | `weapon_profile`、`mob_profile` |
| Tag | 11 | 4 物品 + 4 实体 + 3 伤害类型 |
| 网络 Payload | 1 | `PlayerStatsPayload`（S→C） |
| HUD | 1 | 左上角 LoL 风格面板 |
| 武器池 | 1 套 | JSON + 代码双来源；JSON 3 条示例 |
| 命中效果 | 1 套 | `HitEffect` / `HitContext` / `WeaponHitProvider` |
| 调试命令 | 1 组 | `class` / `profile` / `stats` / `pool` |

包结构：

```text
src/main/java/com/varlike/oathbreaker/
├── Oathbreaker.java / OathbreakerConfig.java
├── client/            OathbreakerClient, OathbreakerHud, ClientStatsCache
├── combat/
│   ├── DamageCategory, DamageComponent, DamageProfile
│   ├── CombatMath, CombatStats, CombatDamageHandler, ClassWeaponHandler, DamagePipeline
│   └── hit/           HitSource, HitContext, HitEffect, WeaponHitProvider
├── command/           OathbreakerCommands
├── core/
│   ├── attribute/     ModAttributes, AttributeModifiers
│   ├── classification/OathbreakerDataMaps, WeaponProfile, MobProfile, ModTags
│   ├── mob/           MobHealthScaling
│   ├── network/       OathbreakerNetwork, PlayerStatsPayload, StatsSyncHandler
│   ├── player/        PlayerBaseStats, PlayerClassData, PlayerClasses
│   └── weapon/        WeaponDefinition, WeaponPool, WeaponPoolReloadListener, WeaponPoolReloadHandler
└── datagen/           ModDataGen, ModLanguageProvider
```

战斗与武器池数据流：

```text
玩家攻击
└─ LivingDamageEvent.Pre（NORMAL）→ CombatDamageHandler
   ├─ WeaponPool.resolveProfile(weapon)：代码 > JSON 池条目 > DataMap
   ├─ DamageProfile 拆成多段（物理/魔法/真实）
   ├─ 物理段判暴击；逐段算穿透/抗性；求和
   └─ setNewDamage
LivingDamageEvent.Pre（LOWEST）→ ClassWeaponHandler（非本职业强制 1）
LivingDamageEvent.Post → 吸血 + WeaponPool.fireHit → HitEffect.onHit
LivingDeathEvent → WeaponPool.fireKill → HitEffect.onKill（可选）
```

## 2. 类总览

| # | 类 | 文件路径 | 角色 | 方法数 |
|---|---|---|---|---|
| 1 | `Oathbreaker` | [Oathbreaker.java](../src/main/java/com/varlike/oathbreaker/Oathbreaker.java) | Mod 主类、注册、ID 工厂 | 2 |
| 2 | `OathbreakerConfig` | [OathbreakerConfig.java](../src/main/java/com/varlike/oathbreaker/OathbreakerConfig.java) | COMMON 配置 | 0（16 字段） |
| 3 | `OathbreakerClient` | [OathbreakerClient.java](../src/main/java/com/varlike/oathbreaker/client/OathbreakerClient.java) | 客户端入口 | 1 |
| 4 | `OathbreakerHud` | [OathbreakerHud.java](../src/main/java/com/varlike/oathbreaker/client/OathbreakerHud.java) | HUD Layer | 9 |
| 5 | `ClientStatsCache` | [ClientStatsCache.java](../src/main/java/com/varlike/oathbreaker/client/ClientStatsCache.java) | 客户端面板缓存 | 4 |
| 6 | `ModAttributes` | [ModAttributes.java](../src/main/java/com/varlike/oathbreaker/core/attribute/ModAttributes.java) | 自定义属性注册 | 0（15 字段） |
| 7 | `AttributeModifiers` | [AttributeModifiers.java](../src/main/java/com/varlike/oathbreaker/core/attribute/AttributeModifiers.java) | 固定 ID 修饰符工具 | 5 |
| 8 | `OathbreakerDataMaps` | [OathbreakerDataMaps.java](../src/main/java/com/varlike/oathbreaker/core/classification/OathbreakerDataMaps.java) | DataMap 注册与查询 | 4 |
| 9 | `WeaponProfile` | [WeaponProfile.java](../src/main/java/com/varlike/oathbreaker/core/classification/WeaponProfile.java) | 武器分类 + 多段伤害 record | 5 |
| 10 | `MobProfile` | [MobProfile.java](../src/main/java/com/varlike/oathbreaker/core/classification/MobProfile.java) | 怪物分类 record | 3 |
| 11 | `ModTags` | [ModTags.java](../src/main/java/com/varlike/oathbreaker/core/classification/ModTags.java) | Tag 常量容器 | 0（11 常量） |
| 12 | `DamageCategory` | [DamageCategory.java](../src/main/java/com/varlike/oathbreaker/combat/DamageCategory.java) | 伤害分类 enum | 2 |
| 13 | `DamageComponent` | [DamageComponent.java](../src/main/java/com/varlike/oathbreaker/combat/DamageComponent.java) | 单段伤害 record | 0 |
| 14 | `DamageProfile` | [DamageProfile.java](../src/main/java/com/varlike/oathbreaker/combat/DamageProfile.java) | 多段伤害构成 | 5 |
| 15 | `CombatMath` | [CombatMath.java](../src/main/java/com/varlike/oathbreaker/combat/CombatMath.java) | 抗性/穿透公式 | 2 |
| 16 | `CombatStats` | [CombatStats.java](../src/main/java/com/varlike/oathbreaker/combat/CombatStats.java) | 服务端面板快照 | 1（+21 accessor） |
| 17 | `CombatDamageHandler` | [CombatDamageHandler.java](../src/main/java/com/varlike/oathbreaker/combat/CombatDamageHandler.java) | 多段伤害、吸血、命中触发 | 8 |
| 18 | `ClassWeaponHandler` | [ClassWeaponHandler.java](../src/main/java/com/varlike/oathbreaker/combat/ClassWeaponHandler.java) | 非本职业武器裁决 | 1 |
| 19 | `DamagePipeline` | [DamagePipeline.java](../src/main/java/com/varlike/oathbreaker/combat/DamagePipeline.java) | 面板值、职业可用性 | 2 |
| 20 | `HitSource` | [HitSource.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitSource.java) | WEAPON/SKILL enum | 0 |
| 21 | `HitContext` | [HitContext.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitContext.java) | 命中上下文 | 0（record） |
| 22 | `HitEffect` | [HitEffect.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitEffect.java) | 命中效果接口 | 2 |
| 23 | `WeaponHitProvider` | [WeaponHitProvider.java](../src/main/java/com/varlike/oathbreaker/combat/hit/WeaponHitProvider.java) | Item 自带效果接口 | 1 |
| 24 | `WeaponDefinition` | [WeaponDefinition.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponDefinition.java) | 武器池条目 + Builder | 11 |
| 25 | `WeaponPool` | [WeaponPool.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPool.java) | 武器池解析/注册/抽取/触发 | 10 |
| 26 | `WeaponPoolReloadListener` | [WeaponPoolReloadListener.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPoolReloadListener.java) | JSON 池加载器 | 2 |
| 27 | `WeaponPoolReloadHandler` | [WeaponPoolReloadHandler.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPoolReloadHandler.java) | 注册 reload listener | 1 |
| 28 | `MobHealthScaling` | [MobHealthScaling.java](../src/main/java/com/varlike/oathbreaker/core/mob/MobHealthScaling.java) | 怪物全局生命倍率 | 1 |
| 29 | `PlayerBaseStats` | [PlayerBaseStats.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerBaseStats.java) | 玩家基础面板/倍率 | 6 |
| 30 | `PlayerClassData` | [PlayerClassData.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerClassData.java) | 玩家职业 record | 8 |
| 31 | `PlayerClasses` | [PlayerClasses.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerClasses.java) | 职业 Attachment | 2 |
| 32 | `OathbreakerNetwork` | [OathbreakerNetwork.java](../src/main/java/com/varlike/oathbreaker/core/network/OathbreakerNetwork.java) | Payload 注册 | 1 |
| 33 | `PlayerStatsPayload` | [PlayerStatsPayload.java](../src/main/java/com/varlike/oathbreaker/core/network/PlayerStatsPayload.java) | 面板同步包 | 2（+21 accessor） |
| 34 | `StatsSyncHandler` | [StatsSyncHandler.java](../src/main/java/com/varlike/oathbreaker/core/network/StatsSyncHandler.java) | 面板推送 | 8 |
| 35 | `OathbreakerCommands` | [OathbreakerCommands.java](../src/main/java/com/varlike/oathbreaker/command/OathbreakerCommands.java) | 调试命令 | 10 |
| 36 | `ModDataGen` | [ModDataGen.java](../src/main/java/com/varlike/oathbreaker/datagen/ModDataGen.java) | datagen 入口 | 1 |
| 37 | `ModLanguageProvider` | [ModLanguageProvider.java](../src/main/java/com/varlike/oathbreaker/datagen/ModLanguageProvider.java) | 语言文件生成 | 4 |

## 3. 逐类详情

### 3.1 主类 / 配置 / 客户端

- [Oathbreaker.java](../src/main/java/com/varlike/oathbreaker/Oathbreaker.java)：`MOD_ID`（L15）、`LOGGER`（L16）、构造（L18–23）、`id`（L26）。
- [OathbreakerConfig.java](../src/main/java/com/varlike/oathbreaker/OathbreakerConfig.java)：16 个字段，新增 `MOB_HEALTH_MULTIPLIER`（L23）、`PLAYER_HEALTH_MULTIPLIER`（L31）、`PLAYER_ATTACK_MULTIPLIER`（L32）；静态块 L37–98。
- [OathbreakerClient.java](../src/main/java/com/varlike/oathbreaker/client/OathbreakerClient.java)：构造 L17–19，注册 HUD。
- [OathbreakerHud.java](../src/main/java/com/varlike/oathbreaker/client/OathbreakerHud.java)：`register` L27、`render` L36、私有辅助 L74–102。
- [ClientStatsCache.java](../src/main/java/com/varlike/oathbreaker/client/ClientStatsCache.java)：`update` L15、`get` L20、`clear` L24。

### 3.2 属性

- [ModAttributes.java](../src/main/java/com/varlike/oathbreaker/core/attribute/ModAttributes.java)：`ATTRIBUTES`（L18）；15 个属性 Holder（L22–103）。
- [AttributeModifiers.java](../src/main/java/com/varlike/oathbreaker/core/attribute/AttributeModifiers.java)：`id` L21、`applyTransient` L26、`applyPermanent` L37、`remove` L47、`has` L54。

### 3.3 分类 / 数据契约

- [OathbreakerDataMaps.java](../src/main/java/com/varlike/oathbreaker/core/classification/OathbreakerDataMaps.java)：`WEAPON_PROFILE` L28、`MOB_PROFILE` L33、`register` L40、`onServerStarted` L47、`weaponProfile` L54、`mobProfile` L62。
- [WeaponProfile.java](../src/main/java/com/varlike/oathbreaker/core/classification/WeaponProfile.java)：record L27–33（`category`、`damage`、`requiredClasses`、`classTags`、`rangedScale`）；`CODEC` L43；`isRanged` L62；`isUnrestricted` L66；`hasMagicComponent` L70；`isUsableBy` L74。
- [MobProfile.java](../src/main/java/com/varlike/oathbreaker/core/classification/MobProfile.java)：record L11、`CODEC` L25、`isBoss` L41、`isElite` L45。
- [ModTags.java](../src/main/java/com/varlike/oathbreaker/core/classification/ModTags.java)：`Items` L19、`EntityTypes` L32、`DamageTypes` L45。

### 3.4 伤害核心

- [DamageCategory.java](../src/main/java/com/varlike/oathbreaker/combat/DamageCategory.java)：`PHYSICAL/MAGIC/TRUE` L15、`CODEC` L20、`fromSource` L35。
- [DamageComponent.java](../src/main/java/com/varlike/oathbreaker/combat/DamageComponent.java)：record L12（type + ratio）、`CODEC` L13。
- [DamageProfile.java](../src/main/java/com/varlike/oathbreaker/combat/DamageProfile.java)：`PHYSICAL` L16、`CODEC` L19、紧凑构造 L22（合并同类/归一化）、`ratioOf` L38、`primaryType` L46、`isPure` L56、`toString` L61。
- [CombatMath.java](../src/main/java/com/varlike/oathbreaker/combat/CombatMath.java)：`mitigate` L15、`effectiveResistance` L25。
- [CombatStats.java](../src/main/java/com/varlike/oathbreaker/combat/CombatStats.java)：record L14、`capture` L37。
- [CombatDamageHandler.java](../src/main/java/com/varlike/oathbreaker/combat/CombatDamageHandler.java)：
  - `onDamagePre` L43（NORMAL）。
  - `applyOutgoing` L52：多段拆分、物理段暴击、逐段穿透/抗性。
  - `applyIncoming` L107：按 Tag 对玩家应用抗性。
  - `onDamagePost` L132：吸血 + `WeaponPool.fireHit`。
  - `onLivingDeath` L168：`WeaponPool.fireKill`（仅物理武器击杀）。
  - `resolveDamageProfile` L187：物理来源读武器 profile，魔法/真实技能按来源 Tag 成段。
  - `debug` L199、`armorOf` L209。
- [ClassWeaponHandler.java](../src/main/java/com/varlike/oathbreaker/combat/ClassWeaponHandler.java)：`onLivingDamagePre` L32（LOWEST），使用 `WeaponPool.resolveProfile`。
- [DamagePipeline.java](../src/main/java/com/varlike/oathbreaker/combat/DamagePipeline.java)：`panelValue` L26（按主伤害类型 AD/AP）、`isUsable` L35。

### 3.5 命中效果接口

- [HitSource.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitSource.java)：enum L6，`WEAPON` / `SKILL`。
- [HitContext.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitContext.java)：record L17，字段 attacker/target/weapon/weaponDefinition/damageProfile/damageDealt/source/hitSource。
- [HitEffect.java](../src/main/java/com/varlike/oathbreaker/combat/hit/HitEffect.java)：`onHit`、默认 `onKill`（L15 起）。
- [WeaponHitProvider.java](../src/main/java/com/varlike/oathbreaker/combat/hit/WeaponHitProvider.java)：`getWeaponHitEffect`（L19 起），自定义 Item 实现即可。

### 3.6 武器池

- [WeaponDefinition.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponDefinition.java)：
  - record L27–36（`id`、`item`、`itemTag`、`weight`、`poolTags`、`profile`、`hitEffect`、`codeRegistered`）。
  - `JSON_CODEC` L38（只读 item/tag/weight/pool_tags）。
  - `withId` L52、`matches` L56、`builder` L66。
  - Builder：`item` L83、`tag` L88、`weight` L93、`poolTag` L98、`profile` L103、`hitEffect` L108、`build` L113。
- [WeaponPool.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPool.java)：
  - `register` L44（代码层，同 ID 覆盖 JSON）、`acceptJson` L50、`rebuild` L58。
  - `all` L66、`size` L70、`resolve` L76、`resolveProfile` L90。
  - `roll` L99（按权重）、`fireHit` L119、`fireKill` L139。
- [WeaponPoolReloadListener.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPoolReloadListener.java)：构造 L25、`apply` L30，路径 `oathbreaker/weapons`。
- [WeaponPoolReloadHandler.java](../src/main/java/com/varlike/oathbreaker/core/weapon/WeaponPoolReloadHandler.java)：`addReloadListener` L16。

### 3.7 玩家 / 数值放大

- [PlayerBaseStats.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerBaseStats.java)：
  - 固定 ID L29–34（生命/攻击倍率、魔抗、法力、回复）。
  - 监听 L39–54（HIGHEST），`apply` L58，`applyTotalMultiplier` L77。
  - 生命/AD 用 `ADD_MULTIPLIED_TOTAL`，默认 5 倍；登录/重生补满生命。
- [MobHealthScaling.java](../src/main/java/com/varlike/oathbreaker/core/mob/MobHealthScaling.java)：`onEntityJoin` L31；对所有 `Enemy` 应用生命倍率并保留血量比例。
- [PlayerClassData.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerClassData.java)：`withClass` L36、`withoutClass` L40、`withTag` L44、`withoutTag` L50、`withTags` L56、`hasClass` L60、`hasAnyTag` L64。
- [PlayerClasses.java](../src/main/java/com/varlike/oathbreaker/core/player/PlayerClasses.java)：`CLASS_DATA` L20、`get` L29、`set` L33。

### 3.8 网络

- [OathbreakerNetwork.java](../src/main/java/com/varlike/oathbreaker/core/network/OathbreakerNetwork.java)：`register` L26、`handlePlayerStats` L32。
- [PlayerStatsPayload.java](../src/main/java/com/varlike/oathbreaker/core/network/PlayerStatsPayload.java)：record L15、`TYPE` L38、`STREAM_CODEC` L41、`from` L90、`type` L106。
- [StatsSyncHandler.java](../src/main/java/com/varlike/oathbreaker/core/network/StatsSyncHandler.java)：登录 L30、重生 L37、跨维度 L44、装备 L51、登出 L58、tick 差分 L63、`send` L72。

### 3.9 命令 / datagen

- [OathbreakerCommands.java](../src/main/java/com/varlike/oathbreaker/command/OathbreakerCommands.java)：注册入口 L47；`setClass` L79、`clearClass` L87、`getClass` L94、`addTag` L103、`removeTag` L111、`clearTags` L119、`profile` L126、`pool` L168、`stats` L179。
- [ModDataGen.java](../src/main/java/com/varlike/oathbreaker/datagen/ModDataGen.java)：`gatherData` L12。
- [ModLanguageProvider.java](../src/main/java/com/varlike/oathbreaker/datagen/ModLanguageProvider.java)：16 条翻译（15 属性 + 1 死亡消息）。

## 4. 数据与资源契约

### 4.1 DataMap

| 文件 | 类型 | 当前条目数 | 说明 |
|---|---|---|---|
| [weapon_profile.json](../src/main/resources/data/oathbreaker/data_maps/item/weapon_profile.json) | `oathbreaker:weapon_profile` | 6 | bow/crossbow/iron_sword/heavy tag；trident 为 70% 物理 + 30% 魔法 |
| [mob_profile.json](../src/main/resources/data/oathbreaker/data_maps/entity_type/mob_profile.json) | `oathbreaker:mob_profile` | 7 | `#common` 3 + `#elite` 2 + `#boss` 2 |

### 4.2 Tag

| 文件 | 类型 | 内容 |
|---|---|---|
| `tags/item/weapon_melee.json` | Item | iron_sword |
| `tags/item/weapon_heavy.json` | Item | iron_axe、diamond_axe |
| `tags/item/weapon_ranged.json` | Item | bow、crossbow |
| `tags/item/weapon_magic.json` | Item | 空 |
| `tags/entity_type/common.json` | EntityType | zombie、skeleton、spider |
| `tags/entity_type/elite.json` | EntityType | ravager、witch |
| `tags/entity_type/boss.json` | EntityType | ender_dragon、wither |
| `tags/entity_type/undead.json` | EntityType | zombie、skeleton、zombie_villager |
| `tags/damage_type/physical.json` | DamageType | player_attack、mob_attack、arrow、trident 等 |
| `tags/damage_type/magic.json` | DamageType | magic、indirect_magic、fireball、wither 等 |
| `tags/damage_type/true.json` | DamageType | out_of_world、generic_kill |

### 4.3 武器池 JSON

| 文件 | 匹配 | 权重 | 池标签 |
|---|---|---|---|
| [bow.json](../src/main/resources/data/oathbreaker/oathbreaker/weapons/bow.json) | `minecraft:bow` | 10 | ranged、starter |
| [iron_sword.json](../src/main/resources/data/oathbreaker/oathbreaker/weapons/iron_sword.json) | `minecraft:iron_sword` | 10 | melee、starter |
| [heavy_weapons.json](../src/main/resources/data/oathbreaker/oathbreaker/weapons/heavy_weapons.json) | `#oathbreaker:weapon_heavy` | 8 | heavy_melee |

JSON 不写 profile/命中效果，因此不修改武器行为；代码注册同 ID 可覆盖。

### 4.4 配置

`run/config/oathbreaker-common.toml` 新增：

- `mob_scaling.healthMultiplier`：默认 5.0。
- `base_stats.healthMultiplier`：默认 5.0。
- `base_stats.attackMultiplier`：默认 5.0。

## 5. 命令与验收

```text
/oathbreaker pool
/oathbreaker profile
/oathbreaker stats
/oathbreaker class set oathbreaker:ranger
/attribute @s oathbreaker:generic.crit_chance base set 1.0
```

P3 验收路径：

1. 进入世界：玩家最大生命约 100、AD 约 25（裸手/武器不同），怪物生命约 5 倍；HUD 显示放大后的数值。
2. 分别用普通剑、三叉戟（70/30 混合）测试；混合武器的实际伤害应按物理/魔法分别减伤。
3. 设置 100% 暴击，确认只有物理段享受 175% 暴击。
4. `/oathbreaker profile` 显示 `damage=70% physical + 30% magic`、池 ID 与 effect。
5. `/oathbreaker pool` 显示 3 条 JSON 池条目。
6. 代码注册同 ID 定义 + HitEffect，确认命中时触发；JSON 条目不会触发额外效果。
7. 用自定义 Item 实现 `WeaponHitProvider`，确认无需注册即可触发。

## 6. 实现缺口

| 设计能力 | 章节 | 当前实现 | 缺口 |
|---|---|---|---|
| 多段伤害 | 4.3 | `DamageProfile` + `CombatDamageHandler` | 技能系统接入、伤害增幅、持续伤害 |
| 命中效果 | 4.9 | `HitEffect`/`HitContext`/`WeaponHitProvider` | 技能命中触发、命中特效/音效、冷却与触发次数限制 |
| 武器池 | 4.9、6.12 | JSON + 代码双来源、权重抽取 | 与掉落/商店/大厅 UI 对接；池刷新事件通知 |
| 数值放大 | 4.1、4.6 | 玩家生命/AD、怪物生命倍率 | 怪物攻击/护甲缩放、Boss 单独曲线 |
| HUD/网络 | 5.4 | PlayerStatsPayload + HUD | 客户端运行验证受环境 OpenGL 限制 |
| 技能/关卡/账户 | 2.x、3.x | 无 | 后续阶段 |

已知边界：

- HUD 未能在此环境运行验证（无 OpenGL），仅通过编译与 API 检查。
- 怪物生命倍率对已存在且已带固定 ID 的怪物不重复应用；改配置后需新生成/重新加载。
- `WeaponPool` 同一物品同时匹配多个条目时取解析列表中的第一条（代码优先）。
- `DamageProfile` 比例归一化，不能靠“比例总和 >1”做额外倍率。
- 原版跳跃暴击仍可能与自定义暴击叠加。
- 魔法/真实技能按来源 Tag 成段；物理技能目前会沿用主手 profile，技能系统接入时应使用独立伤害源或 damage profile registry。

## 7. 验证记录（2026-11-17）

| 验证 | 命令/方式 | 结果 |
|---|---|---|
| 编译 | `gradlew compileJava` | BUILD SUCCESSFUL |
| 专用服务器 | `gradlew runServer` | 启动到 `Done`；`Loaded 3 weapon pool entries from JSON` |
| DataMap | 服务器日志 | `weapon_profile entries=6, mob_profile entries=7` |
| 武器池 | 服务器日志 | JSON 3 条；代码层为空（无示例注册） |
| 运行时 RCON | `oathbreaker pool` / `summon` / `attribute get` | 池 3 件；僵尸最大生命 `100.0`，确认怪物 ×5 生效 |
| 客户端 | `gradlew runClient` | 环境无 OpenGL，HUD 未运行验证 |
| 玩家倍率 | 逻辑检查 | 固定 ID + `ADD_MULTIPLIED_TOTAL`；无玩家连接实测 |

## 8. 维护清单

1. **类总览（2）**：新增/删除/重命名类时更新路径、角色、方法数。
2. **逐类详情（3）**：新类补一节；字段、注册 ID、行号随代码更新。
3. **数据契约（4）**：DataMap / Tag / 武器池 JSON / 配置变化时同步。
4. **缺口表（6）**：实现某能力后更新“当前实现”，缩小“缺口”。
5. **验证记录（7）**：每次里程碑更新命令与结果。
6. **变更记录**：设计/契约变更同时在 [09-变更记录](09-变更记录.md) 留一行。
7. **行号维护**：大规模重构后重查：

```bash
grep -rn "^public class\|^public final class\|^public record\|^public enum" src/main/java
grep -nE "^\s+(public|protected|private).*\(" src/main/java/com/varlike/oathbreaker/Oathbreaker.java
```
