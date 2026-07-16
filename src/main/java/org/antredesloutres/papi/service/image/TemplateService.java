package org.antredesloutres.papi.service.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.image.TemplateDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loads and serves data-driven card templates.
 *
 * <p>Built-in definitions ship on the classpath ({@code resources/templates/*.json}).
 * At startup, JSON files found in the external {@code templates/} directory (relative
 * to the working directory, like {@code generated-images/}) are loaded on top and
 * override built-ins sharing the same id — so new templates can be added by dropping
 * a JSON file and its background image, without recompiling.</p>
 */
@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    /** Template ids end up in generated filenames — keep them filename-safe. */
    private static final Pattern SAFE_ID = Pattern.compile("[a-zA-Z0-9_-]+");

    private final ObjectMapper objectMapper;
    private final Path externalDir;
    private final String defaultTemplateId;
    private final Map<String, TemplateDefinition> templates = new ConcurrentHashMap<>();

    public TemplateService(ObjectMapper objectMapper,
                           @Value("${app.images.templates-dir:templates}") String templatesDir,
                           @Value("${app.images.default-template:summary}") String defaultTemplateId) {
        this.objectMapper = objectMapper;
        this.externalDir = Paths.get(templatesDir);
        this.defaultTemplateId = defaultTemplateId;
    }

    @PostConstruct
    public void loadTemplates() {
        templates.clear();
        loadClasspathTemplates();
        loadExternalTemplates();
        log.info("Loaded {} image template(s): {}", templates.size(), templates.keySet());
    }

    public List<TemplateDefinition> listTemplates() {
        return templates.values().stream()
                .sorted(Comparator.comparing(TemplateDefinition::id))
                .toList();
    }

    public TemplateDefinition getById(String id) {
        TemplateDefinition def = templates.get(id);
        if (def == null) {
            throw new EntityNotFoundException("Template", id);
        }
        return def;
    }

    /** Resolves the requested template, falling back to the configured default when absent. */
    public TemplateDefinition getByIdOrDefault(String id) {
        return getById(id != null && !id.isBlank() ? id : defaultTemplateId);
    }

    private void loadClasspathTemplates() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:templates/*.json");
            for (Resource resource : resources) {
                try (InputStream in = resource.getInputStream()) {
                    register(objectMapper.readValue(in, TemplateDefinition.class), resource.getDescription());
                } catch (Exception e) {
                    log.warn("Skipping invalid template {}: {}", resource.getDescription(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Could not scan classpath templates: {}", e.getMessage());
        }
    }

    private void loadExternalTemplates() {
        if (!Files.isDirectory(externalDir)) {
            return;
        }
        try (Stream<Path> files = Files.list(externalDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            register(objectMapper.readValue(p.toFile(), TemplateDefinition.class), p.toString());
                        } catch (Exception e) {
                            log.warn("Skipping invalid template {}: {}", p, e.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.warn("Could not scan external templates directory {}: {}", externalDir, e.getMessage());
        }
    }

    private void register(TemplateDefinition def, String source) {
        if (def.id() == null || !SAFE_ID.matcher(def.id()).matches()) {
            log.warn("Skipping template from {}: invalid id '{}'", source, def.id());
            return;
        }
        if (def.elements() == null || def.referenceWidth() <= 0 || def.referenceHeight() <= 0) {
            log.warn("Skipping template '{}' from {}: missing elements or invalid reference size", def.id(), source);
            return;
        }
        templates.put(def.id(), def);
    }
}
