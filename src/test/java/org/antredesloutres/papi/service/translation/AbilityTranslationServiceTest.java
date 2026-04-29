package org.antredesloutres.papi.service.translation;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbilityTranslationServiceTest {

    @Mock
    AbilityRepository abilityRepository;

    @InjectMocks
    AbilityTranslationService service;

    @Test
    void getAbilityTranslations_returnsListFromAbility() {
        // arrange
        Ability ability = TestFixtures.ability(1, "static");
        ability.getLang().clear();
        ability.getLang().add(new AbilityTranslation(Language.EN, "Static", "Paralyzes"));
        ability.getLang().add(new AbilityTranslation(Language.FR, "Statik", "Paralyse"));
        when(abilityRepository.findById(1)).thenReturn(Optional.of(ability));

        // act
        List<AbilityTranslation> result = service.getAbilityTranslations(1);

        // assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getAbilityTranslations_throwsWhenAbilityMissing() {
        // arrange
        when(abilityRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getAbilityTranslations(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void saveAbilityTranslation_updatesExistingTranslation() {
        // arrange
        Ability ability = TestFixtures.ability(1, "static");
        AbilityTranslation existing = new AbilityTranslation(Language.EN, "Old name", "Old desc");
        ability.getLang().clear();
        ability.getLang().add(existing);
        when(abilityRepository.findById(1)).thenReturn(Optional.of(ability));
        when(abilityRepository.save(ability)).thenReturn(ability);

        // act
        AbilityTranslation result = service.saveAbilityTranslation(1, Language.EN, "New name", "New desc");

        // assert
        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(ability.getLang()).hasSize(1);
    }

    @Test
    void saveAbilityTranslation_createsNewTranslationWhenAbsent() {
        // arrange
        Ability ability = TestFixtures.ability(1, "static");
        ability.getLang().clear();
        when(abilityRepository.findById(1)).thenReturn(Optional.of(ability));
        when(abilityRepository.save(ability)).thenReturn(ability);

        // act
        AbilityTranslation result = service.saveAbilityTranslation(1, Language.FR, "Statik", "Paralyse");

        // assert
        assertThat(result.getLanguage()).isEqualTo(Language.FR);
        assertThat(result.getName()).isEqualTo("Statik");
        assertThat(ability.getLang()).hasSize(1);
        verify(abilityRepository).save(ability);
    }

    @Test
    void saveAbilityTranslation_throwsWhenAbilityMissing() {
        // arrange
        when(abilityRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.saveAbilityTranslation(99, Language.EN, "x", "y"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
