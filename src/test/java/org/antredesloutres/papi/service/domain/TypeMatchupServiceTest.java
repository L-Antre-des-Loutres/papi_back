package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.dto.domain.TypeMatchupDto;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.antredesloutres.papi.repository.TypeMatchupRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeMatchupServiceTest {

    @Mock
    TypeMatchupRepository matchupRepository;
    @Mock
    TypeRepository typeRepository;

    @InjectMocks
    TypeMatchupService service;

    @Test
    void findAll_delegatesToRepositoryWithJoinFetch() {
        // arrange
        TypeMatchup m = TestFixtures.matchup(1L,
                TestFixtures.type(1, "fire"), TestFixtures.type(2, "water"),
                Effectiveness.NOT_VERY_EFFECTIVE);
        when(matchupRepository.findAllWithTypes()).thenReturn(List.of(m));

        // act
        List<TypeMatchup> result = service.findAll();

        // assert
        assertThat(result).hasSize(1);
    }

    @Test
    void upsert_updatesExistingMatchup() {
        // arrange
        TypeMatchup existing = TestFixtures.matchup(5L,
                TestFixtures.type(1, "fire"), TestFixtures.type(2, "water"),
                Effectiveness.EFFECTIVE);
        when(matchupRepository.findByAttackingTypeIdAndDefendingTypeId(1, 2))
                .thenReturn(Optional.of(existing));
        when(matchupRepository.save(existing)).thenReturn(existing);

        // act
        TypeMatchup result = service.upsert(new TypeMatchupDto(1, 2, Effectiveness.NOT_VERY_EFFECTIVE));

        // assert
        assertThat(result.getEffectiveness()).isEqualTo(Effectiveness.NOT_VERY_EFFECTIVE);
        verify(typeRepository, never()).getReferenceById(any());
    }

    @Test
    void upsert_createsNewMatchupWhenAbsent() {
        // arrange
        Type attacker = TestFixtures.type(1, "fire");
        Type defender = TestFixtures.type(2, "water");
        when(matchupRepository.findByAttackingTypeIdAndDefendingTypeId(1, 2))
                .thenReturn(Optional.empty());
        when(typeRepository.getReferenceById(1)).thenReturn(attacker);
        when(typeRepository.getReferenceById(2)).thenReturn(defender);
        when(matchupRepository.save(any(TypeMatchup.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        TypeMatchup result = service.upsert(new TypeMatchupDto(1, 2, Effectiveness.SUPER_EFFECTIVE));

        // assert
        ArgumentCaptor<TypeMatchup> captor = ArgumentCaptor.forClass(TypeMatchup.class);
        verify(matchupRepository).save(captor.capture());
        TypeMatchup saved = captor.getValue();
        assertThat(saved.getAttackingType()).isSameAs(attacker);
        assertThat(saved.getDefendingType()).isSameAs(defender);
        assertThat(saved.getEffectiveness()).isEqualTo(Effectiveness.SUPER_EFFECTIVE);
        assertThat(result.getEffectiveness()).isEqualTo(Effectiveness.SUPER_EFFECTIVE);
    }

    @Test
    void reset_setsEffectivenessToEffectiveWhenPresent() {
        // arrange
        TypeMatchup existing = TestFixtures.matchup(1L,
                TestFixtures.type(1, "fire"), TestFixtures.type(2, "water"),
                Effectiveness.SUPER_EFFECTIVE);
        when(matchupRepository.findByAttackingTypeIdAndDefendingTypeId(1, 2))
                .thenReturn(Optional.of(existing));

        // act
        service.reset(1, 2);

        // assert
        assertThat(existing.getEffectiveness()).isEqualTo(Effectiveness.EFFECTIVE);
        verify(matchupRepository).save(existing);
    }

    @Test
    void reset_doesNothingWhenAbsent() {
        // arrange
        when(matchupRepository.findByAttackingTypeIdAndDefendingTypeId(1, 2))
                .thenReturn(Optional.empty());

        // act
        service.reset(1, 2);

        // assert
        verify(matchupRepository, never()).save(any());
    }
}
