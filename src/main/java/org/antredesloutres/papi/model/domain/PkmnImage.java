package org.antredesloutres.papi.model.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "pkmn_images", indexes = {
        @Index(name = "idx_pkmn_images_pkmn_id", columnList = "pkmn_id")
})
public class PkmnImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pkmn_id", nullable = false)
    private Pkmn pkmn;

    @Column(nullable = false, length = 2048)
    private String url;

    private String name;

    @ElementCollection
    @CollectionTable(name = "pkmn_image_tags", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "is_main", nullable = false)
    private boolean main = false;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    public PkmnImage() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pkmn getPkmn() {
        return pkmn;
    }

    public void setPkmn(Pkmn pkmn) {
        this.pkmn = pkmn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PkmnImage other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
