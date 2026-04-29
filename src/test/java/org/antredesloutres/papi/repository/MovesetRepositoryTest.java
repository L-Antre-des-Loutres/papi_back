package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext
class MovesetRepositoryTest {

    @Autowired
    MovesetRepository movesetRepository;
    @Autowired
    PkmnRepository pkmnRepository;
    @Autowired
    MoveRepository moveRepository;
    @Autowired
    TypeRepository typeRepository;

    @Test
    void findByPkmnId_returnsOnlyMatchingPkmn() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));
        Pkmn pikachu = pkmnRepository.save(makePkmn("pikachu"));
        Pkmn raichu = pkmnRepository.save(makePkmn("raichu"));
        Move thunderbolt = moveRepository.save(new Move("thunderbolt", "Thunderbolt", fire, 90, 100, 15));
        Move tackle = moveRepository.save(new Move("tackle", "Tackle", fire, 40, 100, 35));

        Moveset pikachuEntry = new Moveset();
        pikachuEntry.setPkmn(pikachu);
        pikachuEntry.setMove(thunderbolt);
        pikachuEntry.setLearnMethod(MoveLearnMethod.LEVEL_UP);
        pikachuEntry.setLearnLevel(26);
        movesetRepository.save(pikachuEntry);

        Moveset raichuEntry = new Moveset();
        raichuEntry.setPkmn(raichu);
        raichuEntry.setMove(tackle);
        raichuEntry.setLearnMethod(MoveLearnMethod.LEVEL_UP);
        raichuEntry.setLearnLevel(1);
        movesetRepository.save(raichuEntry);

        // act
        List<Moveset> result = movesetRepository.findByPkmnId(pikachu.getId());

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMove().getSymbol()).isEqualTo("thunderbolt");
        assertThat(result.get(0).getMove().getType().getSymbol()).isEqualTo("fire");
    }

    @Test
    void findByPkmnId_returnsEmptyForUnknownId() {
        // arrange + act
        List<Moveset> result = movesetRepository.findByPkmnId(99999);

        // assert
        assertThat(result).isEmpty();
    }

    private Pkmn makePkmn(String symbol) {
        Pkmn p = new Pkmn();
        p.setSymbol(symbol);
        return p;
    }
}
