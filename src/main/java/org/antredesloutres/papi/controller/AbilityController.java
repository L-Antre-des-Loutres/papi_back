package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.DescribedTranslationRequest;
import org.antredesloutres.papi.dto.request.SymbolRequest;
import org.antredesloutres.papi.dto.response.AbilityResponse;
import org.antredesloutres.papi.dto.response.AbilityTranslationResponse;
import org.antredesloutres.papi.mapper.AbilityMapper;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.service.domain.AbilityService;
import org.antredesloutres.papi.service.translation.AbilityTranslationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abilities")
@RequiredArgsConstructor
public class AbilityController {

    private final AbilityService abilityService;
    private final AbilityTranslationService abilityTranslationService;
    private final AbilityMapper abilityMapper;

    @GetMapping
    public Page<AbilityResponse> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return abilityService.getAllAbilities(pageable).map(abilityMapper::toResponse);
    }

    @GetMapping("/count")
    public int getCount() {
        return abilityService.getTotalCount();
    }

    @GetMapping("/{id}")
    public AbilityResponse getById(@PathVariable Integer id) {
        return abilityMapper.toResponse(abilityService.getAbilityById(id));
    }

    @PostMapping("/default")
    @ResponseStatus(HttpStatus.CREATED)
    public AbilityResponse createDefault() {
        String random = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
        return abilityMapper.toResponse(abilityService.createDefaultAbility("new-ability-" + random));
    }

    @PatchMapping("/{id}/symbol")
    public AbilityResponse updateSymbol(@PathVariable Integer id, @Valid @RequestBody SymbolRequest body) {
        return abilityMapper.toResponse(abilityService.updateSymbol(id, body.symbol()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        abilityService.deleteAbility(id);
    }

    @GetMapping("/{id}/translations")
    public List<AbilityTranslationResponse> getTranslations(@PathVariable Integer id) {
        return abilityMapper.toTranslationResponseList(abilityTranslationService.getAbilityTranslations(id));
    }

    @PutMapping("/{id}/translations/{language}")
    public AbilityTranslationResponse saveTranslation(@PathVariable Integer id,
                                                      @PathVariable Language language,
                                                      @Valid @RequestBody DescribedTranslationRequest body) {
        return abilityMapper.toTranslationResponse(
                abilityTranslationService.saveAbilityTranslation(id, language, body.name(), body.description())
        );
    }
}
