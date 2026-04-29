package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DirtiesContext
class TypeMatchupRepositoryTest {

    @Autowired
    TypeMatchupRepository matchupRepository;
    @Autowired
    TypeRepository typeRepository;

    @Test
    void findByAttackingTypeIdAndDefendingTypeId_returnsMatchup() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));
        Type water = typeRepository.save(new Type("water", "#6890f0", "Water"));
        TypeMatchup m = new TypeMatchup();
        m.setAttackingType(fire);
        m.setDefendingType(water);
        m.setEffectiveness(Effectiveness.NOT_VERY_EFFECTIVE);
        matchupRepository.save(m);

        // act
        Optional<TypeMatchup> result = matchupRepository
                .findByAttackingTypeIdAndDefendingTypeId(fire.getId(), water.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getEffectiveness()).isEqualTo(Effectiveness.NOT_VERY_EFFECTIVE);
    }

    @Test
    void findByAttackingTypeIdAndDefendingTypeId_returnsEmptyWhenAbsent() {
        // arrange + act
        Optional<TypeMatchup> result = matchupRepository
                .findByAttackingTypeIdAndDefendingTypeId(99, 100);

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithTypes_eagerlyLoadsBothTypes() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));
        Type water = typeRepository.save(new Type("water", "#6890f0", "Water"));
        TypeMatchup m = new TypeMatchup();
        m.setAttackingType(fire);
        m.setDefendingType(water);
        m.setEffectiveness(Effectiveness.SUPER_EFFECTIVE);
        matchupRepository.save(m);

        // act
        List<TypeMatchup> all = matchupRepository.findAllWithTypes();

        // assert
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAttackingType().getSymbol()).isEqualTo("fire");
        assertThat(all.get(0).getDefendingType().getSymbol()).isEqualTo("water");
    }

    @Test
    void seedAttackingMatchups_createsRowsForAllOtherTypes() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));
        Type water = typeRepository.save(new Type("water", "#6890f0", "Water"));
        Type grass = typeRepository.save(new Type("grass", "#78c850", "Grass"));

        // act
        matchupRepository.seedAttackingMatchups(fire.getId());

        // assert
        List<TypeMatchup> all = matchupRepository.findAllWithTypes();
        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(m -> {
            assertThat(m.getAttackingType().getId()).isEqualTo(fire.getId());
            assertThat(m.getDefendingType().getId()).isIn(water.getId(), grass.getId());
            assertThat(m.getEffectiveness()).isEqualTo(Effectiveness.EFFECTIVE);
        });
    }

    @Test
    void seedDefendingMatchups_createsRowsForAllOtherTypes() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));
        Type water = typeRepository.save(new Type("water", "#6890f0", "Water"));
        Type grass = typeRepository.save(new Type("grass", "#78c850", "Grass"));

        // act
        matchupRepository.seedDefendingMatchups(fire.getId());

        // assert
        List<TypeMatchup> all = matchupRepository.findAllWithTypes();
        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(m -> {
            assertThat(m.getDefendingType().getId()).isEqualTo(fire.getId());
            assertThat(m.getAttackingType().getId()).isIn(water.getId(), grass.getId());
        });
    }

    @Test
    void seedAttackingMatchups_doesNothingWhenSingleType() {
        // arrange
        Type fire = typeRepository.save(new Type("fire", "#ff0000", "Fire"));

        // act
        matchupRepository.seedAttackingMatchups(fire.getId());

        // assert
        assertThat(matchupRepository.findAllWithTypes()).isEmpty();
    }
}
