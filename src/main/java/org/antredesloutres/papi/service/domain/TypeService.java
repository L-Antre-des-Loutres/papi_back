package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.repository.TypeMatchupRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TypeService {

    private final TypeRepository repository;
    private final TypeMatchupRepository matchupRepository;

    @Transactional(readOnly = true)
    public Page<Type> getAllTypes(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Type getTypeById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type", id));
    }

    @Transactional
    public Type createDefaultType(String symbol, String color, String nameEN) {
        Type newType = repository.saveAndFlush(new Type(symbol, color, nameEN));
        // Two native bulk inserts: O(1) round-trips instead of O(N) per direction.
        matchupRepository.seedAttackingMatchups(newType.getId());
        matchupRepository.seedDefendingMatchups(newType.getId());
        return newType;
    }

    @Transactional
    public Type addTag(Integer id, String tag) {
        Type type = getTypeById(id);
        Set<String> tags = type.getTags();
        if (tags == null) {
            tags = new HashSet<>();
            type.setTags(tags);
        }
        tags.add(tag);
        return repository.save(type);
    }

    @Transactional
    public Type updateSymbol(Integer id, String symbol) {
        Type type = getTypeById(id);
        type.setSymbol(symbol);
        return repository.save(type);
    }

    @Transactional
    public Type updateColor(Integer id, String color) {
        Type type = getTypeById(id);
        type.setColor(color);
        return repository.save(type);
    }

    @Transactional
    public Type removeTag(Integer id, String tag) {
        Type type = getTypeById(id);
        type.getTags().remove(tag);
        return repository.save(type);
    }

    @Transactional
    public void deleteType(Integer id) {
        repository.delete(getTypeById(id));
    }

    @Transactional(readOnly = true)
    public int getTotalCount() {
        return (int) repository.count();
    }
}
