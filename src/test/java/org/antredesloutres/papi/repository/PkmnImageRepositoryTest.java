package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext
class PkmnImageRepositoryTest {

    @Autowired
    PkmnImageRepository pkmnImageRepository;
    @Autowired
    PkmnRepository pkmnRepository;
    @Autowired
    TestEntityManager em;

    private Pkmn persistPkmn(String symbol) {
        Pkmn p = new Pkmn();
        p.setSymbol(symbol);
        return pkmnRepository.save(p);
    }

    private PkmnImage persistImage(Pkmn pkmn, String url, String name, Set<String> tags, boolean main) {
        PkmnImage img = new PkmnImage();
        img.setPkmn(pkmn);
        img.setUrl(url);
        img.setName(name);
        img.setTags(tags);
        img.setMain(main);
        img.setAddedAt(Instant.now());
        return pkmnImageRepository.save(img);
    }

    @Test
    void findByPkmn_Id_returnsImagesForPkmn() {
        // arrange
        Pkmn p1 = persistPkmn("pikachu");
        Pkmn p2 = persistPkmn("raichu");
        persistImage(p1, "https://x/a.png", "front", Set.of(), false);
        persistImage(p1, "https://x/b.png", "back", Set.of(), false);
        persistImage(p2, "https://x/c.png", "front", Set.of(), false);

        // act
        List<PkmnImage> result = pkmnImageRepository.findByPkmn_Id(p1.getId());

        // assert
        assertThat(result).hasSize(2)
                .allSatisfy(img -> assertThat(img.getPkmn().getId()).isEqualTo(p1.getId()));
    }

    @Test
    void findByPkmnIdAndTag_returnsOnlyMatchingTag() {
        // arrange
        Pkmn p = persistPkmn("pikachu");
        persistImage(p, "https://x/a.png", "shiny-front", Set.of("shiny", "front"), false);
        persistImage(p, "https://x/b.png", "normal-front", Set.of("front"), false);

        // act
        List<PkmnImage> shiny = pkmnImageRepository.findByPkmnIdAndTag(p.getId(), "shiny");

        // assert
        assertThat(shiny).hasSize(1);
        assertThat(shiny.get(0).getUrl()).isEqualTo("https://x/a.png");
    }

    @Test
    void findByPkmn_IdAndName_returnsMatch() {
        // arrange
        Pkmn p = persistPkmn("pikachu");
        persistImage(p, "https://x/a.png", "front", Set.of(), false);
        persistImage(p, "https://x/b.png", "back", Set.of(), false);

        // act
        Optional<PkmnImage> result = pkmnImageRepository.findByPkmn_IdAndName(p.getId(), "back");

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo("https://x/b.png");
    }

    @Test
    void findByPkmn_IdAndMainTrue_returnsMainImage() {
        // arrange
        Pkmn p = persistPkmn("pikachu");
        persistImage(p, "https://x/a.png", "front", Set.of(), false);
        PkmnImage mainImg = persistImage(p, "https://x/main.png", "official", Set.of(), true);

        // act
        Optional<PkmnImage> result = pkmnImageRepository.findByPkmn_IdAndMainTrue(p.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(mainImg.getId());
    }

    @Test
    void clearMainForPkmn_resetsMainFlag() {
        // arrange
        Pkmn p = persistPkmn("pikachu");
        PkmnImage mainImg = persistImage(p, "https://x/main.png", "official", Set.of(), true);
        em.flush();

        // act
        pkmnImageRepository.clearMainForPkmn(p.getId());
        em.clear();

        // assert
        PkmnImage reloaded = pkmnImageRepository.findById(mainImg.getId()).orElseThrow();
        assertThat(reloaded.isMain()).isFalse();
    }

    @Test
    void existsByPkmn_IdAndId_truthTable() {
        // arrange
        Pkmn p = persistPkmn("pikachu");
        Pkmn other = persistPkmn("raichu");
        PkmnImage img = persistImage(p, "https://x/a.png", "front", Set.of(), false);

        // assert
        assertThat(pkmnImageRepository.existsByPkmn_IdAndId(p.getId(), img.getId())).isTrue();
        assertThat(pkmnImageRepository.existsByPkmn_IdAndId(other.getId(), img.getId())).isFalse();
    }

    @Test
    void deletingPkmn_cascadesToImages() {
        // arrange: this is the test for the cascade fix on Pkmn — without it, the delete
        // would throw a FK ConstraintViolationException.
        Pkmn p = persistPkmn("pikachu");
        PkmnImage img1 = persistImage(p, "https://x/a.png", "front", Set.of(), false);
        PkmnImage img2 = persistImage(p, "https://x/b.png", "back", Set.of(), true);
        em.flush();
        em.clear();

        // act
        pkmnRepository.deleteById(p.getId());
        em.flush();
        em.clear();

        // assert
        assertThat(pkmnImageRepository.findById(img1.getId())).isEmpty();
        assertThat(pkmnImageRepository.findById(img2.getId())).isEmpty();
        assertThat(pkmnRepository.findById(p.getId())).isEmpty();
    }
}
