package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.PkmnImageRequest;
import org.antredesloutres.papi.dto.response.PkmnImageResponse;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.mapper.PkmnImageMapper;
import org.antredesloutres.papi.service.domain.PkmnImageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon/{id}/images")
@RequiredArgsConstructor
public class PkmnImageController {

    private final PkmnImageService pkmnImageService;
    private final PkmnImageMapper pkmnImageMapper;

    @GetMapping
    public List<PkmnImageResponse> getImages(
            @PathVariable Integer id,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String name) {

        if (tag != null) {
            return pkmnImageMapper.toResponseList(pkmnImageService.getImagesByTag(id, tag));
        }
        if (name != null) {
            return pkmnImageService.getImageByName(id, name)
                    .map(pkmnImageMapper::toResponse)
                    .map(List::of)
                    .orElse(List.of());
        }
        return pkmnImageMapper.toResponseList(pkmnImageService.getImages(id));
    }

    @GetMapping("/main")
    public PkmnImageResponse getMainImage(@PathVariable Integer id) {
        return pkmnImageService.getMainImage(id)
                .map(pkmnImageMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Main image for Pokemon", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PkmnImageResponse addImage(@PathVariable Integer id,
                                      @Valid @RequestBody PkmnImageRequest req) {
        return pkmnImageMapper.toResponse(pkmnImageService.addImage(id, req));
    }

    @PatchMapping("/{imageId}")
    public PkmnImageResponse updateImage(@PathVariable Integer id,
                                         @PathVariable Long imageId,
                                         @Valid @RequestBody PkmnImageRequest req) {
        return pkmnImageMapper.toResponse(pkmnImageService.updateImage(id, imageId, req));
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Integer id, @PathVariable Long imageId) {
        pkmnImageService.deleteImage(id, imageId);
    }

    @PostMapping("/{imageId}/main")
    public PkmnImageResponse promoteToMain(@PathVariable Integer id, @PathVariable Long imageId) {
        return pkmnImageMapper.toResponse(pkmnImageService.promoteToMain(id, imageId));
    }
}
