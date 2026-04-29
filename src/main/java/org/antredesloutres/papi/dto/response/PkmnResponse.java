package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.EggGroup;
import org.antredesloutres.papi.model.enumerated.ExperienceGroup;

import java.util.Set;

public record PkmnResponse(
        Integer id,
        String symbol,
        Integer nationalDexNumber,
        TypeRefResponse primaryType,
        TypeRefResponse secondaryType,
        AbilityRefResponse primaryAbility,
        AbilityRefResponse secondaryAbility,
        AbilityRefResponse hiddenAbility,
        Set<String> tags,
        int height,
        int weight,
        int baseHp,
        int baseAttack,
        int baseDefense,
        int baseSpeAttack,
        int baseSpeDefense,
        int baseSpeed,
        int evHp,
        int evAttack,
        int evDefense,
        int evSpeAttack,
        int evSpeDefense,
        int evSpeed,
        int experienceYield,
        ExperienceGroup experienceGroup,
        int catchRate,
        int maleRatio,
        int eggCycles,
        Set<EggGroup> eggGroups,
        int baseFriendship
) {}
