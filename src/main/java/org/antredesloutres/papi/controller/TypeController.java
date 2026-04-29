package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.ColorRequest;
import org.antredesloutres.papi.dto.request.NameTranslationRequest;
import org.antredesloutres.papi.dto.request.SymbolRequest;
import org.antredesloutres.papi.dto.request.TagRequest;
import org.antredesloutres.papi.dto.domain.TypeCreateRequest;
import org.antredesloutres.papi.dto.response.TypeResponse;
import org.antredesloutres.papi.dto.response.TypeTranslationResponse;
import org.antredesloutres.papi.mapper.TypeMapper;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.service.domain.TypeService;
import org.antredesloutres.papi.service.translation.TypeTranslationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types")
@RequiredArgsConstructor
public class TypeController {

    private final TypeService typeService;
    private final TypeTranslationService typeTranslationService;
    private final TypeMapper typeMapper;

    @GetMapping
    public Page<TypeResponse> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return typeService.getAllTypes(pageable).map(typeMapper::toResponse);
    }

    @GetMapping("/count")
    public int getCount() {
        return typeService.getTotalCount();
    }

    @GetMapping("/{id}")
    public TypeResponse getById(@PathVariable Integer id) {
        return typeMapper.toResponse(typeService.getTypeById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TypeResponse create(@Valid @RequestBody TypeCreateRequest body) {
        return typeMapper.toResponse(typeService.createDefaultType(body.symbol(), normalizeColor(body.color()), body.nameEN()));
    }

    @PostMapping("/default")
    @ResponseStatus(HttpStatus.CREATED)
    public TypeResponse createDefault() {
        String color = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
        return typeMapper.toResponse(typeService.createDefaultType("new-type-" + color, "#" + color, "New type"));
    }

    @PostMapping("/{id}/tags")
    public TypeResponse addTag(@PathVariable Integer id, @Valid @RequestBody TagRequest body) {
        return typeMapper.toResponse(typeService.addTag(id, body.tag()));
    }

    @PatchMapping("/{id}/symbol")
    public TypeResponse updateSymbol(@PathVariable Integer id, @Valid @RequestBody SymbolRequest body) {
        return typeMapper.toResponse(typeService.updateSymbol(id, body.symbol()));
    }

    @PatchMapping("/{id}/color")
    public TypeResponse updateColor(@PathVariable Integer id, @Valid @RequestBody ColorRequest body) {
        return typeMapper.toResponse(typeService.updateColor(id, normalizeColor(body.color())));
    }

    @DeleteMapping("/{id}/tags/{tag}")
    public TypeResponse removeTag(@PathVariable Integer id, @PathVariable String tag) {
        return typeMapper.toResponse(typeService.removeTag(id, tag));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        typeService.deleteType(id);
    }

    @GetMapping("/{id}/translations")
    public List<TypeTranslationResponse> getTranslations(@PathVariable Integer id) {
        return typeMapper.toTranslationResponseList(typeTranslationService.getTypeTranslations(id));
    }

    @PutMapping("/{id}/translations/{language}")
    public TypeTranslationResponse saveTranslation(@PathVariable Integer id,
                                                   @PathVariable Language language,
                                                   @Valid @RequestBody NameTranslationRequest body) {
        return typeMapper.toTranslationResponse(
                typeTranslationService.saveTypeTranslation(id, language, body.name())
        );
    }

    private static String normalizeColor(String color) {
        if (color == null || color.isBlank()) return null;
        return color.startsWith("#") ? color : "#" + color;
    }
}
