package io.lumine.xikage.mythicmobs.skills.conditions;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public interface ISkillMetaComparisonCondition {
    boolean check(SkillMetadata meta, AbstractEntity entity);
}
