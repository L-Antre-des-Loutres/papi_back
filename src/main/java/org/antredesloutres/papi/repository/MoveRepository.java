package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<Move, Integer> {

}