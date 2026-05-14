package org.antredesloutres.papi.service.image;

import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageGeneratorServiceTest {

    private ImageGeneratorService imageGeneratorService;

    @BeforeEach
    void setUp() {
        imageGeneratorService = new ImageGeneratorService();
    }

    @Test
    void generatePkmnInfoImage_ShouldReturnImage() {
        // Arrange
        Pkmn pkmn = TestFixtures.pkmn(1, "bulbasaur");
        pkmn.setNationalDexNumber(1);
        pkmn.setPrimaryType(new Type("grass", "#78C850", "Grass"));
        pkmn.setPrimaryAbility(new Ability());
        
        PkmnTranslation trans = new PkmnTranslation(Language.FR, "Bulbizarre", "normal", "Description");
        pkmn.setLang(Collections.singleton(trans));

        // Act
        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(pkmn, Language.FR);

        // Assert
        assertNotNull(image);
    }
}
