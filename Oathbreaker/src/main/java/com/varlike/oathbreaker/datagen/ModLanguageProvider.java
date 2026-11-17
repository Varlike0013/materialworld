package com.varlike.oathbreaker.datagen;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.core.attribute.ModAttributes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    private final boolean isChinese;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Oathbreaker.MOD_ID, locale);
        this.isChinese = locale.equals("zh_cn");
    }

    @Override
    protected void addTranslations() {
        defineTranslations();
    }

    /** 登记单条翻译：en 与 zh 同时给出，运行时按 locale 二选一 */
    private void translate(String key, String en, String zh) {
        add(key, isChinese ? zh : en);
    }

    /** 集中定义所有翻译，新增条目只需在此加一行 translate(...) */
    private void defineTranslations() {
        // 属性
        translate(ModAttributes.RANGED_DAMAGE.get().getDescriptionId(),
                "Ranged Damage", "远程伤害");
        translate(ModAttributes.CRIT_CHANCE.get().getDescriptionId(),
                "Critical Chance", "暴击率");
        translate(ModAttributes.CRIT_DAMAGE.get().getDescriptionId(),
                "Critical Damage", "暴击伤害");
        translate(ModAttributes.HEALTH_REGENERATION.get().getDescriptionId(),
                "Health Regeneration", "生命回复");
        translate(ModAttributes.ARMOR_PENETRATION.get().getDescriptionId(),
                "Armor Penetration", "护甲穿透");
        translate(ModAttributes.ABILITY_POWER.get().getDescriptionId(),
                "Ability Power", "法术强度");
        translate(ModAttributes.MAGIC_RESIST.get().getDescriptionId(),
                "Magic Resist", "魔法抗性");
        translate(ModAttributes.ARMOR_PENETRATION_PERCENT.get().getDescriptionId(),
                "Armor Penetration %", "护甲穿透率");
        translate(ModAttributes.MAGIC_PENETRATION.get().getDescriptionId(),
                "Magic Penetration", "法术穿透");
        translate(ModAttributes.MAGIC_PENETRATION_PERCENT.get().getDescriptionId(),
                "Magic Penetration %", "法术穿透率");
        translate(ModAttributes.ABILITY_HASTE.get().getDescriptionId(),
                "Ability Haste", "技能急速");
        translate(ModAttributes.LIFESTEAL.get().getDescriptionId(),
                "Lifesteal", "生命偷取");
        translate(ModAttributes.OMNIVAMP.get().getDescriptionId(),
                "Omnivamp", "全能吸血");
        translate(ModAttributes.MAX_MANA.get().getDescriptionId(),
                "Max Mana", "最大法力值");
        translate(ModAttributes.MANA_REGENERATION.get().getDescriptionId(),
                "Mana Regeneration", "法力回复");

        // 伤害类型死亡消息（damage_type: oathbreaker:oathbreaker_weapon）
        translate("death.attack.oathbreaker_weapon",
                "%1$s was slain by %2$s", "%1$s 被 %2$s 击杀");

        // 后续新增：技能、魔神、职业、HUD 文案等
        // translate("skill.oathbreaker.warrior.cleave", "Cleave", "顺劈斩");
    }
}