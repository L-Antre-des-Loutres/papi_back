package org.antredesloutres.papi.model.translation;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;

import java.util.Objects;

@Entity
@Table(name = "move_translations", indexes = {
        @Index(name = "idx_move_translations_move", columnList = "move_id")
})
public class MoveTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String name;
    private String description;

    public MoveTranslation() {}

    public MoveTranslation(Language language, String name, String description) {
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
        if (!(o instanceof MoveTranslation that)) return false;
        return Objects.equals(name, that.name) && language == that.language;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, language);
    }
}
