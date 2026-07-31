package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Ability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AbilityRepository extends JpaRepository<Ability, Integer> {
    Optional<Ability> findBySymbol(String symbol);

}