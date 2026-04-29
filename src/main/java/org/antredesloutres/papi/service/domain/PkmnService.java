package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.domain.PkmnUpdateDto;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.antredesloutres.papi.repository.PkmnRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PkmnService {

    private final PkmnRepository pkmnRepository;
    private final TypeRepository typeRepository;
    private final AbilityRepository abilityRepository;

    @Transactional(readOnly = true)
    public Page<Pkmn> getAllPkmn(Pageable pageable) {
        return pkmnRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Pkmn getPkmnById(Integer id) {
        return pkmnRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pokemon", id));
    }

    @Transactional(readOnly = true)
    public int getTotalCount() {
        return (int) pkmnRepository.count();
    }

    @Transactional
    public Pkmn createDefaultPkmn(String symbol) {
        Pkmn pkmn = new Pkmn();
        pkmn.setSymbol(symbol);
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public Pkmn updateSymbol(Integer id, String symbol) {
        Pkmn pkmn = getPkmnById(id);
        pkmn.setSymbol(symbol);
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public Pkmn updateNationalDexNumber(Integer id, Integer nationalDexNumber) {
        Pkmn pkmn = getPkmnById(id);
        pkmn.setNationalDexNumber(nationalDexNumber);
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public Pkmn updatePrimaryType(Integer id, Integer typeId) {
        Pkmn pkmn = getPkmnById(id);
        pkmn.setPrimaryType(resolveType(typeId));
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public Pkmn updateSecondaryType(Integer id, Integer typeId) {
        Pkmn pkmn = getPkmnById(id);
        pkmn.setSecondaryType(resolveType(typeId));
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public Pkmn updateTags(Integer id, Set<String> tags) {
        Pkmn pkmn = getPkmnById(id);
        pkmn.setTags(tags);
        return pkmnRepository.save(pkmn);
    }

    @Transactional
    public void deletePkmn(Integer id) {
        if (!pkmnRepository.existsById(id)) {
            throw new EntityNotFoundException("Pokemon", id);
        }
        pkmnRepository.deleteById(id);
    }

    @Transactional
    public Pkmn updateAll(Integer id, PkmnUpdateDto dto) {
        Pkmn pkmn = getPkmnById(id);

        if (dto.getSymbol()          != null) pkmn.setSymbol(dto.getSymbol());
        if (dto.getHeight()          != null) pkmn.setHeight(dto.getHeight());
        if (dto.getWeight()          != null) pkmn.setWeight(dto.getWeight());
        if (dto.getExperienceYield() != null) pkmn.setExperienceYield(dto.getExperienceYield());
        if (dto.getExperienceGroup() != null) pkmn.setExperienceGroup(dto.getExperienceGroup());
        if (dto.getBaseFriendship()  != null) pkmn.setBaseFriendship(dto.getBaseFriendship());
        if (dto.getEggGroups()       != null) pkmn.setEggGroups(dto.getEggGroups());
        if (dto.getEggCycles()       != null) pkmn.setEggCycles(dto.getEggCycles());
        if (dto.getCatchRate()       != null) pkmn.setCatchRate(dto.getCatchRate());
        if (dto.getMaleRatio()       != null) pkmn.setMaleRatio(dto.getMaleRatio());

        if (dto.getBaseHp()          != null) pkmn.setBaseHp(dto.getBaseHp());
        if (dto.getBaseAttack()      != null) pkmn.setBaseAttack(dto.getBaseAttack());
        if (dto.getBaseDefense()     != null) pkmn.setBaseDefense(dto.getBaseDefense());
        if (dto.getBaseSpeAttack()   != null) pkmn.setBaseSpeAttack(dto.getBaseSpeAttack());
        if (dto.getBaseSpeDefense()  != null) pkmn.setBaseSpeDefense(dto.getBaseSpeDefense());
        if (dto.getBaseSpeed()       != null) pkmn.setBaseSpeed(dto.getBaseSpeed());

        if (dto.getEvHp()            != null) pkmn.setEvHp(dto.getEvHp());
        if (dto.getEvAttack()        != null) pkmn.setEvAttack(dto.getEvAttack());
        if (dto.getEvDefense()       != null) pkmn.setEvDefense(dto.getEvDefense());
        if (dto.getEvSpeAttack()     != null) pkmn.setEvSpeAttack(dto.getEvSpeAttack());
        if (dto.getEvSpeDefense()    != null) pkmn.setEvSpeDefense(dto.getEvSpeDefense());
        if (dto.getEvSpeed()         != null) pkmn.setEvSpeed(dto.getEvSpeed());

        if (dto.getTags()            != null) pkmn.setTags(dto.getTags());

        pkmn.setPrimaryType(dto.getPrimaryTypeId()           != null ? typeRepository.getReferenceById(dto.getPrimaryTypeId())           : null);
        pkmn.setSecondaryType(dto.getSecondaryTypeId()       != null ? typeRepository.getReferenceById(dto.getSecondaryTypeId())         : null);
        pkmn.setPrimaryAbility(dto.getPrimaryAbilityId()     != null ? abilityRepository.getReferenceById(dto.getPrimaryAbilityId())     : null);
        pkmn.setSecondaryAbility(dto.getSecondaryAbilityId() != null ? abilityRepository.getReferenceById(dto.getSecondaryAbilityId())   : null);
        pkmn.setHiddenAbility(dto.getHiddenAbilityId()       != null ? abilityRepository.getReferenceById(dto.getHiddenAbilityId())      : null);

        return pkmnRepository.save(pkmn);
    }

    private Type resolveType(Integer typeId) {
        if (typeId == null) return null;
        return typeRepository.findById(typeId)
                .orElseThrow(() -> new EntityNotFoundException("Type", typeId));
    }
}
