package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.dto.domain.MovesetRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;
import org.antredesloutres.papi.repository.MoveRepository;
import org.antredesloutres.papi.repository.MovesetRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovesetServiceTest {

    @Mock
    MovesetRepository movesetRepository;
    @Mock
    MoveRepository moveRepository;
    @Mock
    PkmnService pkmnService;

    @InjectMocks
    MovesetService service;

    @Test
    void getAllMovesets_returnsPage() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        Move m = TestFixtures.move(1, "tackle", null);
        Page<Moveset> page = new PageImpl<>(List.of(TestFixtures.moveset(1, p, m)));
        when(movesetRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        // act
        Page<Moveset> result = service.getAllMovesets(PageRequest.of(0, 5));

        // assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getMovesetById_returnsMoveset() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        Move m = TestFixtures.move(1, "tackle", null);
        Moveset entry = TestFixtures.moveset(7, p, m);
        when(movesetRepository.findById(7)).thenReturn(Optional.of(entry));

        // act
        Moveset result = service.getMovesetById(7);

        // assert
        assertThat(result).isSameAs(entry);
    }

    @Test
    void getMovesetById_throwsWhenMissing() {
        // arrange
        when(movesetRepository.findById(0)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getMovesetById(0))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPkmnMovesetByPkmnId_delegatesToRepository() {
        // arrange
        Moveset entry = TestFixtures.moveset(1, TestFixtures.pkmn(1, "x"), TestFixtures.move(1, "y", null));
        when(movesetRepository.findByPkmnId(1)).thenReturn(List.of(entry));

        // act
        List<Moveset> result = service.getPkmnMovesetByPkmnId(1);

        // assert
        assertThat(result).hasSize(1).first().isSameAs(entry);
    }

    @Test
    void addMove_createsAndSavesEntry() {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        Move m = TestFixtures.move(85, "thunderbolt", null);
        when(pkmnService.getPkmnById(25)).thenReturn(p);
        when(moveRepository.findById(85)).thenReturn(Optional.of(m));
        when(movesetRepository.save(any(Moveset.class))).thenAnswer(inv -> inv.getArgument(0));

        MovesetRequest req = new MovesetRequest(85, MoveLearnMethod.LEVEL_UP, 26);

        // act
        Moveset result = service.addMove(25, req);

        // assert
        assertThat(result.getPkmn()).isSameAs(p);
        assertThat(result.getMove()).isSameAs(m);
        assertThat(result.getLearnMethod()).isEqualTo(MoveLearnMethod.LEVEL_UP);
        assertThat(result.getLearnLevel()).isEqualTo(26);
    }

    @Test
    void addMove_throwsWhenMoveMissing() {
        // arrange
        when(pkmnService.getPkmnById(25)).thenReturn(TestFixtures.pkmn(25, "pikachu"));
        when(moveRepository.findById(404)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.addMove(25, new MovesetRequest(404, MoveLearnMethod.LEVEL_UP, 1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Move");
    }

    @Test
    void deleteMove_deletesWhenBelongsToPkmn() {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        Moveset entry = TestFixtures.moveset(1, p, TestFixtures.move(1, "x", null));
        when(movesetRepository.findById(1)).thenReturn(Optional.of(entry));

        // act
        service.deleteMove(25, 1);

        // assert
        verify(movesetRepository).delete(entry);
    }

    @Test
    void deleteMove_throwsWhenEntryDoesNotBelongToPkmn() {
        // arrange
        Pkmn other = TestFixtures.pkmn(99, "other");
        Moveset entry = TestFixtures.moveset(1, other, TestFixtures.move(1, "x", null));
        when(movesetRepository.findById(1)).thenReturn(Optional.of(entry));

        // act + assert
        assertThatThrownBy(() -> service.deleteMove(25, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to Pokemon");
        verify(movesetRepository, never()).delete(any());
    }
}
