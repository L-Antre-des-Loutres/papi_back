package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.antredesloutres.papi.model.enumerated.EggGroup;
import org.antredesloutres.papi.model.enumerated.ExperienceGroup;

import java.util.Set;

/**
 * Partial update payload for a Pkmn.
 * All fields are optional, but if provided must be valid and within defined constraints.
 */
public class PkmnUpdateDto {

    @Size(min = 1, max = 100)
    private String symbol;

    private Integer primaryTypeId;
    private Integer secondaryTypeId;

    private Integer primaryAbilityId;
    private Integer secondaryAbilityId;
    private Integer hiddenAbilityId;

    @Min(0) @Max(100_000)
    private Integer height;
    @Min(0) @Max(10_000_000)
    private Integer weight;

    @Min(0) @Max(255) private Integer baseHp;
    @Min(0) @Max(255) private Integer baseAttack;
    @Min(0) @Max(255) private Integer baseDefense;
    @Min(0) @Max(255) private Integer baseSpeAttack;
    @Min(0) @Max(255) private Integer baseSpeDefense;
    @Min(0) @Max(255) private Integer baseSpeed;

    @Min(0) @Max(3) private Integer evHp;
    @Min(0) @Max(3) private Integer evAttack;
    @Min(0) @Max(3) private Integer evDefense;
    @Min(0) @Max(3) private Integer evSpeAttack;
    @Min(0) @Max(3) private Integer evSpeDefense;
    @Min(0) @Max(3) private Integer evSpeed;

    @Min(0) @Max(1000)
    private Integer experienceYield;
    private ExperienceGroup experienceGroup;
    @Min(0) @Max(255)
    private Integer baseFriendship;

    private Set<EggGroup> eggGroups;
    @Min(0) @Max(255)
    private Integer eggCycles;
    @Min(0) @Max(255)
    private Integer catchRate;

    @Min(-1) @Max(100) // Usually a percentage, but -1 is used to indicate genderless
    private Integer maleRatio;

    private Set<String> tags;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Integer getPrimaryTypeId() { return primaryTypeId; }
    public void setPrimaryTypeId(Integer primaryTypeId) { this.primaryTypeId = primaryTypeId; }

    public Integer getSecondaryTypeId() { return secondaryTypeId; }
    public void setSecondaryTypeId(Integer secondaryTypeId) { this.secondaryTypeId = secondaryTypeId; }

    public Integer getPrimaryAbilityId() { return primaryAbilityId; }
    public void setPrimaryAbilityId(Integer primaryAbilityId) { this.primaryAbilityId = primaryAbilityId; }

    public Integer getSecondaryAbilityId() { return secondaryAbilityId; }
    public void setSecondaryAbilityId(Integer secondaryAbilityId) { this.secondaryAbilityId = secondaryAbilityId; }

    public Integer getHiddenAbilityId() { return hiddenAbilityId; }
    public void setHiddenAbilityId(Integer hiddenAbilityId) { this.hiddenAbilityId = hiddenAbilityId; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getBaseHp() { return baseHp; }
    public void setBaseHp(Integer baseHp) { this.baseHp = baseHp; }

    public Integer getBaseAttack() { return baseAttack; }
    public void setBaseAttack(Integer baseAttack) { this.baseAttack = baseAttack; }

    public Integer getBaseDefense() { return baseDefense; }
    public void setBaseDefense(Integer baseDefense) { this.baseDefense = baseDefense; }

    public Integer getBaseSpeAttack() { return baseSpeAttack; }
    public void setBaseSpeAttack(Integer baseSpeAttack) { this.baseSpeAttack = baseSpeAttack; }

    public Integer getBaseSpeDefense() { return baseSpeDefense; }
    public void setBaseSpeDefense(Integer baseSpeDefense) { this.baseSpeDefense = baseSpeDefense; }

    public Integer getBaseSpeed() { return baseSpeed; }
    public void setBaseSpeed(Integer baseSpeed) { this.baseSpeed = baseSpeed; }

    public Integer getEvHp() { return evHp; }
    public void setEvHp(Integer evHp) { this.evHp = evHp; }

    public Integer getEvAttack() { return evAttack; }
    public void setEvAttack(Integer evAttack) { this.evAttack = evAttack; }

    public Integer getEvDefense() { return evDefense; }
    public void setEvDefense(Integer evDefense) { this.evDefense = evDefense; }

    public Integer getEvSpeAttack() { return evSpeAttack; }
    public void setEvSpeAttack(Integer evSpeAttack) { this.evSpeAttack = evSpeAttack; }

    public Integer getEvSpeDefense() { return evSpeDefense; }
    public void setEvSpeDefense(Integer evSpeDefense) { this.evSpeDefense = evSpeDefense; }

    public Integer getEvSpeed() { return evSpeed; }
    public void setEvSpeed(Integer evSpeed) { this.evSpeed = evSpeed; }

    public Integer getExperienceYield() { return experienceYield; }
    public void setExperienceYield(Integer experienceYield) { this.experienceYield = experienceYield; }

    public ExperienceGroup getExperienceGroup() { return experienceGroup; }
    public void setExperienceGroup(ExperienceGroup experienceGroup) { this.experienceGroup = experienceGroup; }

    public Integer getBaseFriendship() { return baseFriendship; }
    public void setBaseFriendship(Integer baseFriendship) { this.baseFriendship = baseFriendship; }

    public Set<EggGroup> getEggGroups() { return eggGroups; }
    public void setEggGroups(Set<EggGroup> eggGroups) { this.eggGroups = eggGroups; }

    public Integer getEggCycles() { return eggCycles; }
    public void setEggCycles(Integer eggCycles) { this.eggCycles = eggCycles; }

    public Integer getCatchRate() { return catchRate; }
    public void setCatchRate(Integer catchRate) { this.catchRate = catchRate; }

    public Integer getMaleRatio() { return maleRatio; }
    public void setMaleRatio(Integer maleRatio) { this.maleRatio = maleRatio; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}
