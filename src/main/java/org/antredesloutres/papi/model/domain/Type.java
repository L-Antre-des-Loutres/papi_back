package org.antredesloutres.papi.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.TypeTranslation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "types")
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "symbol", unique = true)
    private String symbol;

    /** Hex color code (e.g. "#ff0000"). */
    private String color;

    /** Free-form tags (e.g. "custom", "gen1"). */
    private Set<String> tags = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "type_id")
    private Set<TypeTranslation> lang = new HashSet<>();

    public Type() {}

    public Type(String symbol, String color, String nameEN) {
        this.symbol = symbol;
        this.color = color;

        TypeTranslation langEN = new TypeTranslation();
        langEN.setLanguage(Language.EN);
        langEN.setName(nameEN);
        this.lang.add(langEN);
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<TypeTranslation> getLang() {
        return lang;
    }

    public void setLang(Set<TypeTranslation> lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Type type)) return false;
        return Objects.equals(id, type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
