package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.dto.request.PkmnImageRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.antredesloutres.papi.repository.PkmnImageRepository;
import org.antredesloutres.papi.repository.PkmnRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PkmnImageServiceTest {

    @Mock
    PkmnImageRepository pkmnImageRepository;
    @Mock
    PkmnRepository pkmnRepository;

    @InjectMocks
    PkmnImageService service;

    private static PkmnImage image(Long id, Pkmn pkmn, String url, boolean main) {
        PkmnImage img = new PkmnImage();
        img.setId(id);
        img.setPkmn(pkmn);
        img.setUrl(url);
        img.setMain(main);
        return img;
    }

    @Test
    void getImages_returnsListWhenPkmnExists() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage img = image(1L, pkmn, "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findByPkmn_Id(25)).thenReturn(List.of(img));

        // act
        List<PkmnImage> result = service.getImages(25);

        // assert
        assertThat(result).containsExactly(img);
    }

    @Test
    void getImages_throwsWhenPkmnMissing() {
        // arrange
        when(pkmnRepository.existsById(99)).thenReturn(false);

        // act + assert
        assertThatThrownBy(() -> service.getImages(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pokemon");
        verify(pkmnImageRepository, never()).findByPkmn_Id(any());
    }

    @Test
    void getImagesByTag_delegatesToRepository() {
        // arrange
        PkmnImage img = image(1L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findByPkmnIdAndTag(25, "shiny")).thenReturn(List.of(img));

        // act
        List<PkmnImage> result = service.getImagesByTag(25, "shiny");

        // assert
        assertThat(result).containsExactly(img);
    }

    @Test
    void getImageByName_returnsOptional() {
        // arrange
        PkmnImage img = image(1L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findByPkmn_IdAndName(25, "front")).thenReturn(Optional.of(img));

        // act
        Optional<PkmnImage> result = service.getImageByName(25, "front");

        // assert
        assertThat(result).contains(img);
    }

    @Test
    void getMainImage_returnsOptional() {
        // arrange
        PkmnImage img = image(1L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", true);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findByPkmn_IdAndMainTrue(25)).thenReturn(Optional.of(img));

        // act
        Optional<PkmnImage> result = service.getMainImage(25);

        // assert
        assertThat(result).contains(img);
    }

    @Test
    void addImage_populatesEntityAndSaves() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(pkmn));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PkmnImageRequest req = new PkmnImageRequest("https://x/a.png", "front", Set.of("shiny"), false);

        // act
        PkmnImage result = service.addImage(25, req);

        // assert
        assertThat(result.getPkmn()).isSameAs(pkmn);
        assertThat(result.getUrl()).isEqualTo("https://x/a.png");
        assertThat(result.getName()).isEqualTo("front");
        assertThat(result.getTags()).containsExactly("shiny");
        assertThat(result.isMain()).isFalse();
        assertThat(result.getAddedAt()).isNotNull();
        verify(pkmnImageRepository, never()).clearMainForPkmn(any());
    }

    @Test
    void addImage_clearsExistingMainWhenRequestIsMain() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        when(pkmnRepository.findById(25)).thenReturn(Optional.of(pkmn));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PkmnImageRequest req = new PkmnImageRequest("https://x/a.png", null, null, true);

        // act
        PkmnImage result = service.addImage(25, req);

        // assert: clearMain happens before save, so the new row ends up as the only main
        InOrder order = inOrder(pkmnImageRepository);
        order.verify(pkmnImageRepository).clearMainForPkmn(25);
        order.verify(pkmnImageRepository).save(any(PkmnImage.class));
        assertThat(result.isMain()).isTrue();
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void addImage_throwsWhenPkmnMissing() {
        // arrange
        when(pkmnRepository.findById(99)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.addImage(99,
                new PkmnImageRequest("https://x/a.png", null, null, false)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pokemon");
        verify(pkmnImageRepository, never()).save(any());
    }

    @Test
    void updateImage_mutatesFields() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage existing = image(7L, pkmn, "https://x/old.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PkmnImageRequest req = new PkmnImageRequest("https://x/new.png", "back", Set.of("hd"), false);

        // act
        PkmnImage result = service.updateImage(25, 7L, req);

        // assert
        assertThat(result.getUrl()).isEqualTo("https://x/new.png");
        assertThat(result.getName()).isEqualTo("back");
        assertThat(result.getTags()).containsExactly("hd");
        verify(pkmnImageRepository, never()).clearMainForPkmn(any());
    }

    @Test
    void updateImage_clearsOthersWhenSwitchingToMain() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage existing = image(7L, pkmn, "https://x/old.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PkmnImageRequest req = new PkmnImageRequest("https://x/new.png", null, null, true);

        // act
        PkmnImage result = service.updateImage(25, 7L, req);

        // assert
        verify(pkmnImageRepository).clearMainForPkmn(25);
        assertThat(result.isMain()).isTrue();
    }

    @Test
    void updateImage_doesNotClearWhenAlreadyMain() {
        // arrange: image is already main, request keeps main=true → no need to clear others
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage existing = image(7L, pkmn, "https://x/old.png", true);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PkmnImageRequest req = new PkmnImageRequest("https://x/new.png", null, null, true);

        // act
        service.updateImage(25, 7L, req);

        // assert
        verify(pkmnImageRepository, never()).clearMainForPkmn(any());
    }

    @Test
    void updateImage_throwsWhenImageBelongsToOtherPkmn() {
        // arrange
        PkmnImage existing = image(7L, TestFixtures.pkmn(99, "other"), "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));

        // act + assert
        assertThatThrownBy(() -> service.updateImage(25, 7L,
                new PkmnImageRequest("https://x/n.png", null, null, false)))
                .isInstanceOf(EntityNotFoundException.class);
        verify(pkmnImageRepository, never()).save(any());
    }

    @Test
    void updateImage_throwsWhenImageMissing() {
        // arrange
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.updateImage(25, 7L,
                new PkmnImageRequest("https://x/n.png", null, null, false)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteImage_deletesWhenBelongsToPkmn() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage existing = image(7L, pkmn, "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));

        // act
        service.deleteImage(25, 7L);

        // assert
        verify(pkmnImageRepository).deleteById(7L);
    }

    @Test
    void deleteImage_throwsWhenCrossPkmn() {
        // arrange
        PkmnImage existing = image(7L, TestFixtures.pkmn(99, "other"), "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));

        // act + assert
        assertThatThrownBy(() -> service.deleteImage(25, 7L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(pkmnImageRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void promoteToMain_clearsOthersAndSetsMain() {
        // arrange
        Pkmn pkmn = TestFixtures.pkmn(25, "pikachu");
        PkmnImage existing = image(7L, pkmn, "https://x/a.png", false);
        when(pkmnRepository.existsById(25)).thenReturn(true);
        when(pkmnImageRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(pkmnImageRepository.save(any(PkmnImage.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        PkmnImage result = service.promoteToMain(25, 7L);

        // assert: clear must happen before save, otherwise the UPDATE wipes our flag
        InOrder order = inOrder(pkmnImageRepository);
        order.verify(pkmnImageRepository).clearMainForPkmn(25);
        order.verify(pkmnImageRepository).save(existing);
        assertThat(result.isMain()).isTrue();
    }
}
