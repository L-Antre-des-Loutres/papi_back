package org.antredesloutres.papi.service.translation;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.antredesloutres.papi.repository.TypeRepository;
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
class TypeTranslationServiceTest {

    @Mock
    TypeRepository typeRepository;

    @InjectMocks
    TypeTranslationService service;

    @Test
    void getTypeTranslations_returnsList() {
        // arrange
        Type type = TestFixtures.type(1, "fire");
        type.getLang().clear();
        type.getLang().add(new TypeTranslation(Language.FR, "Feu"));
        when(typeRepository.findById(1)).thenReturn(Optional.of(type));

        // act
        List<TypeTranslation> result = service.getTypeTranslations(1);

        // assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getTypeTranslations_throwsWhenTypeMissing() {
        // arrange
        when(typeRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getTypeTranslations(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void saveTypeTranslation_updatesExisting() {
        // arrange
        Type type = TestFixtures.type(1, "fire");
        TypeTranslation existing = new TypeTranslation(Language.FR, "Old");
        type.getLang().clear();
        type.getLang().add(existing);
        when(typeRepository.findById(1)).thenReturn(Optional.of(type));
        when(typeRepository.save(type)).thenReturn(type);

        // act
        TypeTranslation result = service.saveTypeTranslation(1, Language.FR, "Feu");

        // assert
        assertThat(result.getName()).isEqualTo("Feu");
        assertThat(type.getLang()).hasSize(1);
    }

    @Test
    void saveTypeTranslation_createsNewWhenAbsent() {
        // arrange
        Type type = TestFixtures.type(1, "fire");
        type.getLang().clear();
        when(typeRepository.findById(1)).thenReturn(Optional.of(type));
        when(typeRepository.save(type)).thenReturn(type);

        // act
        TypeTranslation result = service.saveTypeTranslation(1, Language.EN, "Fire");

        // assert
        assertThat(result.getLanguage()).isEqualTo(Language.EN);
        verify(typeRepository).save(type);
    }

    @Test
    void saveTypeTranslation_throwsWhenTypeMissing() {
        // arrange
        when(typeRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.saveTypeTranslation(99, Language.EN, "x"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
