package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.repository.TypeMatchupRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeServiceTest {

    @Mock
    TypeRepository repository;
    @Mock
    TypeMatchupRepository matchupRepository;

    @InjectMocks
    TypeService service;

    @Test
    void getAllTypes_returnsPage() {
        // arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<Type> page = new PageImpl<>(List.of(TestFixtures.type(1, "fire")));
        when(repository.findAll(pageable)).thenReturn(page);

        // act
        Page<Type> result = service.getAllTypes(pageable);

        // assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getTypeById_returnsType() {
        // arrange
        Type type = TestFixtures.type(3, "water");
        when(repository.findById(3)).thenReturn(Optional.of(type));

        // act
        Type result = service.getTypeById(3);

        // assert
        assertThat(result.getSymbol()).isEqualTo("water");
    }

    @Test
    void getTypeById_throwsWhenMissing() {
        // arrange
        when(repository.findById(404)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getTypeById(404))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createDefaultType_savesAndSeedsMatchups() {
        // arrange
        Type saved = TestFixtures.type(10, "fairy");
        when(repository.saveAndFlush(any(Type.class))).thenReturn(saved);

        // act
        Type result = service.createDefaultType("fairy", "#ee99ac", "Fairy");

        // assert
        assertThat(result).isSameAs(saved);
        verify(matchupRepository).seedAttackingMatchups(10);
        verify(matchupRepository).seedDefendingMatchups(10);
    }

    @Test
    void addTag_addsTagAndSaves() {
        // arrange
        Type type = TestFixtures.type(1, "x");
        type.setTags(new HashSet<>(Set.of("a")));
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(type)).thenReturn(type);

        // act
        Type result = service.addTag(1, "b");

        // assert
        assertThat(result.getTags()).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void addTag_initializesTagsWhenNull() {
        // arrange
        Type type = TestFixtures.type(1, "x");
        type.setTags(null);
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(type)).thenReturn(type);

        // act
        Type result = service.addTag(1, "first");

        // assert
        assertThat(result.getTags()).containsExactly("first");
    }

    @Test
    void removeTag_removesTag() {
        // arrange
        Type type = TestFixtures.type(1, "x");
        type.setTags(new HashSet<>(Set.of("a", "b")));
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(type)).thenReturn(type);

        // act
        Type result = service.removeTag(1, "a");

        // assert
        assertThat(result.getTags()).containsExactly("b");
    }

    @Test
    void updateSymbol_changesSymbol() {
        // arrange
        Type type = TestFixtures.type(1, "old");
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(type)).thenReturn(type);

        // act
        Type result = service.updateSymbol(1, "new");

        // assert
        assertThat(result.getSymbol()).isEqualTo("new");
    }

    @Test
    void updateColor_changesColor() {
        // arrange
        Type type = TestFixtures.type(1, "x");
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(type)).thenReturn(type);

        // act
        Type result = service.updateColor(1, "#000000");

        // assert
        assertThat(result.getColor()).isEqualTo("#000000");
    }

    @Test
    void deleteType_deletesExistingType() {
        // arrange
        Type type = TestFixtures.type(1, "x");
        when(repository.findById(1)).thenReturn(Optional.of(type));

        // act
        service.deleteType(1);

        // assert
        verify(repository).delete(type);
    }

    @Test
    void deleteType_throwsWhenMissing() {
        // arrange
        when(repository.findById(1)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.deleteType(1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTotalCount_returnsCountAsInt() {
        // arrange
        when(repository.count()).thenReturn(18L);

        // act
        int result = service.getTotalCount();

        // assert
        assertThat(result).isEqualTo(18);
    }
}
