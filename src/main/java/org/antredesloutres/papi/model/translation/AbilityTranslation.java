package org.antredesloutres.papi.model.translation;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;

import java.util.Objects;

@Entity
@Table(name = "ability_translations", indexes = {
        @Index(name = "idx_ability_translations_ability", columnList = "ability_id")
})
public class AbilityTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String name;
    private String description;

    public AbilityTranslation() {}

    public AbilityTranslation(Language language, String name, String description) {
        this.language = language;
        this.name = name;
        this.description = description;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbilityTranslation that)) return false;
        return Objects.equals(name, that.name) && language == that.language;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, language);
    }
}
