package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.dto.domain.PkmnUpdateDto;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.EggGroup;
import org.antredesloutres.papi.model.enumerated.ExperienceGroup;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.antredesloutres.papi.repository.PkmnRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PkmnServiceTest {

    @Mock
    PkmnRepository pkmnRepository;
    @Mock
    TypeRepository typeRepository;
    @Mock
    AbilityRepository abilityRepository;

    @InjectMocks
    PkmnService service;

    @Test
    void getAllPkmn_returnsPage() {
        // arrange
        Page<Pkmn> page = new PageImpl<>(List.of(TestFixtures.pkmn(1, "pikachu")));
        when(pkmnRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // act
        Page<Pkmn> result = service.getAllPkmn(PageRequest.of(0, 10));

        // assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPkmnById_returnsPkmn() {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(p));

        // act
        Pkmn result = service.getPkmnById(25);

        // assert
        assertThat(result).isSameAs(p);
    }

    @Test
    void getPkmnById_throwsWhenMissing() {
        // arrange
        when(pkmnRepository.findById(0)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getPkmnById(0))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createDefaultPkmn_savesWithSymbol() {
        // arrange
        when(pkmnRepository.save(any(Pkmn.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        Pkmn result = service.createDefaultPkmn("new-mon");

        // assert
        assertThat(result.getSymbol()).isEqualTo("new-mon");
    }

    @Test
    void updateSymbol_changesSymbol() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "old");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updateSymbol(1, "new");

        // assert
        assertThat(result.getSymbol()).isEqualTo("new");
    }

    @Test
    void updateNationalDexNumber_changesValue() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updateNationalDexNumber(1, 25);

        // assert
        assertThat(result.getNationalDexNumber()).isEqualTo(25);
    }

    @Test
    void updatePrimaryType_setsType() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        Type type = TestFixtures.type(13, "electric");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(typeRepository.findById(13)).thenReturn(Optional.of(type));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updatePrimaryType(1, 13);

        // assert
        assertThat(result.getPrimaryType()).isSameAs(type);
    }

    @Test
    void updatePrimaryType_setsNullWhenTypeIdNull() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        p.setPrimaryType(TestFixtures.type(13, "electric"));
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updatePrimaryType(1, null);

        // assert
        assertThat(result.getPrimaryType()).isNull();
    }

    @Test
    void updatePrimaryType_throwsWhenTypeMissing() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(typeRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.updatePrimaryType(1, 99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Type");
    }

    @Test
    void updateSecondaryType_setsType() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        Type type = TestFixtures.type(2, "flying");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(typeRepository.findById(2)).thenReturn(Optional.of(type));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updateSecondaryType(1, 2);

        // assert
        assertThat(result.getSecondaryType()).isSameAs(type);
    }

    @Test
    void updateTags_replacesTags() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);

        // act
        Pkmn result = service.updateTags(1, Set.of("legendary", "rare"));

        // assert
        assertThat(result.getTags()).containsExactlyInAnyOrder("legendary", "rare");
    }

    @Test
    void deletePkmn_deletesExisting() {
        // arrange
        when(pkmnRepository.existsById(1)).thenReturn(true);

        // act
        service.deletePkmn(1);

        // assert
        verify(pkmnRepository).deleteById(1);
    }

    @Test
    void deletePkmn_throwsWhenMissing() {
        // arrange
        when(pkmnRepository.existsById(404)).thenReturn(false);

        // act + assert
        assertThatThrownBy(() -> service.deletePkmn(404))
                .isInstanceOf(EntityNotFoundException.class);
        verify(pkmnRepository, never()).deleteById(any());
    }

    @Test
    void updateAll_appliesOnlyNonNullFields() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "old-symbol");
        p.setBaseHp(50);
        p.setBaseAttack(50);
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);

        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setSymbol("new-symbol");
        dto.setBaseHp(100);
        // baseAttack left null on purpose

        // act
        Pkmn result = service.updateAll(1, dto);

        // assert
        assertThat(result.getSymbol()).isEqualTo("new-symbol");
        assertThat(result.getBaseHp()).isEqualTo(100);
        assertThat(result.getBaseAttack()).isEqualTo(50);
    }

    @Test
    void updateAll_appliesAllFieldsWhenProvided() {
        // arrange
        Pkmn p = TestFixtures.pkmn(1, "x");
        Type primary = TestFixtures.type(1, "fire");
        Ability primaryAbility = TestFixtures.ability(1, "blaze");
        when(pkmnRepository.findById(1)).thenReturn(Optional.of(p));
        when(pkmnRepository.save(p)).thenReturn(p);
        when(typeRepository.getReferenceById(1)).thenReturn(primary);
        when(abilityRepository.getReferenceById(1)).thenReturn(primaryAbility);

        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setSymbol("charizard");
        dto.setHeight(170);
        dto.setWeight(905);
        dto.setBaseHp(78);
        dto.setBaseAttack(84);
        dto.setBaseDefense(78);
        dto.setBaseSpeAttack(109);
        dto.setBaseSpeDefense(85);
        dto.setBaseSpeed(100);
        dto.setEvHp(0);
        dto.setEvAttack(0);
        dto.setEvDefense(0);
        dto.setEvSpeAttack(3);
        dto.setEvSpeDefense(0);
        dto.setEvSpeed(0);
        dto.setExperienceYield(240);
        dto.setExperienceGroup(ExperienceGroup.MEDIUM_SLOW);
        dto.setBaseFriendship(70);
        dto.setEggGroups(Set.of(EggGroup.MONSTER, EggGroup.DRAGON));
        dto.setEggCycles(20);
        dto.setCatchRate(45);
        dto.setMaleRatio(87);
        dto.setTags(Set.of("fire-starter"));
        dto.setPrimaryTypeId(1);
        dto.setPrimaryAbilityId(1);

        // act
        Pkmn result = service.updateAll(1, dto);

        // assert
        assertThat(result.getSymbol()).isEqualTo("charizard");
        assertThat(result.getBaseSpeAttack()).isEqualTo(109);
        assertThat(result.getEvSpeAttack()).isEqualTo(3);
        assertThat(result.getEggGroups()).containsExactlyInAnyOrder(EggGroup.MONSTER, EggGroup.DRAGON);
        assertThat(result.getPrimaryType()).isSameAs(primary);
        assertThat(result.getPrimaryAbility()).isSameAs(primaryAbility);
        assertThat(result.getSecondaryType()).isNull();
        assertThat(result.getHiddenAbility()).isNull();
    }

    @Test
    void updateAll_throwsWhenPkmnMissing() {
        // arrange
        when(pkmnRepository.findById(404)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.updateAll(404, new PkmnUpdateDto()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTotalCount_returnsCountAsInt() {
        // arrange
        when(pkmnRepository.count()).thenReturn(151L);

        // act
        int result = service.getTotalCount();

        // assert
        assertThat(result).isEqualTo(151);
    }
}
