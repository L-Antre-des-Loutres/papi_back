package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.repository.AbilityRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbilityServiceTest {

    @Mock
    AbilityRepository repository;

    @InjectMocks
    AbilityService service;

    @Test
    void getAllAbilities_returnsPageFromRepository() {
        // arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ability> page = new PageImpl<>(List.of(TestFixtures.ability(1, "static")));
        when(repository.findAll(pageable)).thenReturn(page);

        // act
        Page<Ability> result = service.getAllAbilities(pageable);

        // assert
        assertThat(result.getContent()).hasSize(1).first().extracting(Ability::getSymbol).isEqualTo("static");
    }

    @Test
    void getAbilityById_returnsAbility() {
        // arrange
        Ability ability = TestFixtures.ability(7, "intimidate");
        when(repository.findById(7)).thenReturn(Optional.of(ability));

        // act
        Ability result = service.getAbilityById(7);

        // assert
        assertThat(result).isSameAs(ability);
    }

    @Test
    void getAbilityById_throwsWhenMissing() {
        // arrange
        when(repository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getAbilityById(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ability")
                .hasMessageContaining("99");
    }

    @Test
    void createDefaultAbility_persistsNewAbility() {
        // arrange
        when(repository.save(any(Ability.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        Ability result = service.createDefaultAbility("levitate");

        // assert
        assertThat(result.getSymbol()).isEqualTo("levitate");
        verify(repository).save(any(Ability.class));
    }

    @Test
    void updateSymbol_changesSymbolAndSaves() {
        // arrange
        Ability ability = TestFixtures.ability(1, "old");
        when(repository.findById(1)).thenReturn(Optional.of(ability));
        when(repository.save(ability)).thenReturn(ability);

        // act
        Ability result = service.updateSymbol(1, "new");

        // assert
        assertThat(result.getSymbol()).isEqualTo("new");
        verify(repository).save(ability);
    }

    @Test
    void updateSymbol_throwsWhenAbilityMissing() {
        // arrange
        when(repository.findById(404)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.updateSymbol(404, "x"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteAbility_deletesExistingAbility() {
        // arrange
        Ability ability = TestFixtures.ability(2, "x");
        when(repository.findById(2)).thenReturn(Optional.of(ability));

        // act
        service.deleteAbility(2);

        // assert
        verify(repository).delete(ability);
    }

    @Test
    void deleteAbility_throwsWhenAbilityMissing() {
        // arrange
        when(repository.findById(2)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.deleteAbility(2))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTotalCount_returnsCountAsInt() {
        // arrange
        when(repository.count()).thenReturn(42L);

        // act
        int result = service.getTotalCount();

        // assert
        assertThat(result).isEqualTo(42);
    }
}
