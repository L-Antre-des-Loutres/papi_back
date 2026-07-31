package org.antredesloutres.papi.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaFixer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixSchema() {
        try {
            log.info("Applying database schema fixes for text columns...");
            jdbcTemplate.execute("ALTER TABLE pokemon_translations MODIFY description TEXT");
            jdbcTemplate.execute("ALTER TABLE ability_translations MODIFY description TEXT");
            jdbcTemplate.execute("ALTER TABLE move_translations MODIFY description TEXT");
            log.info("Schema fixes applied successfully.");
        } catch (Exception e) {
            log.warn("Schema fix failed (maybe already applied or tables missing): {}", e.getMessage());
        }
    }
}
