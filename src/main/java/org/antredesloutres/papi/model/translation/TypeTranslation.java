package org.antredesloutres.papi.model.translation;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;

import java.util.Objects;

@Entity
@Table(name = "type_translations", indexes = {
        @Index(name = "idx_type_translations_type", columnList = "type_id")
})
public class TypeTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String name;

    public TypeTranslation() {}

    public TypeTranslation(Language language, String name) {
        this.language = language;
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TypeTranslation that)) return false;
        return Objects.equals(name, that.name) && language == that.language;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, language);
    }
}
