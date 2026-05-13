package org.antredesloutres.papi.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.response.ImageResponse;
import org.antredesloutres.papi.model.enumerated.Language;
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

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Endpoints for retrieving and managing generated images")
public class ImageController {

    private final ImageService imageService;
    private final PkmnService pkmnService;
    private final ImageGeneratorService imageGeneratorService;

    @Operation(
            summary = "Generate Pokémon info card",
            description = "Generates a stylized image containing Pokémon stats and info.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Image generated successfully",
                            content = @Content(mediaType = "image/png")
                    ),
                    @ApiResponse(responseCode = "404", description = "Pokémon not found")
            }
    )
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<Resource> getPkmnImage(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "EN") Language lang) throws IOException {
        var pkmn = pkmnService.getPkmnById(id);
        BufferedImage image = imageGeneratorService.generatePkmnInfoImage(pkmn, lang);

        String filename = "pkmn-" + id + "-" + lang.name().toLowerCase() + ".png";
        imageService.saveImage(filename, image);

        Resource resource = imageService.loadImage(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
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
            if (file.getFile() != null) {
                contentType = Files.probeContentType(file.getFile().toPath());
            }
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
