package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.domain.TypeMatchupDto;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.antredesloutres.papi.repository.TypeMatchupRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeMatchupService {

    private final TypeMatchupRepository matchupRepository;
    private final TypeRepository typeRepository;

    @Transactional(readOnly = true)
    public List<TypeMatchup> findAll() {
        return matchupRepository.findAllWithTypes();
    }

    @Transactional
    public TypeMatchup upsert(TypeMatchupDto dto) {
        TypeMatchup matchup = matchupRepository
                .findByAttackingTypeIdAndDefendingTypeId(dto.attackingTypeId(), dto.defendingTypeId())
                .orElseGet(() -> {
                    TypeMatchup m = new TypeMatchup();
                    m.setAttackingType(typeRepository.getReferenceById(dto.attackingTypeId()));
                    m.setDefendingType(typeRepository.getReferenceById(dto.defendingTypeId()));
                    return m;
                });

        matchup.setEffectiveness(dto.effectiveness());
        return matchupRepository.save(matchup);
    }

    @Transactional
    public void reset(Integer attackingTypeId, Integer defendingTypeId) {
        matchupRepository
                .findByAttackingTypeIdAndDefendingTypeId(attackingTypeId, defendingTypeId)
                .ifPresent(m -> {
                    m.setEffectiveness(Effectiveness.EFFECTIVE);
                    matchupRepository.save(m);
                });
    }
}
