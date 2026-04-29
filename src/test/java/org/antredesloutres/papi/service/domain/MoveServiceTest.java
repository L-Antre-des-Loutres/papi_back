package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.repository.MoveRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveServiceTest {

    @Mock
    MoveRepository moveRepository;
    @Mock
    TypeService typeService;

    @InjectMocks
    MoveService service;

    @Test
    void getAllMoves_returnsPage() {
        // arrange
        Page<Move> page = new PageImpl<>(List.of(TestFixtures.move(1, "tackle", null)));
        when(moveRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // act
        Page<Move> result = service.getAllMoves(PageRequest.of(0, 10));

        // assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getMoveById_returnsMove() {
        // arrange
        Move move = TestFixtures.move(5, "ember", null);
        when(moveRepository.findById(5)).thenReturn(Optional.of(move));

        // act
        Move result = service.getMoveById(5);

        // assert
        assertThat(result.getSymbol()).isEqualTo("ember");
    }

    @Test
    void getMoveById_throwsWhenMissing() {
        // arrange
        when(moveRepository.findById(0)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getMoveById(0))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createDefaultMove_savesNewMove() {
        // arrange
        when(moveRepository.save(any(Move.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        Move result = service.createDefaultMove("scratch", "Scratch", null, 40, 100, 35);

        // assert
        assertThat(result.getSymbol()).isEqualTo("scratch");
        assertThat(result.getPower()).isEqualTo(40);
    }

    @Test
    void updateSymbol_changesAndSaves() {
        // arrange
        Move move = TestFixtures.move(1, "old", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updateSymbol(1, "new");

        // assert
        assertThat(result.getSymbol()).isEqualTo("new");
    }

    @Test
    void updateType_setsType() {
        // arrange
        Type type = TestFixtures.type(2, "fire");
        Move move = TestFixtures.move(1, "x", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(typeService.getTypeById(2)).thenReturn(type);
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updateType(1, 2);

        // assert
        assertThat(result.getType()).isSameAs(type);
    }

    @Test
    void updateType_setsNullWhenTypeIdNull() {
        // arrange
        Move move = TestFixtures.move(1, "x", TestFixtures.type(9, "old"));
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updateType(1, null);

        // assert
        assertThat(result.getType()).isNull();
    }

    @Test
    void updatePower_changesPower() {
        // arrange
        Move move = TestFixtures.move(1, "x", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updatePower(1, 120);

        // assert
        assertThat(result.getPower()).isEqualTo(120);
    }

    @Test
    void updateAccuracy_changesAccuracy() {
        // arrange
        Move move = TestFixtures.move(1, "x", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updateAccuracy(1, 85);

        // assert
        assertThat(result.getAccuracy()).isEqualTo(85);
    }

    @Test
    void updatePp_changesPp() {
        // arrange
        Move move = TestFixtures.move(1, "x", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));
        when(moveRepository.save(move)).thenReturn(move);

        // act
        Move result = service.updatePp(1, 5);

        // assert
        assertThat(result.getPp()).isEqualTo(5);
    }

    @Test
    void deleteMove_deletesExisting() {
        // arrange
        Move move = TestFixtures.move(1, "x", null);
        when(moveRepository.findById(1)).thenReturn(Optional.of(move));

        // act
        service.deleteMove(1);

        // assert
        verify(moveRepository).delete(move);
    }

    @Test
    void deleteMove_throwsWhenMissing() {
        // arrange
        when(moveRepository.findById(1)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.deleteMove(1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTotalCount_returnsCountAsInt() {
        // arrange
        when(moveRepository.count()).thenReturn(150L);

        // act
        int result = service.getTotalCount();

        // assert
        assertThat(result).isEqualTo(150);
    }
}
