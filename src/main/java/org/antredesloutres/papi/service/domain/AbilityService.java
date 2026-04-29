package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbilityService {

    private final AbilityRepository repository;

    @Transactional(readOnly = true)
    public Page<Ability> getAllAbilities(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Ability getAbilityById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ability", id));
    }

    @Transactional
    public Ability createDefaultAbility(String symbol) {
        return repository.save(new Ability(symbol));
    }

    @Transactional
    public Ability updateSymbol(Integer id, String symbol) {
        Ability ability = getAbilityById(id);
        ability.setSymbol(symbol);
        return repository.save(ability);
    }

    @Transactional
    public void deleteAbility(Integer id) {
        Ability ability = getAbilityById(id);
        repository.delete(ability);
    }

    @Transactional(readOnly = true)
    public int getTotalCount() {
        return (int) repository.count();
    }
}
