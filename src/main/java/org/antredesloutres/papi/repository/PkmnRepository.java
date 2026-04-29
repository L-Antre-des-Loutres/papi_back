package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Pkmn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PkmnRepository extends JpaRepository<Pkmn, Integer> {

    @Override
    @EntityGraph(attributePaths = {"primaryType", "secondaryType", "primaryAbility", "secondaryAbility", "hiddenAbility"})
    List<Pkmn> findAll();

    @Override
    @EntityGraph(attributePaths = {"primaryType", "secondaryType", "primaryAbility", "secondaryAbility", "hiddenAbility"})
    Page<Pkmn> findAll(Pageable pageable);

}
