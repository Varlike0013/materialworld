# 弃誓者 / Oathbreaker 项目主文档

> 建议路径：`docs/PROJECT_MASTER.md`  
> 目标平台：NeoForge 1.21.1  
> Mod 名称：弃誓者 / Oathbreaker  
> modid：`oathbreaker`  
> 主包名：`com.varlike.oathbreaker`  
> 数据包路径：`data/oathbreaker/...`  
> 文档版本：草案 v0.4（整合叙事框架、誓言之地、轮回链、魔神契约）  
> 文档状态：方向整合稿，待最终确认  
> 定位：不自研武器/魔法/怪物内容；自研分类、成长、结算、UI、网络、多人数据、关卡实例与叙事/契约规则层。

---

## 目录

0. 大类划分说明  
1. 项目概览  
2. 玩法设计  
3. 世界与规则设定  
4. 数值与战斗设定  
5. 技术架构  
6. 数据格式与接口  
7. 开发规范  
8. 错误记录  
9. 变更记录  
10. 待确认决策与风险  
11. 里程碑与验收  
12. 附录  

---

## 0. 大类划分说明

| 大类 | 内容 | 划分理由 |
|---|---|---|
| 1. 项目概览 | 目标、范围、非目标、核心原则、命名 | 先锁定边界与身份 |
| 2. 玩法设计 | 核心循环、大厅准备、关卡流程、技能抽取、局外成长、多人规则、叙事推进、魔神契约、职业武器适配 | 描述玩家实际体验 |
| 3. 世界与规则设定 | 誓言之地、关卡实例、房间模板、怪物锚点、分类体系、怪物阶段、数据归属、轮回链、魔神关系 | 描述“世界如何运转”的稳定规则 |
| 4. 数值与战斗设定 | 属性来源、伤害管线、UI 面板值与实际伤害、武器属性覆盖、怪物缩放、职业武器适配、死魂灵与契约加成 | 保证显示与实际一致 |
| 5. 技术架构 | Mod 清单、自研模块、网络包、HUD、持久化、构建期扫描、叙事/契约 UI | 描述实现骨架 |
| 6. 数据格式与接口 | Tag、DataMap、技能定义、房间元数据、怪物锚点、魔神关系、契约层级、记忆残片 | 给开发直接使用的数据契约 |
| 7. 开发规范 | 版本锁定、兼容测试、服务端权威、/reload、叙事唯一性、契约切换约束 | 降低多人/跨 Mod 开发混乱 |
| 8. 错误记录 | 缺陷追踪 | 只记录 Bug 与异常 |
| 9. 变更记录 | 设计/实现变更追踪 | 记录“为什么改、影响了什么” |
| 10. 待确认决策与风险 | 未拍板事项、兼容性风险 | 与错误记录分离 |
| 11. 里程碑与验收 | 分阶段目标与验收标准 | 可验证推进 |
| 12. 附录 | 术语、参考 | 辅助阅读 |

---

## 1. 项目概览

### 1.1 要实现的体验

- 多人联机 PvE Roguelike：誓言之地准备 → 生成 Level N 实例 → 起点出发 → 击败终点 Boss → 交互进入下一关。
- 玩家身份为“弃誓者”：背弃“永不归来”誓言、从誓言之地登陆大陆的死刑犯后裔/回归者。
- 叙事通过五层关卡 + 五枚记忆残片碎片化呈现：每层 Boss 固定掉落对应残片，右键阅读，拼出“王室截流灵魂、死根→树心轮回链被破坏”的真相。
- 玩家拥有职业；升级时从职业池与激活魔神池中抽取主动/被动技能，类似吸血鬼幸存者的成长循环。
- 魔神契约是局外成长核心：十三魔神（首版实装 4 位），关系矩阵（友善/中立/敌对），死魂灵献祭提升契约等级，每层契约从 2–3 个天赋中选 1 激活、可切换。
- 武器、怪物、Boss、房间素材来自其他 Mod；本 Mod 负责分类、数值覆盖、阶段缩放、技能加成、叙事映射。
- 武器和技能通过 Tag + DataMap 分类；武器适配由玩家职业决定，非本职业武器造成 1 点伤害；魔神不影响武器适配。
- 不使用原版血条和饱食度条，自研 HUD 显示生命、护甲、近战/远程伤害、武器附加信息等。
- UI 显示攻击方面板值，不计算目标护甲/魔抗；实际伤害在伤害管线中计算防御修正。
- 局外成长（解锁职业、技能池、永久点数、契约等级、天赋选择、记忆残片）跨对局保存；局内状态（当局属性分配、技能装配、Mana/冷却、激活魔神）随对局结束丢弃或结算。
- 3D 区域可保存为“房间”模板；房间中的怪物通过“怪物锚点方块”存储完整实体 NBT，构建期主动扫描物化。

### 1.2 明确不做

- 不实现具体武器模型、动画、合成表。
- 不实现具体魔法特效内容；只实现技能定义、抽取、装配、触发与结算框架。
- 不重写原版生存机制。移除原版血条/饱食度仅移除渲染层；食物消耗、回血等原版逻辑仍由服务端运行。
- 不做长段对话、NPC 对话树、过场动画；叙事通过记忆残片物品 + 只读文本界面呈现。
- 不依赖结构模板中直接放置实体；怪物占位统一走怪物锚点方块 + 构建期扫描。
- 不强制第三方 Mod 提供兼容接口；通过 Tag/DataMap/Attribute 非侵入接入。

### 1.3 核心原则

- **内容外包，规则自研**：第三方提供武器、怪物、结构；本 Mod 提供分类、阶段、技能、UI、实例、叙事与契约。
- **服务端权威**：伤害、冷却、Mana、技能抽取、阶段推进、账户数据、死魂灵、契约等级、天赋选择全部以服务端为准。
- **数据驱动优先**：分类、技能池、成长曲线、房间元数据、魔神关系、契约层级尽量走 JSON / DataMap / Tag，`/reload` 可生效。
- **显示与实际共享前半程**：UI 面板值和实际伤害都从同一组 `AttributeInstance.getValue()` 出发；差异只发生在防御方修正阶段。
- **生成时快照**：怪物数值在生成时按当前阶段、实例人数、关卡序号施加，已生成怪物不因阶段推进而突变。
- **局外与局内分离**：账户层只存解锁、永久点数、契约、残片；运行层存当局状态，实例结束即销毁。
- **不依赖方块实体 tick**：结构放置后的方块实体不会自动激活 tick；怪物锚点由构建流程主动扫描消费。
- **叙事碎片化、低成本**：五枚残片、五段文本，复用只读 UI 框架，不引入对话系统。
- **契约激活排他**：每局只能激活一位魔神作为 activePatron；敌对魔神技能永不进入本局候选池。

### 1.4 命名与命名空间

| 项 | 值 |
|---|---|
| Mod 名称 | 弃誓者 / Oathbreaker |
| modid | `oathbreaker` |
| 主包名 | `com.varlike.oathbreaker` |
| 数据包根 | `data/oathbreaker/...` |
| 大厅维度 | `oathbreaker:oathground`（誓言之地） |
| 准备方块 | `oathbreaker:oath_stone`（誓约石台） |
| 记忆残片 | `oathbreaker:lore_fragment`，带 `fragment_index` DataComponent（1–5） |
| 死魂灵 | 账户级计数器 `unspentSouls`，不掉落为物品 |
| 魔神关系数据 | `data/oathbreaker/oathbreaker/patron_relations.json` |
| 魔神定义 | `data/oathbreaker/oathbreaker/patrons/*.json` |

> 下文示例路径统一使用 `oathbreaker` 命名空间；早期草稿中的 `mymod` 均视为占位。

---

## 2. 玩法设计

### 2.1 核心循环

```text
进入誓言之地（大厅）
→ 查看局外解锁 / 选择职业 / 献祭死魂灵 / 选择本局激活魔神
→ 在誓约石台“立誓”（Ready）
→ 全员立誓
→ 服务端生成 Level N 实例（起点 + 战斗房间 + Boss 房）
→ 结构放置完成，扫描怪物锚点并物化怪物
→ 传送到起点
→ 战斗/升级/抽技能
→ 击败 Boss，获得记忆残片（若未拥有）
→ 交互“下一关”或倒计时
→ 销毁旧实例，生成 Level N+1
→ 循环；失败/退出回到誓言之地
2.2 大厅准备：誓言之地
玩家加入后出生在 oathbreaker:oathground 的誓言之地准备区。

准备区核心是誓约石台 oathbreaker:oath_stone：玩家右键表示“立誓/准备”。

UI 文案从“Ready”改为“已立誓 / 未立誓”；ReadyPayload 字段名可保留 ready，只在客户端展示层叙事化。

大厅中可查看局外解锁、职业选择、永久属性点分配、魔神契约与献祭界面、记忆残片图鉴。

全员立誓后进入 GENERATING，客户端 HUD 显示“正在生成地图…”，可锁定输入直到传送完成。

客户端只发送“我已立誓”的意图，服务端判定“是否全员就绪”。

2.3 关卡流程状态机
状态	含义	转移事件
LOBBY	玩家在誓言之地等待	全员立誓 → GENERATING
GENERATING	构建 Level N 实例；结构放置 + 怪物锚点扫描	构建完成 + 扫描完成 + 传送就绪 → ACTIVE
ACTIVE	玩家在起点，Boss 存活	Boss 被击杀 → BOSS_CLEARED
BOSS_CLEARED	Boss 已死，发放残片，等待交互/倒计时	交互或倒计时结束 → TRANSITIONING
TRANSITIONING	传送、销毁旧实例、构建下一关	下一关就绪 → ACTIVE（levelIndex+1）
CLEARING	终止/失败/全员退出	回到 LOBBY
Boss 击杀检测使用 LivingDeathEvent，验证死亡实体 UUID 等于实例 bossEntityUUID，且击杀来源玩家属于实例成员。

Boss 房放置交互点（可右键传送门/信标），同时启动较长自动倒计时兜底。

GENERATING 完成条件必须包含“怪物锚点扫描完成”。

2.4 技能抽取
玩家升级时服务端触发抽取。

候选池 = 当前激活魔神池 + 友善魔神池（低权重）；同时受职业、已解锁、阶段、武器分类过滤。

服务端抽 N 个候选，发送 SkillOfferPayload。

客户端发送 SkillChoicePayload，服务端校验候选合法性、槽位类型与数量。

通过后写入 RunState，施加 AttributeModifier，推送 PlayerStatsPayload。

主动/被动槽位数量由职业与局外解锁决定。

2.5 局外成长
账户层记录：

已解锁职业、已解锁技能池；

永久属性点总量、永久槽位数量；

历史最高关卡；

未献祭死魂灵 unspentSouls；

每位魔神的契约等级、当前等级进度、每层已选天赋；

已解锁记忆残片集合。

对局结束只写回“新解锁 / 获得点数 / 死魂灵 / 残片”，不写回当局临时属性分配与技能装配。

2.6 多人规则
伤害、冷却、Mana、技能抽取、阶段推进、账户数据、死魂灵、契约全部服务端权威。

每队伍/对局分配独立关卡实例，实例状态不污染全局。

怪物缩放读取实例 playerCount 与 levelIndex，不在生成循环中反复查询全局在线人数。

死魂灵每人获得相同数量，不受玩家数量影响（详见 2.8）。

记忆残片对每个未拥有的实例成员独立发放，多人均可完整体验叙事。

玩家断线/退出时，实例按生命周期清理；推荐清理并将玩家送回誓言之地。

2.7 叙事推进：五层与记忆残片
五层对应 levelIndex 区间，每层 Boss 固定掉落对应记忆残片：

层	levelIndex 区间	场景	Boss	残片核心信息
大厅	—	誓言之地	—	背誓的起点
第一层	1–2	誓言之地外围 / 灰烬滩	守岸人	王室从未信任誓约，仍派人监视归来者
第二层	3–4	死根浅层	残缺的轮回者	死根吸收灵魂，本该送往树心；中途被截流，残缺灵魂转世失败成怪物
第三层	5–6	截流闸 / 王陵外围	织魂者	前代弃誓者被“干净轮回”收买，负责撕碎别人的灵魂
第四层	7–8	树心通道	守根者	王族残魂接在截流闸上；闸断则王族消亡，守根者既是锁也是被锁者
第五层	9+	树心之前 / 王座	现任统治者 / 死根化身	玩家灵魂也被截过；终局选择摧毁或接管截流闸
记忆残片是普通物品 oathbreaker:lore_fragment，带 fragment_index DataComponent（1–5）。

右键打开只读文本界面，复用 HUD/UI 框架；账户已解锁的残片可随时在图鉴重读。

Boss 死亡时，对实例内所有尚未拥有该残片的玩家发放；已拥有则不再重复掉落。

残片可丢弃/交易，但账户解锁记录不会丢失；图鉴始终可读。

怪物标签叙事化：undead 映射“灵魂残缺的轮回者”，elite 映射“被注入多余灵魂碎片的强化体”，boss 映射“王陵体系的节点”。分类即叙事的一部分，不强制代码。

2.8 魔神契约与死魂灵
死魂灵（Soul Remnant）：局外货币，不进入背包，直接累加到账户计数器 unspentSouls。

获取时机：LivingDeathEvent 中，与经验分配同一处触发。

公式：

text
soulPerPlayer = baseSoul * (1 + 0.15 * levelIndex)
每个参与击杀的玩家获得相同数量，不受玩家数量影响。

参与击杀定义：对该怪物造成过伤害的玩家；Boss 为实例内全体成员。

baseSoul 可配置。总死魂灵随人数线性增长，但每人获得量相同，组队不会降低个人刷魂效率。

献祭：

只能在大厅（誓言之地）进行，不能在战斗中随时献祭。

玩家在献祭界面选择一位魔神，投入死魂灵，填充该魔神当前契约等级进度。

服务端校验 unspentSouls >= amount，扣除并增加 contracts[patron].progress。

进度满后 level++，触发该层天赋选择；溢出进度保留。

推送更新后的 PlayerAccountPayload 刷新界面。

契约层级与天赋：

每位魔神有多个契约层级（contractLevel，如 1–5 或更多）。

每层有 2–3 个天赋选项；达到该层后从中选择 1 个激活。

天赋可切换，但仅限大厅/非战斗状态；切换不消耗死魂灵。

每层天赋选择独立记录在账户层，跨对局保留。

天赋效果可包括：永久 AttributeModifier、解锁该魔神技能池中的特定技能、局内起始资源（初始 Mana/金币）等。

魔神与职业没有任何绑定关系；玩家自行判断某魔神加成适合哪个职业。删除 affinity/契合度设计。

激活魔神：

玩家可同时与多位魔神签约（各自有等级），但每局只能激活一位 activePatron。

激活魔神决定：本局技能抽取的主池来源、本局可用的魔神专属主动技能。

魔神不影响武器适配；武器适配由职业决定（见 2.9）。

敌对魔神可以局外签约，但无法在局内激活；其技能永不进入本局候选池。

魔神关系：

十三魔神两两关系：NEUTRAL / FRIENDLY / HOSTILE；自己与自己为 SELF。

关系数据放 JSON，未声明默认 NEUTRAL；对称，写一条双向生效。

首版只实装 4 位魔神，只需定义这 4 位之间的 6 对关系，其余默认中立。

友善关系不影响献祭效率；只影响技能池（方案 B，低权重进入候选池）。

2.9 职业与武器适配
武器适配由玩家职业决定，不由魔神决定。

weapon_profile DataMap 增加 required_classes 或 class_tags 字段。

非本职业武器：在 LivingHurtEvent 最低优先级将最终伤害设为 1.0。

无 weapon_profile 的武器（空手、方块、第三方未标记武器）不受职业惩罚。

魔神不参与武器适配判定；武器分类与魔神关系解耦。

3. 世界与规则设定
3.1 誓言之地（Oathground）大厅
弃誓者当年从大陆边缘的誓言之地登船离开，立下“永不归来”誓约；如今从同一地点登陆。

大厅是世界预设 oathbreaker:oathground，维度描述“被遗忘的海岸”。

出生点附近放置誓约石台 oathbreaker:oath_stone、残破船骸/石碑，暗示放逐者登船点。

大厅是根维度，不卸载，适合挂账户层 SavedData。

可选参考 EpicQuestHub 的世界替换思路；若自研，使用 flat 生成器仅保留空气层。

自定义天空、雾效、音乐可在 dimension_type JSON 配置，不影响流程。

3.2 轮回链：死根 → 树心
灵魂完整路径：

text
人死后 → 死根吸收 → 沿根系向上传递 → 树心（生命熔炉）熔炼 → 送回生命、完成轮回
树心是大陆中央的巨型生命熔炉，中性转换装置，无意识。

王室在“死根到树心的传递段”截流：灵魂离开死根、尚未抵达树心前，最亮部分被抽走。

被截灵魂抵达树心时已残缺，熔炼后送回的生命“不合格”，转世失败，成为怪物。

王陵建在死根之上，压在根系主干上，本身就是“截流闸”。

第五层“树心”不需要独立维度；它是 Boss 房中央的巨大熔炉结构，复用关卡实例。

3.3 关卡实例
每一关是独立实例，包含起点、战斗/宝箱/精英房间、终点 Boss 房。

实例之间沿固定轴间隔（如 X 轴 10,000 格）避免重叠。

实例状态：levelIndex、instanceOrigin、phase、bossEntityUUID、startPos、bossRoomPos、playerCount、memberUuids、instanceBounds。

实例结束销毁；账户层数据不受影响。

优先验证 EpicQuestDungeoning 作为运行层；若完成/交互流程不灵活，再自研 LevelInstanceState。

GENERATING 期间确认全部结构 piece 已放置、锚点已扫描、Boss 已生成、起点安全落点已确定，再传送玩家。

3.4 房间模板
用原版结构方块框选 3D 区域，保存为 .nbt，放入 data/oathbreaker/structure/rooms/。

每个房间配元数据 JSON：尺寸、出入口方向、标签（start/combat/treasure/elite/boss/shop/soul_sluice）、允许连接方向、权重。

拼接按标签与连接方向选择下一房间；Boss 房固定为最后 piece。

房间中的怪物占位使用“怪物锚点方块”，不要在结构模板中直接放实体。

实例生命周期：创建 → 生成房间 → 扫描怪物锚点 → 波次/战斗 → 玩家离开/死亡 → 销毁 → 结算写回账户层。

3.5 怪物锚点方块
定位：设计期占位符，不是运行期刷怪器。数据静静躺在 BlockEntity 中，由 GENERATING 阶段扫描消费。

原版陷阱：通过结构方块或 /place structure 放置结构时，方块实体虽随结构加载，但其 tick 不会自动激活。不能设计成依赖自身 tick 的主动方块。

数据字段：

字段	类型	说明
EntityNbt	CompoundTag	完整实体 NBT，含 id、装备、属性等
SpawnChance	float	默认 0.5
FallbackEntityType	String（可选）	反序列化失败兜底实体 ID
AnchorId	String（可选）	调试标识
扫描与消费：

结构放置完成，方块实体已从 NBT 恢复。

GENERATING 阶段、传送玩家之前，遍历 instanceBounds 内方块实体。

命中锚点时读取 EntityNbt 与 SpawnChance。

按概率决定是否生成；无论是否生成，removeBlock(pos, false) 移除方块。

生成怪物时按 playerCount、levelIndex 施加 AttributeModifier，setHealth(getMaxHealth())。

扫描完成后才允许 GENERATING → ACTIVE。

边界：

默认玩家不可见：扫描后无条件移除。

需要生成特效时，可在移除前播放粒子/音效，但仍不保留方块。

同步放置可立即扫描；异步区块加载必须在完成回调中扫描。

/place structure、/place jigsaw 不触发方块更新，/place template 会触发；扫描不依赖方块 tick，不影响可靠性。

3.6 分类体系
ItemTags：布尔路由，如 #oathbreaker:weapon_ranged、#oathbreaker:weapon_heavy。

EntityTypeTags：布尔路由，如 #oathbreaker:elite、#oathbreaker:boss、#oathbreaker:undead。

DataMap：富配置路由，如 oathbreaker:weapon_profile、oathbreaker:mob_profile。

技能判定只查询这套分类，不认识具体物品/实体 ID。

叙事映射：undead = 灵魂残缺轮回者；elite = 多余灵魂碎片强化体；boss = 王陵体系节点。

3.7 怪物阶段缩放
第三方怪物通过 EntityTypeTags 分类，mob_profile DataMap 指定 tier 与缩放曲线。

生成时（EntityJoinLevelEvent / MobSpawnEvent / 锚点物化）按当前阶段、实例人数、关卡序号施加 AttributeModifier。

阶段缩放使用固定 UUID；阶段变化时先移除旧 Modifier 再施加新的。

已生成怪物不追溯变强；新生成怪物读取当前阶段快照。

阶段状态存大厅主世界 SavedData；推进条件由服务端事件验证（击杀特定 Boss、进入维度、Advancement 等）。

可选由 Dynamic Difficulty 接管具体缩放公式，自研只负责“何时推进”和“锚点物化时的实例参数”。

3.8 魔神关系矩阵
十三魔神两两关系：NEUTRAL / FRIENDLY / HOSTILE；自己与自己 SELF。

关系数据 data/oathbreaker/oathbreaker/patron_relations.json。

未声明配对默认 NEUTRAL；对称，写一条双向生效。

服务端单例 PatronRelations 提供 getRelation(a, b)，内部缓存，/reload 重建。

首版只实装 4 位魔神，只定义这 4 位之间的 6 对关系；其余默认为中立。未实装魔神等实装时再补。

3.9 数据归属
数据	存放	生命周期
已解锁职业/技能池/永久点数/永久槽位	大厅主世界 AccountSavedData，UUID 为键	跨对局、跨会话
未献祭死魂灵 unspentSouls	AccountSavedData	跨对局
魔神契约等级/进度/每层天赋选择	AccountSavedData	跨对局
已解锁记忆残片集合	AccountSavedData	跨对局
当局属性分配/技能装配/Mana/冷却/activePatron	RunState（Player Attachment 或实例对象）	对局结束丢弃/结算
阶段进度	大厅主世界 StageProgressSavedData	跨对局
关卡实例状态	LevelInstanceState	实例销毁即丢弃
房间模板元数据	数据包自定义注册表	静态数据
怪物锚点数据	结构 NBT 内 MonsterAnchorBlockEntity	构建期扫描后随方块移除
武器/怪物分类	Tag + DataMap	数据包，/reload 生效
魔神关系	patron_relations.json	数据包，/reload 生效
4. 数值与战斗设定
4.1 自定义属性
至少注册：

oathbreaker:generic.ranged_damage：远程攻击伤害

oathbreaker:generic.crit_chance：暴击率

oathbreaker:generic.crit_damage：暴击伤害

oathbreaker:generic.health_regeneration：生命回复（可选）

oathbreaker:generic.armor_penetration：护甲穿透（或复用 AC Core）

生命、护甲、韧性、近战攻击伤害、攻速直接读原版 minecraft:generic.*。

4.2 加成来源统一汇入 Attribute
来源	施加方式
技能/天赋	AttributeInstance.addTransientModifier()，固定 UUID 便于移除
契约天赋永久加成	固定 UUID Modifier，登录/重生时施加
装备	ItemAttributeModifierEvent 注入
药水/Buff	MobEffect.addAttributeModifier() 或同一事件钩子
第三方 Buff	修改标准 Attribute 自动合并
禁止在 UI 或伤害代码中遍历技能/契约列表手动累加。

4.3 伤害管线
前半程：攻击方结算（服务端）

近战：读 ATTACK_DAMAGE.getValue()，叠加近战倍率。

远程：从发射者（Shooter）读 RANGED_DAMAGE.getValue()，叠加远程倍率。

输出“面板值”。

后半程：防御方修正（LivingHurtEvent / LivingIncomingDamageEvent）

固定顺序：

护甲穿透/削减：百分比穿透先于固定穿透。

护甲减伤。

魔抗减伤（独立乘区）。

最终修正：暴击、易伤、全局增伤。

职业武器适配裁决（最低优先级）：非本职业武器 setAmount(1.0f)。

UI 显示 = 前半程面板值；实际跳字 = 后半程修正后的最终值。
两者前半程共享同一 AttributeInstance。

4.4 重生边界
技能施加的 Transient Modifier 死亡重生后需重新施加。

PlayerEvent.Clone 复制持久数据；重生/登录后重建 Modifier。

永久性局外加成（契约天赋、永久属性点）用固定 UUID Modifier，登录/重生时统一施加/移除。

4.5 武器属性覆盖
第三方武器伤害、攻速、耐久等常规属性优先通过 Attribute Modify / DataGear 数据驱动覆盖。

本 Mod 不写每个武器适配器；只维护分类 Tag/DataMap、职业适配与技能加成逻辑。

修改攻速注意自定义 id 与固定 id 的叠加语义差异。

/reload 后服务端重算并推送 WeaponInfoPayload。

4.6 怪物缩放（锚点物化时）
属性	缩放策略	说明
生命值	强缩放：玩家数量为主，levelIndex 基础增长	例：1 + 0.5*(playerCount-1) + 0.15*levelIndex
攻击力	弱缩放：levelIndex 为主，玩家数量小乘区	例：1 + 0.2*(playerCount-1) + 0.25*levelIndex
护甲/韧性	仅精英与 Boss 少量缩放	普通杂兵不缩放护甲
移速/攻速	默认不缩放	特殊词缀单独处理
固定 UUID AttributeModifier；生成后 setHealth(getMaxHealth())。

具体曲线由 mob_profile DataMap 指定。

4.7 职业武器适配
java
@SubscribeEvent(priority = EventPriority.LOWEST)
public static void onClassWeaponMismatch(LivingHurtEvent event) {
    if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
    if (!(event.getEntity() instanceof Monster)) return;

    ItemStack weapon = player.getMainHandItem();
    WeaponProfile profile = weapon.getData(OathbreakerDataMaps.WEAPON_PROFILE);
    if (profile == null) return; // 无分类武器不受限

    if (!profile.isUsableBy(player)) {
        event.setAmount(1.0f);
    }
}
判定依据：玩家当前职业标签 vs weapon_profile.required_classes / class_tags。

魔神不参与此判定。

无 weapon_profile 的空手、方块、第三方未标记武器不受惩罚。

setAmount(1.0f) 在最低优先级执行，覆盖此前所有乘区。

4.8 死魂灵与契约加成
死魂灵获取公式：soulPerPlayer = baseSoul * (1 + 0.15 * levelIndex)。

每人相同，不受玩家数量影响；Boss 为实例全体成员，普通怪物为造成过伤害的玩家。

契约天赋加成走固定 UUID AttributeModifier，登录/重生施加；切换天赋时移除旧 UUID、施加新 UUID。

契约等级只决定技能池可见范围与天赋层数，不直接线性加属性；属性来自所选天赋。

每层天赋 2–3 选 1，可大厅切换；切换不消耗死魂灵，但不可在战斗中切换。

5. 技术架构
5.1 总体架构
text
┌─────────────────────────────────────────────┐
│ 内容层（第三方 Mod）                          │
│ 武器 / 怪物 / Boss / 地牢结构素材              │
└─────────────────────────────────────────────┘
                    ↓ Tag / DataMap 分类
┌─────────────────────────────────────────────┐
│ 规则层（Oathbreaker 自研）                    │
│ 分类路由 / 阶段缩放 / 技能池 / 伤害管线 / UI   │
│ 关卡实例 / 怪物锚点扫描 / 叙事残片 / 魔神契约  │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│ 数据层（Oathbreaker 自研）                    │
│ AccountSavedData（局外） / RunState（局内）    │
│ 房间模板元数据 / 阶段进度 SavedData            │
└─────────────────────────────────────────────┘
5.2 整合包 Mod 清单
内容提供层
Mod	用途	备注
Spartan Weaponry Unofficial	主力武器库，类型丰富，便于 Tag 分类	NeoForge 1.21.1 社区移植
Simply Swords	可选武器风格补充	1.21.1 存在 Architectury 兼容风险，锁定版本测试；冲突则移除
L_Ender's Cataclysm	Boss 与高难度结构，阶段推进触发器来源	
Born in Chaos	常规敌人/精英池，波次生成素材	
Mowzie's Mobs	高质量 AI 精英怪	
Bosses of Mass Destruction	额外 Boss 池	
EpicQuestDungeoning	拼图式地牢运行器，按队伍生成独立实例并自动清理	优先作为地牢运行层；若不满足再自研
Integrated Dungeons and Structures	可选房间素材库/拼接密度参考	非必需
规则与基础设施层
Mod	用途	自研替代关系
Dynamic Difficulty	怪物多维缩放，支持标签/维度/结构	若阶段模型满足，直接接管数值缩放；自研只做阶段触发器与锚点物化
AC Core	暴击、魔抗、法术穿透、生命回复等共享属性与结算	复用结算公式；面板/实际分叉仍自研
Attribute Modify	数据包修改任意物品 Attribute/耐久，/reload 生效	承载第三方武器属性覆盖
Rogue-like Skills	卡牌抽取 UI/多人同步参考	池过滤与槽位校验自研
Advanced Skills: Remastered	可选技能槽位模型参考	可替代自研装配 UI 的一部分
Remnant	离线玩家数据快照	仅当账户数据走 Player Attachment 时需要
Curios API	饰品/箭袋槽	前置
GeckoLib	动画实体前置	前置
5.3 自研 Mod 模块建议
模块	职责
oathbreaker-core	分类注册、DataMap、阶段、SavedData、房间实例、技能动态注册表、怪物锚点方块、魔神关系加载
oathbreaker-combat	自定义属性、伤害管线、怪物锚点扫描、生成缩放、技能触发、职业武器适配
oathbreaker-ui	HUD Layer、Payload 注册、ClientStatsCache、Tooltip 注入、献祭/契约/残片只读界面
oathbreaker-narrative（可合并）	记忆残片物品、残片图鉴、五层叙事映射数据
可以合并为一个 Mod 的多个包，但逻辑边界按上述划分。主包 com.varlike.oathbreaker。

5.4 HUD 与网络
客户端 AddGuiOverlayLayersEvent 移除原版 HEALTH、ARMOR、FOOD 对应 Layer。

注册自定义 LayeredDraw.Layer 绘制 HUD；只读客户端缓存。

网络包：

包	方向	触发
PlayerStatsPayload	S→C	登录、重生、切维度、技能/装备/药水/契约天赋变化、生命护甲变化
WeaponInfoPayload	S→C	手持/快捷栏武器变化、/reload 后重算
CooldownPayload	S→C	冷却开始/结束
ManaPayload	S→C	Mana 变化节点
SkillOfferPayload	S→C	升级抽取候选
SkillChoicePayload	C→S	玩家选择技能
ReadyPayload	C→S	大厅立誓/取消立誓
StageSyncPayload	S→C	阶段推进，HUD 提示
PlayerAccountPayload	S→C	登录、献祭、契约升级、天赋切换、残片解锁
ContractActionPayload	C→S	献祭、选择/切换天赋、激活魔神（大厅/允许时机）
ActivePatronSyncPayload	S→C	本局激活魔神变化
事件驱动推送，不在 tick 中无条件全量发送。

冷却客户端基于“开始时间 + 总时长”本地插值，不每 tick 发包。

ItemTooltipEvent 从缓存读取武器面板伤害、分类、职业适配，追加 tooltip；检查 event.getPlayer() 空值。

不在客户端用 ItemStack 组件手动推算伤害。

5.5 关卡构建与怪物锚点扫描
注册：

MonsterAnchorBlock + MonsterAnchorBlockEntity。

BlockEntity 使用 ValueOutput / ValueInput 与 CompoundTag.CODEC 序列化 EntityNbt，存储 SpawnChance。

构建流程：

GENERATING 开始：创建/定位实例，记录 memberUuids、playerCount、levelIndex。

结构放置：按房间元数据拼接 .nbt。

锚点扫描：遍历 instanceBounds：

读取 EntityNbt、SpawnChance；

随机决定是否生成；

生成则反序列化、移动、施加实例缩放、setHealth(maxHealth)、addFreshEntity；

无论是否生成，removeBlock(pos, false)。

扫描完成：记录锚点总数、生成数、失败数；失败超阈值告警。

传送玩家：仅扫描完成后允许。

伪代码：

java
public void materializeMonsterAnchors(ServerLevel level, LevelInstanceState instance) {
    int total = 0, spawned = 0, failed = 0;
    for (BlockPos pos : BlockPos.betweenClosed(instance.instanceBounds())) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MonsterAnchorBlockEntity anchor)) continue;
        total++;

        boolean shouldSpawn = level.random.nextFloat() < anchor.getSpawnChance();
        if (shouldSpawn) {
            Entity entity = EntityType.loadEntityRecursive(anchor.getEntityNbt(), level, e -> {
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                return e;
            });
            if (entity instanceof LivingEntity living) {
                applyInstanceScaling(living, instance.playerCount(), instance.levelIndex());
                living.setHealth(living.getMaxHealth());
                level.addFreshEntity(living);
                spawned++;
            } else {
                failed++;
            }
        }
        level.removeBlock(pos, false);
    }
    instance.setAnchorScanResult(total, spawned, failed);
}
5.6 叙事与契约 UI
记忆残片：右键 oathbreaker:lore_fragment，客户端打开只读文本界面；文本来自数据包或 fragment_index 映射。账户已解锁残片可在图鉴重读。

献祭界面：仅大厅可打开；展示 unspentSouls、各魔神契约等级/进度、当前层天赋选项。

契约界面：展示每层 2–3 个天赋，已选高亮，可切换；切换请求走 ContractActionPayload，服务端校验是否在大厅/非战斗。

激活魔神：本局开始前/大厅中选择；进入实例后写入 RunState.activePatron。

所有界面只读服务端下发的 PlayerAccountPayload / PlayerStatsPayload，客户端不自行计算契约加成。

5.7 持久化
账户层：大厅主世界 SavedDataStorage，UUID 为键；存职业解锁、永久点数、死魂灵、契约、天赋选择、残片集合。

运行层：RunState 挂 Player Attachment 或实例对象；存当局属性分配、技能装配、Mana/冷却、activePatron。

离线查询：账户数据只在 SavedData 时，在线/离线均可按 UUID 查询；若部分数据在 Player Attachment，引入 Remnant。

实例状态挂实例对象，不写账户层。

对局结束只写回“新解锁/获得点数/死魂灵/残片”，不写回当局临时分配。

6. 数据格式与接口
6.1 武器 Tag 示例
data/oathbreaker/tags/item/weapon_ranged.json

json
{
  "values": [
    "spartanweaponry:longbow",
    "spartanweaponry:heavy_crossbow",
    "some_mod:magic_staff",
    "#oathbreaker:weapon_melee"
  ]
}
6.2 实体 Tag 示例
data/oathbreaker/tags/entity_type/elite.json

json
{
  "values": [
    "born_in_chaos:some_elite",
    "cataclysm:some_miniboss",
    "#minecraft:raiders"
  ]
}
6.3 武器 DataMap 示例
data/oathbreaker/data_maps/item/weapon_profile.json

json
{
  "values": {
    "spartanweaponry:longbow": {
      "category": "ranged",
      "required_classes": ["ranger"],
      "ranged_scale": 1.0
    },
    "#oathbreaker:weapon_heavy": {
      "category": "heavy_melee",
      "required_classes": ["warrior"],
      "ranged_scale": 0.0
    }
  }
}
魔神不参与武器适配；weapon_profile 不再包含 patron 字段。

6.4 怪物 DataMap 示例
data/oathbreaker/data_maps/entity_type/mob_profile.json

json
{
  "values": {
    "cataclysm:some_boss": {
      "tier": "boss",
      "stage_scaling": "boss_curve",
      "skill_resistance": 0.5,
      "scale_armor": true
    },
    "#oathbreaker:elite": {
      "tier": "elite",
      "stage_scaling": "elite_curve",
      "scale_armor": true
    },
    "#oathbreaker:common": {
      "tier": "common",
      "stage_scaling": "common_curve",
      "scale_armor": false
    }
  }
}
6.5 技能定义示例
data/oathbreaker/oathbreaker/skills/ranged_power.json

json
{
  "type": "passive",
  "class": "ranger",
  "patron": "oathbreaker:patron_a",
  "required_contract_level": 1,
  "weight": 10,
  "required_stage": 0,
  "applies_to": {
    "categories": ["ranged"],
    "tags": ["#oathbreaker:weapon_ranged"]
  },
  "effects": [
    {
      "type": "attribute_modifier",
      "attribute": "oathbreaker:generic.ranged_damage",
      "operation": "add_multiplied_base",
      "amount": 0.2
    }
  ]
}
主动技能额外字段：cooldown、resource_cost、cast_effect。

6.6 房间元数据示例
json
{
  "id": "oathbreaker:rooms/crypt_combat_01",
  "size": [16, 8, 16],
  "tags": ["combat"],
  "connectors": [
    { "direction": "north", "offset": [8, 1, 0] },
    { "direction": "south", "offset": [8, 1, 16] }
  ],
  "weight": 10
}
6.7 怪物锚点方块实体数据
字段	类型	说明
EntityNbt	CompoundTag	完整实体 NBT
SpawnChance	float	默认 0.5
FallbackEntityType	String（可选）	兜底实体 ID
AnchorId	String（可选）	调试标识
java
public class MonsterAnchorBlockEntity extends BlockEntity {
    private CompoundTag entityNbt = new CompoundTag();
    private float spawnChance = 0.5f;
    private String fallbackEntityType = "";

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("EntityNbt", CompoundTag.CODEC, entityNbt);
        output.putFloat("SpawnChance", spawnChance);
        output.putString("FallbackEntityType", fallbackEntityType);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("EntityNbt", CompoundTag.CODEC).ifPresent(tag -> entityNbt = tag);
        spawnChance = input.getFloatOr("SpawnChance", 0.5f);
        fallbackEntityType = input.getStringOr("FallbackEntityType", "");
    }
}
6.8 魔神关系数据
data/oathbreaker/oathbreaker/patron_relations.json

json
{
  "relations": [
    { "a": "oathbreaker:patron_a", "b": "oathbreaker:patron_b", "relation": "friendly" },
    { "a": "oathbreaker:patron_a", "b": "oathbreaker:patron_c", "relation": "hostile" },
    { "a": "oathbreaker:patron_b", "b": "oathbreaker:patron_c", "relation": "neutral" }
  ]
}
未声明默认 NEUTRAL；对称，写一条双向生效。

首版只定义已实装 4 位之间的 6 对关系。

6.9 魔神定义与契约层级
data/oathbreaker/oathbreaker/patrons/patron_a.json

json
{
  "id": "oathbreaker:patron_a",
  "display_name": "魔神 A",
  "contract_levels": [
    {
      "level": 1,
      "required_souls": 100,
      "perks": [
        {
          "id": "oathbreaker:patron_a/perk_1a",
          "effects": [
            { "type": "attribute_modifier", "attribute": "minecraft:generic.max_health", "operation": "add_value", "amount": 2 }
          ]
        },
        {
          "id": "oathbreaker:patron_a/perk_1b",
          "effects": [
            { "type": "attribute_modifier", "attribute": "oathbreaker:generic.ranged_damage", "operation": "add_multiplied_base", "amount": 0.1 }
          ]
        }
      ]
    },
    {
      "level": 2,
      "required_souls": 250,
      "perks": [
        {
          "id": "oathbreaker:patron_a/perk_2a",
          "effects": [
            { "type": "unlock_skill", "skill": "oathbreaker:patron_a/skill_x" }
          ]
        },
        {
          "id": "oathbreaker:patron_a/perk_2b",
          "effects": [
            { "type": "starting_resource", "resource": "mana", "amount": 20 }
          ]
        }
      ]
    }
  ]
}
每层 2–3 个天赋；达到层级后选 1 激活，可大厅切换。

天赋效果可组合；属性走固定 UUID Modifier。

魔神与职业无绑定字段。

6.10 玩家账户数据
java
public class PlayerAccount {
    long unspentSouls;
    Map<ResourceLocation, PatronContract> contracts;
    Set<ResourceLocation> unlockedLoreFragments;
    Set<ResourceLocation> unlockedClasses;
    Set<ResourceLocation> unlockedSkillPools;
    long permanentAttributePoints;
    int permanentActiveSlots;
    int permanentPassiveSlots;
    int highestLevelIndex;
}

public class PatronContract {
    int level;
    long progress;
    Map<Integer, ResourceLocation> selectedPerksByLevel; // 契约层 → 天赋 ID
}
6.11 记忆残片
物品：oathbreaker:lore_fragment

DataComponent：fragment_index（1–5）

账户层记录 unlockedLoreFragments；Boss 死亡时对未拥有该残片的实例成员发放。

残片文本可放数据包 data/oathbreaker/oathbreaker/lore/fragment_1.json，客户端只读展示。

已解锁残片可在大厅图鉴重读；物品丢弃不影响账户解锁。

7. 开发规范
版本锁定：NeoForge 1.21.1；所有第三方 Mod 锁定具体构建版本。

服务端权威：伤害、冷却、Mana、技能抽取、阶段推进、账户数据、死魂灵、契约、天赋切换不得由客户端决定。

数据驱动优先：分类、技能池、成长曲线、房间元数据、魔神关系、契约层级走 JSON/DataMap/Tag，/reload 可生效。

禁止 UI 重复计算：HUD 与 Tooltip 只读服务端下发缓存。

生成时快照：怪物数值在生成时施加，不在 tick 中轮询调整。

不依赖方块 tick：怪物锚点由构建流程扫描消费。

扫描门禁：GENERATING → ACTIVE 必须等待锚点扫描完成；失败记录日志。

叙事唯一性：记忆残片按账户记录唯一；Boss 只对未拥有者发放，不重复掉落到同一玩家。

契约切换约束：天赋切换、献祭、激活魔神仅限大厅/允许时机；服务端校验，不信任客户端。

死魂灵每人相同：公式 baseSoul * (1 + 0.15 * levelIndex)，不受玩家数量影响。

魔神与职业解耦：魔神定义中不出现职业绑定字段；武器适配只读职业。

兼容性测试：Simply Swords 等早期测试；冲突则移出核心清单。

/reload 后重算推送：武器分类/属性覆盖/魔神关系变化后，服务端重算并推送相关 Payload。

错误与变更分离：Bug 记错误记录；设计调整记变更记录；兼容性风险记风险表。

8. 错误记录
只记录缺陷、异常、崩溃、数值不一致等。兼容性风险记录在第 10 节。

错误 ID	日期	版本	模块	严重级别	现象	复现步骤	根因	修复方案	状态	关联变更
ERR-0001									待处理/修复中/已解决/无法复现	
ERR-0002										
记录规范：

严重级别：崩溃 / 阻断 / 严重 / 一般 / 轻微。

锚点扫描问题记录：锚点总数、生成数、失败数、结构放置方式、是否异步。

契约问题记录：玩家 UUID、魔神 ID、契约等级、天赋 ID、是否大厅、服务端校验结果。

叙事残片问题记录：Boss 层数、fragment_index、玩家是否已拥有、发放对象列表。

9. 变更记录
版本	日期	类型	模块	变更内容	原因	影响范围	状态
v0.1	2026-11-17	文档初始化	全局	创建设计文档草案，明确内容外包、规则自研	确认方向	全局	草案
v0.2	2026-11-17	文档	全局	整合项目主文档大类，增加错误记录、变更记录、开发规范、关卡状态机与数据接口	完善开发记录规范	全局	草案
v0.3	2026-11-17	设定/技术	关卡构建、怪物生成	增加怪物锚点方块方案；构建期扫描消费；生成时按实例参数缩放；护甲仅精英/Boss 缩放	规避结构放置方块实体不自动 tick 的陷阱	房间模板、状态机、怪物缩放、数据格式	草案
v0.4	2026-11-17	设定/玩法/数据	叙事、大厅、魔神契约、职业武器	整合 Oathbreaker 叙事框架；大厅命名誓言之地与誓约石台；轮回链死根→树心与截流闸；五层与记忆残片；十三魔神关系矩阵、死魂灵、契约层级与天赋；武器适配改由职业决定；魔神与职业解耦；技能池方案 B	完善世界观与局外成长骨架	全局	草案
设定/玩法/技术/数据/兼容/文档					
类型建议： 设定 / 玩法 / 技术 / 数据 / 兼容 / 文档。
状态建议： 草案 / 已确认 / 已实现 / 已废弃。

10. 待确认决策与风险
10.1 已确认决策
编号	决策点	结论
D-01	原版生存机制	保留服务端逻辑，仅替换 HUD 显示
D-02	局外成长粒度	角色绑定（UUID + 大厅主世界 SavedData）
D-03	技能装配层	参考 Rogue-like Skills / Advanced Skills，池过滤与槽位校验自研
D-04	关卡运行层	先用 EpicQuestDungeoning 验证，不足再自研
D-05	怪物缩放	先用 Dynamic Difficulty，自研只做阶段触发器与锚点物化
D-06	账户数据	只存 SavedData；仅离线读 Player Attachment 时引入 Remnant
D-07	Simply Swords	可选，不纳入核心，避免早期兼容阻塞
D-08	Boss 后进入下一关	交互点 + 长倒计时兜底并存
D-09	锚点特效	默认无特效，直接移除；需要反馈时再加
D-10	普通杂兵护甲	不缩放；仅精英/Boss 按配置缩放
D-11	锚点失败阈值	失败超总数 5% 记录严重日志并阻止进入 ACTIVE
D-12	Mod 名称/包名	Oathbreaker / 弃誓者；modid oathbreaker；包 com.varlike.oathbreaker
D-13	大厅命名	誓言之地 oathbreaker:oathground；准备方块誓约石台
D-14	死魂灵获取	每人相同，公式 baseSoul * (1 + 0.15 * levelIndex)，不受人数影响
D-15	契约层级与天赋	每层 2–3 天赋选 1，可大厅切换；多层级
D-16	魔神与职业	无关联；玩家自行判断适配
D-17	武器适配	由职业决定；非本职业武器 1 点伤害；魔神不参与
D-18	技能池	方案 B：友善魔神技能低权重进入候选池
D-19	献祭地点	仅大厅
D-20	敌对魔神	可局外签约，不可局内激活；技能永不进入候选池
D-21	死魂灵掉落	不掉落为物品，直接累加账户计数器
D-22	友善关系与献祭	友善不影响献祭效率
D-23	魔神关系初始密度	首版只定义已实装 4 位之间的 6 对关系，其余默认中立
10.2 仍需确认的边界
编号	决策点	推荐
Q-01	友善魔神技能进入候选池是否要求玩家已与该魔神签约（level≥1）且满足其 required_contract_level	是；否则友善池会白送技能
Q-02	记忆残片是否允许交易/丢弃但账户解锁永久保留	是；图鉴可重读，物品只是触发器
Q-03	契约天赋切换是否消耗资源	不消耗；仅限大厅/非战斗
Q-04	死魂灵普通怪物参与判定	造成过伤害的玩家；Boss 为实例全体成员
Q-05	首版实装 4 位魔神的具体领域/定位	待内容设计，不影响架构
10.3 风险
风险 ID	风险	影响	缓解
R-01	Simply Swords 与 Architectury 兼容问题	启动/战斗异常	锁定版本测试；冲突则移除
R-02	EpicQuestDungeoning 完成流程不满足“下一关”过渡	需自研实例层	两关卡原型验证
R-03	移除原版 HUD 渲染后食物机制仍运行	玩家看不到饥饿值但仍在消耗	HUD 中以其他形式提示
R-04	/reload 后武器/魔神关系与客户端缓存不同步	显示旧值	服务端重算并推送相关 Payload
R-05	阶段推进时旧怪物不追溯变强	战斗体验突兀	文档明确“生成时快照”
R-06	结构放置后锚点 BlockEntity 不自动 tick	若误设计为主动方块将不工作	锚点只做被动数据载体
R-07	异步结构放置未完成就扫描	锚点漏扫	完成回调扫描 + 状态机门禁
R-08	第三方实体 NBT 反序列化失败	怪物缺失	记录失败数；可选 FallbackEntityType；超阈值告警
R-09	多人高关卡攻击力缩放过高	玩家被秒杀	攻击力弱缩放
R-10	记忆残片重复掉落或多人漏发	叙事体验不一致	账户记录唯一性；Boss 对未拥有者发放
R-11	契约天赋切换在战斗中滥用	数值突变	服务端校验仅大厅/非战斗
R-12	死魂灵经济膨胀	契约过快满级	baseSoul 与层级需求可配置
11. 里程碑与验收
11.1 里程碑
阶段	内容	验收标准
M1	分类层 + 武器属性覆盖	第三方武器打 Tag/DataMap，/reload 后技能能识别分类
M2	自定义属性 + 伤害管线	UI 面板值与 LivingHurtEvent 前半程一致；高护甲目标实际伤害低于面板
M3	HUD 与网络	原版血条/饱食度消失；HUD 显示服务端下发值；技能变化后更新
M4	技能池与抽取	升级抽取候选、服务端校验槽位、装配后属性变化
M5	怪物锚点与阶段缩放	结构放置后扫描锚点；按实例人数/关卡缩放；锚点无残留；新生成生效，旧怪物不变
M6	房间模板与实例	结构 NBT 拼接；多人独立实例；结束清理
M7	账户层与结算	局外解锁/点数跨对局保留；局内临时状态不写回
M8	多关卡循环	大厅立誓 → 生成 → 锚点扫描 → Boss → 交互 → 下一关完整跑通
M9	叙事残片	五层 Boss 掉落对应残片；账户唯一；只读界面可读；图鉴可重读
M10	魔神契约	死魂灵每人相同获取；大厅献祭；契约层级/天赋选择/切换；激活魔神；友善低权重技能池
M11	职业武器适配	非本职业武器 1 点伤害；无分类武器不受限；魔神不参与
11.2 验收标准
任意第三方武器可通过 Tag/DataMap 归入类别，技能加成正确匹配；非本职业武器造成 1 点伤害。

HUD 显示的生命、护甲、近战/远程伤害来自服务端快照，技能/装备/契约天赋变化后更新。

对高护甲目标，HUD 面板远程伤害不变，实际跳字降低；对零护甲目标两者接近。

第三方怪物按 EntityTypeTags 和阶段获得属性缩放，新生成怪物生效。

升级时从职业池 + 激活魔神池抽取；友善魔神技能低权重；敌对魔神技能永不出现；服务端校验槽位。

房间 .nbt 可拼接为随机地图，多队伍实例隔离，结束清理。

玩家退出重进后，局外解锁、永久点数、死魂灵、契约等级、每层天赋选择、残片解锁保留；当局临时状态重置。

大厅全员立誓后生成关卡，起点传送、Boss 击杀、交互进入下一关流程完整跑通。

怪物锚点方块在结构放置后被主动扫描消费，玩家到达时无残留占位符；未命中锚点同样移除。

锚点生成怪物按 playerCount 与 levelIndex 缩放；4 人生命显著高于单人，攻击力不出现“摸到即死”。

普通杂兵不缩放护甲；精英/Boss 按 mob_profile 决定是否缩放。

五层 Boss 分别掉落对应记忆残片；同一玩家不重复获得；多人未拥有者均获得。

死魂灵每人获得相同数量，不受玩家数量影响；献祭仅大厅；契约天赋仅大厅切换。

魔神与职业无绑定；武器适配只由职业决定。

12. 附录
12.1 术语
弃誓者/Oathbreaker：背弃“永不归来”誓言的死刑犯后裔/回归者；也是 Mod 名称。

誓言之地/Oathground：大厅维度，弃誓者当年登船离开的起点。

誓约石台：大厅准备方块，右键“立誓”。

死根：吸收灵魂的根系网络。

树心/生命熔炉：灵魂熔炼与轮回的中央装置。

截流闸：王室在死根→树心传递段截取灵魂的装置/结构；王陵建在其上。

记忆残片：五枚叙事物品，右键只读文本，账户唯一解锁。

死魂灵：局外货币，击杀怪物获得，每人相同，用于献祭魔神。

契约等级：每位魔神独立的局外成长层级。

契约天赋：每层 2–3 选 1 的永久加成/解锁，可大厅切换。

激活魔神：本局唯一生效的魔神，决定技能主池。

账户层：跨对局保存的局外数据。

运行层：单次对局/实例内的临时状态。

面板值：攻击方结算后的 UI 显示值，不含目标护甲/魔抗。

实际伤害：经过防御方修正后的最终伤害。

生成时快照：怪物生成时按当前阶段、实例人数、关卡序号施加数值，之后不追溯。

怪物锚点：结构 NBT 中的占位方块实体，存储实体 NBT 与生成概率；构建期扫描消费。

12.2 参考方向
NeoForge 1.21.1：Data Components、DataMap、PayloadRegistrar、LayeredDraw、SavedData、ValueOutput/ValueInput。

EpicQuestDungeoning：拼图式实例地牢与自动清理。

Dynamic Difficulty：标签/阶段怪物缩放。

AC Core：暴击、魔抗、穿透等战斗结算属性。

Rogue-like Skills / Advanced Skills：技能抽取与槽位 UI 参考。

EpicQuestHub：虚空大厅世界替换思路参考。

原版刷怪笼 SpawnData.entity：CompoundTag 承载完整实体 NBT 的参考。