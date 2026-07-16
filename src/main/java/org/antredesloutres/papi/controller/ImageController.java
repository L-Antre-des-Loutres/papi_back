package org.antredesloutres.papi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.response.ImageResponse;
import org.antredesloutres.papi.dto.response.TemplateResponse;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.image.ImageMetadata;
import org.antredesloutres.papi.model.image.TemplateDefinition;
import org.antredesloutres.papi.repository.image.ImageMetadataRepository;
import org.antredesloutres.papi.service.domain.PkmnImageService;
import org.antredesloutres.papi.service.domain.PkmnService;
import org.antredesloutres.papi.service.image.ImageGeneratorService;
import org.antredesloutres.papi.service.image.ImageService;
import org.antredesloutres.papi.service.image.TemplateService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
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
    private final TemplateService templateService;
    private final ImageMetadataRepository imageMetadataRepository;

    @Operation(
            summary = "List available templates",
            description = "Returns the card templates that can be passed as the 'template' parameter of the render and generate endpoints.",
            responses = @ApiResponse(responseCode = "200", description = "Templates listed successfully")
    )
    @GetMapping("/templates")
    public List<TemplateResponse> listTemplates() {
        return templateService.listTemplates().stream()
                .map(t -> new TemplateResponse(t.id(), t.name()))
                .toList();
    }

    @Operation(
            summary = "Render Pokemon Info Image (one-shot)",
            description = "Renders a Pokemon summary card and streams the PNG directly, without persisting anything. " +
                    "Intended for live previews and automated consumers that just need the bytes. " +
                    "The sprite defaults to the Pokemon's main gallery image; pass an imageId to use a specific gallery image.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image rendered successfully",
                            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Pokemon, image or template not found", content = @Content)
            }
    )
    @GetMapping("/render/pokemon/{id}")
    public ResponseEntity<byte[]> renderPokemonImage(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "FR") Language language,
            @Parameter(description = "Gallery image to use as the sprite. Defaults to the main image, then the Pokemon's spriteUrl.")
            @RequestParam(required = false) Long imageId,
            @Parameter(description = "Template id (see GET /api/images/templates). Defaults to the configured default template.")
            @RequestParam(required = false) String template) throws IOException {
        Pkmn pkmn = pkmnService.getPkmnById(id);
        String spriteUrl = pkmnImageService.resolveSpriteUrl(pkmn, imageId);
        TemplateDefinition tpl = templateService.getByIdOrDefault(template);

        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(pkmn, language, spriteUrl, tpl);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(out.toByteArray());
    }

    @Operation(
            summary = "Generate Pokemon Info Image (persisted)",
            description = "Generates and saves a Pokemon information summary image in the specified language, and returns a stable URL to it. " +
                    "The sprite defaults to the Pokemon's main gallery image; pass an imageId to use a specific gallery image. " +
                    "Reuses the existing image if data hasn't changed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image generated or retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Pokemon, image or template not found")
            }
    )
    @PostMapping("/generate/pokemon/{id}")
    public ImageResponse generatePokemonImage(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "FR") Language language,
            @Parameter(description = "Gallery image to use as the sprite. Defaults to the main image, then the Pokemon's spriteUrl.")
            @RequestParam(required = false) Long imageId,
            @Parameter(description = "Template id (see GET /api/images/templates). Defaults to the configured default template.")
            @RequestParam(required = false) String template) throws IOException {
        Pkmn pkmn = pkmnService.getPkmnById(id);
        String spriteUrl = pkmnImageService.resolveSpriteUrl(pkmn, imageId);
        TemplateDefinition tpl = templateService.getByIdOrDefault(template);
        String currentStateHash = imageGeneratorService.calculateStateHash(pkmn, language, spriteUrl, tpl);

        Optional<ImageMetadata> existingMetadata =
                imageMetadataRepository.findByPkmnIdAndLanguageAndTemplate(id, language, tpl.id());

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
        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(pkmn, language, spriteUrl, tpl);
        String filename = String.format("pkmn-%d-%s-%s.png", id, language.name().toLowerCase(), tpl.id());
        imageService.saveImage(filename, image);

        // Update or create metadata
        ImageMetadata meta = existingMetadata.orElse(new ImageMetadata());
        meta.setPkmnId(id);
        meta.setLanguage(language);
        meta.setTemplate(tpl.id());
        meta.setFilename(filename);
        meta.setStateHash(currentStateHash);
        meta.setUpdatedAt(LocalDateTime.now());
        imageMetadataRepository.save(meta);

        Resource resource = imageService.loadImage(filename);
        return toImageResponse(filename, resource);
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
