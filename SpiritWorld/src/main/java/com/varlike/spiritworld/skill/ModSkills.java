package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.resources.ResourceLocation;

public class ModSkills {

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, path);
    }

    public static void registerAll() {
        SkillTree.clear();

        // 中心节点：被动
        SkillTree.register(SkillNode.builder(id("spirit_sense"))
                .type(SkillType.PASSIVE)
                .grid(2, 2)
                .maxLevel(3)
                .cost(5)
                .requiredVisionLevel(0)
                .icon("感")
                .name("skill.spiritworld.spirit_sense")
                .desc("skill.spiritworld.spirit_sense.desc")
                .build());

        // 主动技能：正方形，需要灵视等级 1
        SkillTree.register(SkillNode.builder(id("spirit_burst"))
                .type(SkillType.ACTIVE)
                .grid(1, 1)
                .maxLevel(1)
                .cost(10)
                .requiredVisionLevel(1)
                .prereq(id("spirit_sense"))
                .icon("爆")
                .name("skill.spiritworld.spirit_burst")
                .desc("skill.spiritworld.spirit_burst.desc")
                .build());

        // 被动技能：需要灵视等级 1
        SkillTree.register(SkillNode.builder(id("spirit_focus"))
                .type(SkillType.PASSIVE)
                .grid(3, 1)
                .maxLevel(5)
                .cost(8)
                .requiredVisionLevel(1)
                .prereq(id("spirit_sense"))
                .icon("聚")
                .name("skill.spiritworld.spirit_focus")
                .desc("skill.spiritworld.spirit_focus.desc")
                .build());

        // 特殊技能：六边形，需要灵视等级 2
        SkillTree.register(SkillNode.builder(id("spirit_vision"))
                .type(SkillType.SPECIAL)
                .grid(2, 3)
                .maxLevel(1)
                .cost(20)
                .requiredVisionLevel(2)
                .prereq(id("spirit_focus"))
                .icon("视")
                .name("skill.spiritworld.spirit_vision")
                .desc("skill.spiritworld.spirit_vision.desc")
                .build());

        // 高级主动技能：需要灵视等级 3
        SkillTree.register(SkillNode.builder(id("spirit_storm"))
                .type(SkillType.ACTIVE)
                .grid(1, 4)
                .maxLevel(3)
                .cost(15)
                .requiredVisionLevel(3)
                .prereq(id("spirit_burst"))
                .prereq(id("spirit_vision"))
                .icon("暴")
                .name("skill.spiritworld.spirit_storm")
                .desc("skill.spiritworld.spirit_storm.desc")
                .build());

        // 特殊技能：另一个分支
        SkillTree.register(SkillNode.builder(id("spirit_echo"))
                .type(SkillType.SPECIAL)
                .grid(4, 3)
                .maxLevel(1)
                .cost(25)
                .requiredVisionLevel(2)
                .prereq(id("spirit_focus"))
                .icon("回")
                .name("skill.spiritworld.spirit_echo")
                .desc("skill.spiritworld.spirit_echo.desc")
                .build());
    }
}