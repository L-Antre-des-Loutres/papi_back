package org.antredesloutres.papi.service.translation;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.MoveTranslation;
import org.antredesloutres.papi.repository.MoveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveTranslationService {

    private final MoveRepository moveRepository;

    @Transactional(readOnly = true)
    public List<MoveTranslation> getMoveTranslations(Integer moveId) {
        return moveRepository.findById(moveId)
                .map(Move::getLang)
                .orElseThrow(() -> new EntityNotFoundException("Move", moveId))
                .stream().toList();
    }

    @Transactional
    public MoveTranslation saveMoveTranslation(Integer id, Language language, String name, String description) {
        Move move = moveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Move", id));

        MoveTranslation translation = move.getLang().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> {
                    MoveTranslation t = new MoveTranslation(language, name, description);
                    move.getLang().add(t);
                    return t;
                });

        translation.setName(name);
        translation.setDescription(description);
        moveRepository.save(move);
        return translation;
    }
}
