package org.antredesloutres.papi.service.image;

import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PreviewGenerator {

    @Test
    void generatePreviews() throws IOException {
        ImageGeneratorService service = new ImageGeneratorService();
        Pkmn pkmn = createMockPkmn();

        // FR
        ImageIO.write(service.generatePkmnInfoImage(pkmn, Language.FR), "PNG", new File("generated-images/pkmn-1-fr.png"));
        // EN
        ImageIO.write(service.generatePkmnInfoImage(pkmn, Language.EN), "PNG", new File("generated-images/pkmn-1-en.png"));
        
        System.out.println("Preview images generated in generated-images/");
    }

    private Pkmn createMockPkmn() {
        Pkmn p = TestFixtures.pkmn(1, "bulbasaur");
        p.setNationalDexNumber(1);
        p.setBaseHp(45);
        p.setBaseAttack(49);
        p.setBaseDefense(49);
        p.setBaseSpeAttack(65);
        p.setBaseSpeDefense(65);
        p.setBaseSpeed(45);
        p.setHeight(70);
        p.setWeight(69);
        p.setExperienceYield(64);
        p.setCatchRate(45);
        p.setSpriteUrl("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png");

        // Type
        Type grass = new Type("grass", "#78C850", "Grass");
        Set<TypeTranslation> grassTrans = new HashSet<>();
        grassTrans.add(new TypeTranslation(Language.FR, "Plante"));
        grassTrans.add(new TypeTranslation(Language.EN, "Grass"));
        grass.setLang(grassTrans);
        p.setPrimaryType(grass);

        Type poison = new Type("poison", "#A040A0", "Poison");
        Set<TypeTranslation> poisonTrans = new HashSet<>();
        poisonTrans.add(new TypeTranslation(Language.FR, "Poison"));
        poisonTrans.add(new TypeTranslation(Language.EN, "Poison"));
        poison.setLang(poisonTrans);
        p.setSecondaryType(poison);

        // Ability
        Ability overgrow = new Ability("overgrow");
        Set<AbilityTranslation> overTrans = new HashSet<>();
        overTrans.add(new AbilityTranslation(Language.FR, "Engrais", "Augmente la puissance des capacités de type Plante quand les PV sont bas."));
        overTrans.add(new AbilityTranslation(Language.EN, "Overgrow", "Powers up Grass-type moves when the Pokémon's HP is low."));
        overgrow.setLang(overTrans);
        p.setPrimaryAbility(overgrow);

        Ability chloro = new Ability("chlorophyll");
        Set<AbilityTranslation> chloroTrans = new HashSet<>();
        chloroTrans.add(new AbilityTranslation(Language.FR, "Chlorophylle", "Double la Vitesse sous un soleil radieux."));
        chloroTrans.add(new AbilityTranslation(Language.EN, "Chlorophyll", "Boosts the Pokémon's Speed stat in harsh sunlight."));
        chloro.setLang(chloroTrans);
        p.setHiddenAbility(chloro);

        // Pkmn Translations
        Set<PkmnTranslation> pkTrans = new HashSet<>();
        pkTrans.add(new PkmnTranslation(Language.FR, "Bulbizarre", "normal", "Un étrange Pokémon qui est à la fois plante et animal."));
        pkTrans.add(new PkmnTranslation(Language.EN, "Bulbasaur", "normal", "A strange seed was planted on its back at birth."));
        p.setLang(pkTrans);

        return p;
    }
}
