package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.domain.MovesetRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.repository.MoveRepository;
import org.antredesloutres.papi.repository.MovesetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovesetService {

    private final MovesetRepository movesetRepository;
    private final MoveRepository moveRepository;
    private final PkmnService pkmnService;

    @Transactional(readOnly = true)
    public Page<Moveset> getAllMovesets(Pageable pageable) {
        return movesetRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Moveset getMovesetById(Integer id) {
        return movesetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moveset", id));
    }

    @Transactional(readOnly = true)
    public List<Moveset> getPkmnMovesetByPkmnId(Integer pkmnId) {
        return movesetRepository.findByPkmnId(pkmnId);
    }

    @Transactional
    public Moveset addMove(Integer pkmnId, MovesetRequest req) {
        Moveset entry = new Moveset();
        entry.setPkmn(pkmnService.getPkmnById(pkmnId));
        entry.setMove(moveRepository.findById(req.moveId())
                .orElseThrow(() -> new EntityNotFoundException("Move", req.moveId())));
        entry.setLearnMethod(req.learnMethod());
        entry.setLearnLevel(req.learnLevel());
        return movesetRepository.save(entry);
    }

    @Transactional
    public void deleteMove(Integer pkmnId, Integer entryId) {
        Moveset entry = getMovesetById(entryId);
        if (!entry.getPkmn().getId().equals(pkmnId)) {
            throw new IllegalArgumentException("Moveset entry " + entryId + " does not belong to Pokemon " + pkmnId);
        }
        movesetRepository.delete(entry);
    }
}
