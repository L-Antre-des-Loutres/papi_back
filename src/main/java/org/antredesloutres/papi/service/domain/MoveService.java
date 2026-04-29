package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.repository.MoveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoveService {

    private final MoveRepository moveRepository;
    private final TypeService typeService;

    @Transactional(readOnly = true)
    public Page<Move> getAllMoves(Pageable pageable) {
        return moveRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Move getMoveById(Integer id) {
        return moveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Move", id));
    }

    @Transactional
    public Move createDefaultMove(String symbol, String nameEN, Type type, Integer power, Integer accuracy, Integer pp) {
        return moveRepository.save(new Move(symbol, nameEN, type, power, accuracy, pp));
    }

    @Transactional
    public Move updateSymbol(Integer id, String symbol) {
        Move move = getMoveById(id);
        move.setSymbol(symbol);
        return moveRepository.save(move);
    }

    @Transactional
    public Move updateType(Integer id, Integer typeId) {
        Move move = getMoveById(id);
        move.setType(typeId == null ? null : typeService.getTypeById(typeId));
        return moveRepository.save(move);
    }

    @Transactional
    public Move updatePower(Integer id, Integer power) {
        Move move = getMoveById(id);
        move.setPower(power);
        return moveRepository.save(move);
    }

    @Transactional
    public Move updateAccuracy(Integer id, Integer accuracy) {
        Move move = getMoveById(id);
        move.setAccuracy(accuracy);
        return moveRepository.save(move);
    }

    @Transactional
    public Move updatePp(Integer id, Integer pp) {
        Move move = getMoveById(id);
        move.setPp(pp);
        return moveRepository.save(move);
    }

    @Transactional
    public void deleteMove(Integer id) {
        moveRepository.delete(getMoveById(id));
    }

    @Transactional(readOnly = true)
    public int getTotalCount() {
        return (int) moveRepository.count();
    }
}
