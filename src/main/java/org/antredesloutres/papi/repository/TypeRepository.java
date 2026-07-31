package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeRepository extends JpaRepository<Type, Integer> {
    Optional<Type> findBySymbol(String symbol);
}
