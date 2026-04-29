package org.antredesloutres.papi.service.translation;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.MoveTranslation;
import org.antredesloutres.papi.repository.MoveRepository;
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
class MoveTranslationServiceTest {

    @Mock
    MoveRepository moveRepository;

    @InjectMocks
    MoveTranslationService service;

    @Test
    void getMoveTranslations_returnsListFromMove() {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        move.getLang().clear();
        move.getLang().add(new MoveTranslation(Language.EN, "Tackle", "Charge"));
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));

        // act
        List<MoveTranslation> result = service.getMoveTranslations(1);

        // assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getMoveTranslations_throwsWhenMoveMissing() {
        // arrange
        when(moveRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getMoveTranslations(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void saveMoveTranslation_updatesExisting() {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        MoveTranslation existing = new MoveTranslation(Language.EN, "Old", "Old desc");
        move.getLang().clear();
        move.getLang().add(existing);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        MoveTranslation result = service.saveMoveTranslation(1, Language.EN, "New", "New desc");

        // assert
        assertThat(result.getName()).isEqualTo("New");
        assertThat(move.getLang()).hasSize(1);
    }

    @Test
    void saveMoveTranslation_createsNewWhenAbsent() {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        move.getLang().clear();
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        MoveTranslation result = service.saveMoveTranslation(1, Language.FR, "Charge", "Charge fr");

        // assert
        assertThat(result.getLanguage()).isEqualTo(Language.FR);
        verify(moveRepository).save(move);
    }

    @Test
    void saveMoveTranslation_throwsWhenMoveMissing() {
        // arrange
        when(moveRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.saveMoveTranslation(99, Language.EN, "x", "y"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
