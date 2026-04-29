package org.antredesloutres.papi.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.EggGroup;
import org.antredesloutres.papi.model.enumerated.ExperienceGroup;
import org.antredesloutres.papi.model.translation.PkmnTranslation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "pokemon", indexes = {
        @Index(name = "idx_pokemon_primary_type",      columnList = "primary_type_id"),
        @Index(name = "idx_pokemon_secondary_type",    columnList = "secondary_type_id"),
        @Index(name = "idx_pokemon_primary_ability",   columnList = "primary_ability_id"),
        @Index(name = "idx_pokemon_secondary_ability", columnList = "secondary_ability_id"),
        @Index(name = "idx_pokemon_hidden_ability",    columnList = "hidden_ability_id"),
        @Index(name = "idx_pokemon_national_dex",      columnList = "nationalDexNumber")
})
public class Pkmn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "symbol", unique = true)
    private String symbol;

    private Integer nationalDexNumber;

    @ManyToOne
    @JoinColumn(name = "primary_type_id")
    private Type primaryType;
    @ManyToOne
    @JoinColumn(name = "secondary_type_id")
    private Type secondaryType;

    @ManyToOne
    @JoinColumn(name = "primary_ability_id")
    private Ability primaryAbility;
    @ManyToOne
    @JoinColumn(name = "secondary_ability_id")
    private Ability secondaryAbility;
    @ManyToOne
    @JoinColumn(name = "hidden_ability_id")
    private Ability hiddenAbility;

    /** Free-form tags (e.g. "starter", "gen1"). */
    private Set<String> tags = new HashSet<>();

    /** Height in centimeters. */
    private int height;

    /** Weight in kilograms. */
    private int weight;

    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpeAttack;
    private int baseSpeDefense;
    private int baseSpeed;

    /** EV yield on KO. */
    private int evHp;
    private int evAttack;
    private int evDefense;
    private int evSpeAttack;
    private int evSpeDefense;
    private int evSpeed;

    /** Base experience yield on KO. */
    private int experienceYield;

    @Enumerated(EnumType.STRING)
    private ExperienceGroup experienceGroup;

    /** Catch rate (0-255). */
    private int catchRate;

    /** Male ratio as a percentage (0-100), or -1 for genderless. */
    private int maleRatio;

    private int eggCycles;

    @Enumerated(EnumType.STRING)
    private Set<EggGroup> eggGroups = new HashSet<>();

    /** Base friendship (0-255). */
    private int baseFriendship;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pkmn_id")
    private Set<PkmnTranslation> lang = new HashSet<>();

    public Pkmn() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getNationalDexNumber() {
        return nationalDexNumber;
    }

    public void setNationalDexNumber(Integer nationalDexNumber) {
        this.nationalDexNumber = nationalDexNumber;
    }

    public Type getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(Type primaryType) {
        this.primaryType = primaryType;
    }

    public Type getSecondaryType() {
        return secondaryType;
    }

    public void setSecondaryType(Type secondaryType) {
        this.secondaryType = secondaryType;
    }

    public Ability getPrimaryAbility() {
        return primaryAbility;
    }

    public void setPrimaryAbility(Ability primaryAbility) {
        this.primaryAbility = primaryAbility;
    }

    public Ability getSecondaryAbility() {
        return secondaryAbility;
    }

    public void setSecondaryAbility(Ability secondaryAbility) {
        this.secondaryAbility = secondaryAbility;
    }

    public Ability getHiddenAbility() {
        return hiddenAbility;
    }

    public void setHiddenAbility(Ability hiddenAbility) {
        this.hiddenAbility = hiddenAbility;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
    }

    public int getBaseSpeAttack() {
        return baseSpeAttack;
    }

    public void setBaseSpeAttack(int baseSpeAttack) {
        this.baseSpeAttack = baseSpeAttack;
    }

    public int getBaseSpeDefense() {
        return baseSpeDefense;
    }

    public void setBaseSpeDefense(int baseSpeDefense) {
        this.baseSpeDefense = baseSpeDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public int getEvHp() {
        return evHp;
    }

    public void setEvHp(int evHp) {
        this.evHp = evHp;
    }

    public int getEvAttack() {
        return evAttack;
    }

    public void setEvAttack(int evAttack) {
        this.evAttack = evAttack;
    }

    public int getEvDefense() {
        return evDefense;
    }

    public void setEvDefense(int evDefense) {
        this.evDefense = evDefense;
    }

    public int getEvSpeAttack() {
        return evSpeAttack;
    }

    public void setEvSpeAttack(int evSpeAttack) {
        this.evSpeAttack = evSpeAttack;
    }

    public int getEvSpeDefense() {
        return evSpeDefense;
    }

    public void setEvSpeDefense(int evSpeDefense) {
        this.evSpeDefense = evSpeDefense;
    }

    public int getEvSpeed() {
        return evSpeed;
    }

    public void setEvSpeed(int evSpeed) {
        this.evSpeed = evSpeed;
    }

    public int getExperienceYield() {
        return experienceYield;
    }

    public void setExperienceYield(int experienceYield) {
        this.experienceYield = experienceYield;
    }

    public ExperienceGroup getExperienceGroup() {
        return experienceGroup;
    }

    public void setExperienceGroup(ExperienceGroup experienceGroup) {
        this.experienceGroup = experienceGroup;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public void setCatchRate(int catchRate) {
        this.catchRate = catchRate;
    }

    public int getMaleRatio() {
        return maleRatio;
    }

    public void setMaleRatio(int maleRatio) {
        this.maleRatio = maleRatio;
    }

    public int getEggCycles() {
        return eggCycles;
    }

    public void setEggCycles(int eggCycles) {
        this.eggCycles = eggCycles;
    }

    public Set<EggGroup> getEggGroups() {
        return eggGroups;
    }

    public void setEggGroups(Set<EggGroup> eggGroups) {
        this.eggGroups = eggGroups;
    }

    public int getBaseFriendship() {
        return baseFriendship;
    }

    public void setBaseFriendship(int baseFriendship) {
        this.baseFriendship = baseFriendship;
    }

    public Set<PkmnTranslation> getLang() {
        return lang;
    }

    public void setLang(Set<PkmnTranslation> lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pkmn pkmn)) return false;
        return Objects.equals(id, pkmn.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
