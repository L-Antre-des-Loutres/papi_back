package org.antredesloutres.papi.service.translation;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AbilityTranslationService {

    private final AbilityRepository abilityRepository;

    @Transactional(readOnly = true)
    public List<AbilityTranslation> getAbilityTranslations(Integer abilityId) {
        return abilityRepository.findById(abilityId)
                .map(Ability::getLang)
                .orElseThrow(() -> new EntityNotFoundException("Ability", abilityId))
                .stream().toList();
    }

    @Transactional
    public AbilityTranslation saveAbilityTranslation(Integer id, Language language, String name, String description) {
        Ability ability = abilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ability", id));

        AbilityTranslation translation = ability.getLang().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> {
                    AbilityTranslation t = new AbilityTranslation(language, name, description);
                    ability.getLang().add(t);
                    return t;
                });

        translation.setName(name);
        translation.setDescription(description);
        abilityRepository.save(ability);
        return translation;
    }
}
