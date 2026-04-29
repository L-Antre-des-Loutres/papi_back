package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Moveset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovesetRepository extends JpaRepository<Moveset, Integer> {

    @Query("SELECT ms FROM Moveset ms JOIN FETCH ms.move m LEFT JOIN FETCH m.type WHERE ms.pkmn.id = :id")
    List<Moveset> findByPkmnId(@Param("id") Integer id);

}