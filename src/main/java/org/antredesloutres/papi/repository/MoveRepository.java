package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, Integer> {
    Optional<Move> findBySymbol(String symbol);

}