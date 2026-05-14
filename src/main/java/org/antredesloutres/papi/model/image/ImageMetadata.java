package org.antredesloutres.papi.model.image;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.Language;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_metadata", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pkmn_id", "language"})
})
public class ImageMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pkmn_id", nullable = false)
    private Integer pkmnId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false)
    private String filename;

    @Column(columnDefinition = "TEXT")
    private String stateHash;

    private LocalDateTime updatedAt;

    public ImageMetadata() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPkmnId() {
        return pkmnId;
    }

    public void setPkmnId(Integer pkmnId) {
        this.pkmnId = pkmnId;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStateHash() {
        return stateHash;
    }

    public void setStateHash(String stateHash) {
        this.stateHash = stateHash;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
