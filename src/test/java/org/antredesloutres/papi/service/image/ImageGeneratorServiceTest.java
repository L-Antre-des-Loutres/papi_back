package org.antredesloutres.papi.service.image;

import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.image.TemplateDefinition;
import org.antredesloutres.papi.model.image.TemplateElement;
import org.antredesloutres.papi.model.image.TemplateElementType;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageGeneratorServiceTest {

    private ImageGeneratorService imageGeneratorService;

    @BeforeEach
    void setUp() {
        imageGeneratorService = new ImageGeneratorService();
    }

    private static TemplateDefinition summaryTemplate() {
        return new TemplateDefinition("summary", "Pokémon Summary Card", "template/pokemon_summary.png", 1920, 1080, List.of(
                new TemplateElement(TemplateElementType.NAME,        770,  100, 660, 115),
                new TemplateElement(TemplateElementType.TYPES,       770,  840, 660, 115),
                new TemplateElement(TemplateElementType.SPRITE,      850,  215, 500, 625),
                new TemplateElement(TemplateElementType.STATS,       100,  100, 610, 420),
                new TemplateElement(TemplateElementType.DESCRIPTION, 100,  560, 610, 420),
                new TemplateElement(TemplateElementType.ABILITIES,   1400, 100, 500, 520),
                new TemplateElement(TemplateElementType.DEX_NUMBER,  1540, 660, 300, 300)
        ));
    }

    private static Pkmn bulbasaur() {
        Pkmn pkmn = TestFixtures.pkmn(1, "bulbasaur");
        pkmn.setNationalDexNumber(1);
        pkmn.setPrimaryType(new Type("grass", "#78C850", "Grass"));
        pkmn.setPrimaryAbility(new Ability());

        PkmnTranslation trans = new PkmnTranslation(Language.FR, "Bulbizarre", "normal", "Description");
        pkmn.setLang(Collections.singleton(trans));
        return pkmn;
    }

    @Test
    void generatePkmnInfoImage_ShouldReturnImage() {
        // Arrange
        Pkmn pkmn = bulbasaur();

        // Act
        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(
                pkmn, Language.FR, pkmn.getSpriteUrl(), summaryTemplate());

        // Assert
        assertNotNull(image);
    }

    @Test
    void calculateStateHash_ShouldChange_WhenTemplateDefinitionChanges() {
        // Arrange
        Pkmn pkmn = bulbasaur();
        TemplateDefinition original = summaryTemplate();
        TemplateDefinition moved = new TemplateDefinition(
                original.id(), original.name(), original.background(),
                original.referenceWidth(), original.referenceHeight(),
                List.of(new TemplateElement(TemplateElementType.NAME, 0, 0, 660, 115)));

        // Act
        String hashOriginal = imageGeneratorService.calculateStateHash(pkmn, Language.FR, pkmn.getSpriteUrl(), original);
        String hashSame     = imageGeneratorService.calculateStateHash(pkmn, Language.FR, pkmn.getSpriteUrl(), summaryTemplate());
        String hashMoved    = imageGeneratorService.calculateStateHash(pkmn, Language.FR, pkmn.getSpriteUrl(), moved);

        // Assert
        assertEquals(hashOriginal, hashSame);
        assertNotEquals(hashOriginal, hashMoved);
    }
}
