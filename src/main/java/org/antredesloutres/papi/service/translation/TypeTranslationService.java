package org.antredesloutres.papi.service.translation;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.antredesloutres.papi.repository.TypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeTranslationService {

    private final TypeRepository typeRepository;

    @Transactional(readOnly = true)
    public List<TypeTranslation> getTypeTranslations(Integer typeId) {
        return typeRepository.findById(typeId)
                .map(Type::getLang)
                .orElseThrow(() -> new EntityNotFoundException("Type", typeId))
                .stream().toList();
    }

    @Transactional
    public TypeTranslation saveTypeTranslation(Integer id, Language language, String name) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type", id));

        TypeTranslation translation = type.getLang().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> {
                    TypeTranslation t = new TypeTranslation(language, name);
                    type.getLang().add(t);
                    return t;
                });

        translation.setName(name);
        typeRepository.save(type);
        return translation;
    }
}
