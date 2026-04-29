package org.antredesloutres.papi.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "abilities")
public class Ability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ability_id")
    private Set<AbilityTranslation> lang = new HashSet<>();

    public Ability() {}

    public Ability(String symbol) {
        this.symbol = symbol;
        this.lang.add(new AbilityTranslation(Language.EN, "New ability", symbol + " english description"));
    }

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

    public Set<AbilityTranslation> getLang() {
        return this.lang;
    }

    public void setLang(Set<AbilityTranslation> lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ability ability)) return false;
        return Objects.equals(id, ability.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
