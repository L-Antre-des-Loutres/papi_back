package org.antredesloutres.papi.model.translation;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;

import java.util.Objects;

@Entity
@Table(name = "pokemon_translations", indexes = {
        @Index(name = "idx_pokemon_translations_pkmn", columnList = "pkmn_id")
})
public class PkmnTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String pkmnName;

    /** Form name (e.g. "mega", "alolan"). Defaults to "normal". */
    private String formName = "normal";

    private String description;

    public PkmnTranslation() {}

    public PkmnTranslation(Language language, String pkmnName, String formName, String description) {
        this.language = language;
        this.pkmnName = pkmnName;
        this.formName = formName;
        this.description = description;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getPkmnName() {
        return pkmnName;
    }

    public void setPkmnName(String pkmnName) {
        this.pkmnName = pkmnName;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PkmnTranslation that)) return false;
        return Objects.equals(pkmnName, that.pkmnName) && language == that.language;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkmnName, language);
    }
}
