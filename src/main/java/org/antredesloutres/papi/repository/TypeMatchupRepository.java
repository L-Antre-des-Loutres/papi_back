package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TypeMatchupRepository extends JpaRepository<TypeMatchup, Long> {

    Optional<TypeMatchup> findByAttackingTypeIdAndDefendingTypeId(Integer attackingTypeId, Integer defendingTypeId);

    @Query("SELECT m FROM TypeMatchup m JOIN FETCH m.attackingType JOIN FETCH m.defendingType")
    List<TypeMatchup> findAllWithTypes();
    
    @Modifying
    @Query(value = """
            INSERT INTO type_matchup (attacking_type_id, defending_type_id, effectiveness)
            SELECT :newTypeId, t.id, 'EFFECTIVE' FROM types t WHERE t.id <> :newTypeId
            """, nativeQuery = true)
    void seedAttackingMatchups(@Param("newTypeId") Integer newTypeId);

    @Modifying
    @Query(value = """
            INSERT INTO type_matchup (attacking_type_id, defending_type_id, effectiveness)
            SELECT t.id, :newTypeId, 'EFFECTIVE' FROM types t WHERE t.id <> :newTypeId
            """, nativeQuery = true)
    void seedDefendingMatchups(@Param("newTypeId") Integer newTypeId);

}