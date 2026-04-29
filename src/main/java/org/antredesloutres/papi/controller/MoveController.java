package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.DescribedTranslationRequest;
import org.antredesloutres.papi.dto.request.IntValueRequest;
import org.antredesloutres.papi.dto.request.SymbolRequest;
import org.antredesloutres.papi.dto.request.TypeRefRequest;
import org.antredesloutres.papi.dto.response.MoveResponse;
import org.antredesloutres.papi.dto.response.MoveTranslationResponse;
import org.antredesloutres.papi.mapper.MoveMapper;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.service.domain.MoveService;
import org.antredesloutres.papi.service.translation.MoveTranslationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moves")
@RequiredArgsConstructor
public class MoveController {

    private final MoveService moveService;
    private final MoveTranslationService moveTranslationService;
    private final MoveMapper moveMapper;

    @GetMapping
    public Page<MoveResponse> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return moveService.getAllMoves(pageable).map(moveMapper::toResponse);
    }

    @GetMapping("/count")
    public int getCount() {
        return moveService.getTotalCount();
    }

    @GetMapping("/{id}")
    public MoveResponse getById(@PathVariable Integer id) {
        return moveMapper.toResponse(moveService.getMoveById(id));
    }

    @PostMapping("/default")
    @ResponseStatus(HttpStatus.CREATED)
    public MoveResponse createDefault() {
        String random = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
        return moveMapper.toResponse(moveService.createDefaultMove("new-move-" + random, "New move", null, 0, 0, 0));
    }

    @PatchMapping("/{id}/symbol")
    public MoveResponse updateSymbol(@PathVariable Integer id, @Valid @RequestBody SymbolRequest body) {
        return moveMapper.toResponse(moveService.updateSymbol(id, body.symbol()));
    }

    @PatchMapping("/{id}/type")
    public MoveResponse updateType(@PathVariable Integer id, @RequestBody TypeRefRequest body) {
        return moveMapper.toResponse(moveService.updateType(id, body.typeId()));
    }

    @PatchMapping("/{id}/power")
    public MoveResponse updatePower(@PathVariable Integer id, @Valid @RequestBody IntValueRequest body) {
        return moveMapper.toResponse(moveService.updatePower(id, body.value()));
    }

    @PatchMapping("/{id}/accuracy")
    public MoveResponse updateAccuracy(@PathVariable Integer id, @Valid @RequestBody IntValueRequest body) {
        return moveMapper.toResponse(moveService.updateAccuracy(id, body.value()));
    }

    @PatchMapping("/{id}/pp")
    public MoveResponse updatePp(@PathVariable Integer id, @Valid @RequestBody IntValueRequest body) {
        return moveMapper.toResponse(moveService.updatePp(id, body.value()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        moveService.deleteMove(id);
    }

    @GetMapping("/{id}/translations")
    public List<MoveTranslationResponse> getTranslations(@PathVariable Integer id) {
        return moveMapper.toTranslationResponseList(moveTranslationService.getMoveTranslations(id));
    }

    @PutMapping("/{id}/translations/{language}")
    public MoveTranslationResponse saveTranslation(@PathVariable Integer id,
                                                   @PathVariable Language language,
                                                   @Valid @RequestBody DescribedTranslationRequest body) {
        return moveMapper.toTranslationResponse(
                moveTranslationService.saveMoveTranslation(id, language, body.name(), body.description())
        );
    }
}
