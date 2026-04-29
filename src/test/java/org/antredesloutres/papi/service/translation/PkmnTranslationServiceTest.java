package org.antredesloutres.papi.service.translation;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.repository.PkmnRepository;
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
class PkmnTranslationServiceTest {

    @Mock
    PkmnRepository pkmnRepository;

    @InjectMocks
    PkmnTranslationService service;

    @Test
    void getPkmnTranslations_returnsList() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        pkmn.getLang().add(new PkmnTranslation(Language.EN, "Pikachu", "Normal", "Mouse pkmn"));
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(pkmn));

        // act
        List<PkmnTranslation> result = service.getPkmnTranslations(25);

        // assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getPkmnTranslations_throwsWhenPkmnMissing() {
        // arrange
        when(pkmnRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getPkmnTranslations(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void savePkmnTranslation_updatesExisting() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        pkmn.getLang().add(new PkmnTranslation(Language.EN, "Old", "Normal", "Old desc"));
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(pkmn));
        when(pkmnRepository.save(pkmn)).thenReturn(pkmn);

        // act
        PkmnTranslation result = service.savePkmnTranslation(25, Language.EN, "Pikachu", "Normal", "Cute mouse");

        // assert
        assertThat(result.getPkmnName()).isEqualTo("Pikachu");
        assertThat(result.getDescription()).isEqualTo("Cute mouse");
        assertThat(pkmn.getLang()).hasSize(1);
    }

    @Test
    void savePkmnTranslation_createsNewWhenAbsent() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(pkmn));
        when(pkmnRepository.save(pkmn)).thenReturn(pkmn);

        // act
        PkmnTranslation result = service.savePkmnTranslation(25, Language.FR, "Pikachu", "Normal", "Souris");

        // assert
        assertThat(result.getLanguage()).isEqualTo(Language.FR);
        verify(pkmnRepository).save(pkmn);
    }

    @Test
    void savePkmnTranslation_throwsWhenPkmnMissing() {
        // arrange
        when(pkmnRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.savePkmnTranslation(99, Language.EN, "x", "y", "z"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
