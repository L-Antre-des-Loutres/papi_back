package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext
class PkmnRepositoryTest {

    @Autowired
    PkmnRepository pkmnRepository;
    @Autowired
    TypeRepository typeRepository;
    @Autowired
    AbilityRepository abilityRepository;

    @Test
    void findAll_pageable_returnsPageWithEagerlyLoadedRelations() {
        // arrange
        Type electric = typeRepository.save(new Type("electric", "#f8d030", "Electric"));
        Ability staticAb = abilityRepository.save(new Ability("static"));
        Pkmn pikachu = new Pkmn();
        pikachu.setSymbol("pikachu");
        pikachu.setPrimaryType(electric);
        pikachu.setPrimaryAbility(staticAb);
        pkmnRepository.save(pikachu);

        // act
        Page<Pkmn> page = pkmnRepository.findAll(PageRequest.of(0, 10, Sort.by("id")));

        // assert
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPrimaryType().getSymbol()).isEqualTo("electric");
        assertThat(page.getContent().get(0).getPrimaryAbility().getSymbol()).isEqualTo("static");
    }

    @Test
    void findAll_list_returnsAllWithRelations() {
        // arrange
        Type electric = typeRepository.save(new Type("electric", "#f8d030", "Electric"));
        Pkmn pikachu = new Pkmn();
        pikachu.setSymbol("pikachu");
        pikachu.setPrimaryType(electric);
        pkmnRepository.save(pikachu);

        // act
        List<Pkmn> result = pkmnRepository.findAll();

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrimaryType()).isNotNull();
    }

    @Test
    void findByTag_returnsOnlyMatchingPkmnCaseInsensitively() {
        // arrange
        Type electric = typeRepository.save(new Type("electric", "#f8d030", "Electric"));
        Pkmn pikachu = new Pkmn();
        pikachu.setSymbol("pikachu");
        pikachu.setPrimaryType(electric);
        pikachu.setTags(java.util.Set.of("1g", "legendaire"));
        pkmnRepository.save(pikachu);

        Pkmn chikorita = new Pkmn();
        chikorita.setSymbol("chikorita");
        chikorita.setTags(java.util.Set.of("2g"));
        pkmnRepository.save(chikorita);

        // act: query with different casing than stored
        Page<Pkmn> page = pkmnRepository.findByTag("1G", PageRequest.of(0, 10, Sort.by("id")));

        // assert
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSymbol()).isEqualTo("pikachu");
        assertThat(page.getContent().get(0).getPrimaryType().getSymbol()).isEqualTo("electric");
    }

    @Test
    void findByTag_returnsEmptyPageForUnknownTag() {
        // arrange
        Pkmn pikachu = new Pkmn();
        pikachu.setSymbol("pikachu");
        pikachu.setTags(java.util.Set.of("1g"));
        pkmnRepository.save(pikachu);

        // act
        Page<Pkmn> page = pkmnRepository.findByTag("does-not-exist", PageRequest.of(0, 10, Sort.by("id")));

        // assert
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void findByIdForUpdate_returnsEntityWhenPresent() {
        // arrange
        Pkmn pikachu = new Pkmn();
        pikachu.setSymbol("pikachu");
        Pkmn saved = pkmnRepository.save(pikachu);

        // act: smoke test — verifies the locked query compiles and returns data.
        // True concurrency behavior (SELECT ... FOR UPDATE) is enforced by the DB
        // and tested manually with two transactions if needed.
        Optional<Pkmn> result = pkmnRepository.findByIdForUpdate(saved.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("pikachu");
    }

    @Test
    void findByIdForUpdate_returnsEmptyWhenMissing() {
        // act
        Optional<Pkmn> result = pkmnRepository.findByIdForUpdate(99999);

        // assert
        assertThat(result).isEmpty();
    }
}
