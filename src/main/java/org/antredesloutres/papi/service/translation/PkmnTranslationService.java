package org.antredesloutres.papi.service.translation;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.repository.PkmnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PkmnTranslationService {

    private final PkmnRepository pkmnRepository;

    @Transactional(readOnly = true)
    public List<PkmnTranslation> getPkmnTranslations(Integer pkmnId) {
        return pkmnRepository.findById(pkmnId)
                .map(Pkmn::getLang)
                .orElseThrow(() -> new EntityNotFoundException("Pokemon", pkmnId))
                .stream().toList();
    }

    @Transactional
    public PkmnTranslation savePkmnTranslation(Integer id, Language language, String pkmnName, String formName, String description) {
        Pkmn pkmn = pkmnRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pokemon", id));

        PkmnTranslation translation = pkmn.getLang().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> {
                    PkmnTranslation t = new PkmnTranslation(language, pkmnName, formName, description);
                    pkmn.getLang().add(t);
                    return t;
                });

        translation.setPkmnName(pkmnName);
        translation.setFormName(formName);
        translation.setDescription(description);
        pkmnRepository.save(pkmn);
        return translation;
    }
}
