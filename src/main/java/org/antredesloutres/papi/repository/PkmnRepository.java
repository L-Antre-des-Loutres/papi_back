package org.antredesloutres.papi.repository;

import jakarta.persistence.LockModeType;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PkmnRepository extends JpaRepository<Pkmn, Integer> {
    Optional<Pkmn> findBySymbol(String symbol);

    @Override
    @EntityGraph(attributePaths = {"primaryType", "secondaryType", "primaryAbility", "secondaryAbility", "hiddenAbility"})
    List<Pkmn> findAll();

    @Override
    @EntityGraph(attributePaths = {"primaryType", "secondaryType", "primaryAbility", "secondaryAbility", "hiddenAbility"})
    Page<Pkmn> findAll(Pageable pageable);

    /**
     * Lists Pokémon carrying the given tag, matched exactly but case-insensitively.
     * Requires {@code Pkmn.tags} to be an {@code @ElementCollection} so the tags
     * live in a joinable {@code pokemon_tags} table.
     */
    @EntityGraph(attributePaths = {"primaryType", "secondaryType"})
    @Query("SELECT DISTINCT p FROM Pkmn p JOIN p.tags t WHERE LOWER(t) = LOWER(:tag)")
    Page<Pkmn> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * Loads a Pkmn while acquiring a row-level write lock (SELECT ... FOR UPDATE).
     * Use only inside a transaction that mutates the Pkmn's children where an
     * invariant must hold (e.g. "at most one PkmnImage per Pkmn has main=true").
     * Concurrent transactions calling this method on the same id will queue.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pkmn p WHERE p.id = :id")
    Optional<Pkmn> findByIdForUpdate(@Param("id") Integer id);

}
