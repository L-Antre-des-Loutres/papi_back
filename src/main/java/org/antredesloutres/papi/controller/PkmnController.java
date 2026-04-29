package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.IntValueRequest;
import org.antredesloutres.papi.dto.domain.MovesetRequest;
import org.antredesloutres.papi.dto.domain.PkmnTranslationRequest;
import org.antredesloutres.papi.dto.domain.PkmnUpdateDto;
import org.antredesloutres.papi.dto.request.SymbolRequest;
import org.antredesloutres.papi.dto.request.TagsRequest;
import org.antredesloutres.papi.dto.request.TypeRefRequest;
import org.antredesloutres.papi.dto.response.MovesetResponse;
import org.antredesloutres.papi.dto.response.PkmnResponse;
import org.antredesloutres.papi.dto.response.PkmnSummaryResponse;
import org.antredesloutres.papi.dto.response.PkmnTranslationResponse;
import org.antredesloutres.papi.mapper.MovesetMapper;
import org.antredesloutres.papi.mapper.PkmnMapper;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.service.domain.MovesetService;
import org.antredesloutres.papi.service.domain.PkmnService;
import org.antredesloutres.papi.service.translation.PkmnTranslationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
@RequiredArgsConstructor
public class PkmnController {

    private final PkmnService pkmnService;
    private final PkmnTranslationService pkmnTranslationService;
    private final MovesetService movesetService;
    private final PkmnMapper pkmnMapper;
    private final MovesetMapper movesetMapper;

    @GetMapping
    public Page<PkmnSummaryResponse> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return pkmnService.getAllPkmn(pageable).map(pkmnMapper::toSummary);
    }

    @GetMapping("/count")
    public int getCount() {
        return pkmnService.getTotalCount();
    }

    @GetMapping("/{id}")
    public PkmnResponse getById(@PathVariable Integer id) {
        return pkmnMapper.toResponse(pkmnService.getPkmnById(id));
    }

    @PostMapping("/default")
    @ResponseStatus(HttpStatus.CREATED)
    public PkmnResponse createDefault() {
        String random = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
        return pkmnMapper.toResponse(pkmnService.createDefaultPkmn("new-pkmn-" + random));
    }

    @PatchMapping("/{id}")
    public PkmnResponse updateAll(@PathVariable Integer id, @Valid @RequestBody PkmnUpdateDto dto) {
        return pkmnMapper.toResponse(pkmnService.updateAll(id, dto));
    }

    @PatchMapping("/{id}/symbol")
    public PkmnResponse updateSymbol(@PathVariable Integer id, @Valid @RequestBody SymbolRequest body) {
        return pkmnMapper.toResponse(pkmnService.updateSymbol(id, body.symbol()));
    }

    @PatchMapping("/{id}/nationalDexNumber")
    public PkmnResponse updateNationalDexNumber(@PathVariable Integer id, @Valid @RequestBody IntValueRequest body) {
        return pkmnMapper.toResponse(pkmnService.updateNationalDexNumber(id, body.value()));
    }

    @PatchMapping("/{id}/primaryType")
    public PkmnResponse updatePrimaryType(@PathVariable Integer id, @RequestBody TypeRefRequest body) {
        return pkmnMapper.toResponse(pkmnService.updatePrimaryType(id, body.typeId()));
    }

    @PatchMapping("/{id}/secondaryType")
    public PkmnResponse updateSecondaryType(@PathVariable Integer id, @RequestBody TypeRefRequest body) {
        return pkmnMapper.toResponse(pkmnService.updateSecondaryType(id, body.typeId()));
    }

    @PatchMapping("/{id}/tags")
    public PkmnResponse updateTags(@PathVariable Integer id, @Valid @RequestBody TagsRequest body) {
        return pkmnMapper.toResponse(pkmnService.updateTags(id, body.tags()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        pkmnService.deletePkmn(id);
    }

    @GetMapping("/{id}/translations")
    public List<PkmnTranslationResponse> getTranslations(@PathVariable Integer id) {
        return pkmnMapper.toTranslationResponseList(pkmnTranslationService.getPkmnTranslations(id));
    }

    @PutMapping("/{id}/translations/{language}")
    public PkmnTranslationResponse saveTranslation(@PathVariable Integer id,
                                                   @PathVariable Language language,
                                                   @Valid @RequestBody PkmnTranslationRequest body) {
        return pkmnMapper.toTranslationResponse(
                pkmnTranslationService.savePkmnTranslation(id, language,
                        body.pkmnName(), body.formName(), body.description())
        );
    }

    @GetMapping("/{id}/moveset")
    public List<MovesetResponse> getMoveset(@PathVariable Integer id) {
        return movesetMapper.toResponseList(movesetService.getPkmnMovesetByPkmnId(id));
    }

    @PostMapping("/{id}/moveset")
    @ResponseStatus(HttpStatus.CREATED)
    public MovesetResponse addMove(@PathVariable Integer id, @Valid @RequestBody MovesetRequest req) {
        return movesetMapper.toResponse(movesetService.addMove(id, req));
    }

    @DeleteMapping("/{id}/moveset/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMove(@PathVariable Integer id, @PathVariable Integer entryId) {
        movesetService.deleteMove(id, entryId);
    }
}
