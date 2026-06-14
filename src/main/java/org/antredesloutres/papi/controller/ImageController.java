package org.antredesloutres.papi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.response.ImageResponse;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.image.ImageMetadata;
import org.antredesloutres.papi.repository.image.ImageMetadataRepository;
import org.antredesloutres.papi.service.domain.PkmnImageService;
import org.antredesloutres.papi.service.domain.PkmnService;
import org.antredesloutres.papi.service.image.ImageGeneratorService;
import org.antredesloutres.papi.service.image.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Endpoints for retrieving and managing generated images")
public class ImageController {

    private final ImageService imageService;
    private final ImageGeneratorService imageGeneratorService;
    private final PkmnService pkmnService;
    private final PkmnImageService pkmnImageService;
    private final ImageMetadataRepository imageMetadataRepository;

    @Operation(
            summary = "Generate Pokemon Info Image",
            description = "Generates and saves a Pokemon information summary image in the specified language. " +
                    "The sprite defaults to the Pokemon's main gallery image; pass an imageId to use a specific gallery image. " +
                    "Reuses existing image if data hasn't changed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image generated or retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Pokemon or image not found")
            }
    )
    @PostMapping("/generate/pokemon/{id}")
    public ImageResponse generatePokemonImage(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "FR") Language language,
            @Parameter(description = "Gallery image to use as the sprite. Defaults to the main image, then the Pokemon's spriteUrl.")
            @RequestParam(required = false) Long imageId) throws IOException {
        Pkmn pkmn = pkmnService.getPkmnById(id);
        String spriteUrl = resolveSpriteUrl(pkmn, imageId);
        String currentStateHash = imageGeneratorService.calculateStateHash(pkmn, language, spriteUrl);

        Optional<ImageMetadata> existingMetadata = imageMetadataRepository.findByPkmnIdAndLanguage(id, language);

        if (existingMetadata.isPresent()) {
            ImageMetadata meta = existingMetadata.get();
            if (currentStateHash.equals(meta.getStateHash())) {
                try {
                    Resource resource = imageService.loadImage(meta.getFilename());
                    return toImageResponse(meta.getFilename(), resource);
                } catch (Exception e) {
                    // If file is missing, regenerate
                }
            }
        }

        // Generate new image
        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(pkmn, language, spriteUrl);
        String filename = String.format("pkmn-%d-%s.png", id, language.name().toLowerCase());
        imageService.saveImage(filename, image);

        // Update or create metadata
        ImageMetadata meta = existingMetadata.orElse(new ImageMetadata());
        meta.setPkmnId(id);
        meta.setLanguage(language);
        meta.setFilename(filename);
        meta.setStateHash(currentStateHash);
        meta.setUpdatedAt(LocalDateTime.now());
        imageMetadataRepository.save(meta);

        Resource resource = imageService.loadImage(filename);
        return toImageResponse(filename, resource);
    }

    /**
     * Resolves which sprite URL to draw on the card. An explicit gallery imageId wins;
     * otherwise the Pokemon's main gallery image is used, falling back to its spriteUrl.
     */
    private String resolveSpriteUrl(Pkmn pkmn, Long imageId) {
        if (imageId != null) {
            return pkmnImageService.getImage(pkmn.getId(), imageId).getUrl();
        }
        return pkmnImageService.getMainImage(pkmn.getId())
                .map(PkmnImage::getUrl)
                .orElse(pkmn.getSpriteUrl());
    }

    private ImageResponse toImageResponse(String filename, Resource resource) throws IOException {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(filename)
                .toUriString();

        return new ImageResponse(
                filename,
                MediaType.IMAGE_PNG_VALUE,
                resource.contentLength(),
                url
        );
    }

    @Operation(
            summary = "Get image metadata",
            description = "Returns metadata about a specific image including its URL, size, and content type.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
            }
    )
    @GetMapping("/{filename:.+}/info")
    public ImageResponse getImageInfo(
            @Parameter(description = "The filename of the image (e.g. test-img.png)")
            @PathVariable String filename) throws IOException {
        Resource file = imageService.loadImage(filename);
        String contentType = getContentType(filename, file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(filename)
                .toUriString();

        return new ImageResponse(
                filename,
                contentType,
                file.contentLength(),
                url
        );
    }

    @Operation(
            summary = "Download/Display image",
            description = "Returns the raw binary content of the image for direct display or download.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", 
                            description = "Image retrieved successfully",
                            content = @Content(mediaType = "image/*")
                    ),
                    @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
            }
    )
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "The filename of the image (e.g. test-img.png)")
            @PathVariable String filename) {
        Resource file = imageService.loadImage(filename);
        String contentType = getContentType(filename, file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }

    private String getContentType(String filename, Resource file) {
        String contentType = null;
        try {
            file.getFile();
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (Exception e) {
            // Fallback
        }

        if (contentType == null) {
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF_VALUE;
            } else {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        }
        return contentType;
    }
}
