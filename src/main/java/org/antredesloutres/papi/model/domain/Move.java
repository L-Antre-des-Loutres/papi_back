package org.antredesloutres.papi.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.MoveTranslation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "moves", indexes = {
        @Index(name = "idx_moves_type", columnList = "type_id")
})
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    private int power;

    /** Accuracy as a percentage (0-100). */
    private int accuracy;

    /** Power points. */
    private int pp;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "move_id")
    private Set<MoveTranslation> lang = new HashSet<>();

    public Move() {}

    public Move(String symbol, String nameEN, Type type, int power, int accuracy, int pp) {
        this.symbol = symbol;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;

        MoveTranslation langEN = new MoveTranslation();
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getPp() {
        return pp;
    }

    public void setPp(int pp) {
        this.pp = pp;
    }

    public Set<MoveTranslation> getLang() {
        return lang;
    }

    public void setLang(Set<MoveTranslation> lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move move)) return false;
        return Objects.equals(id, move.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
