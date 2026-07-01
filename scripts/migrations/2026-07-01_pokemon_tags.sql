-- Pkmn.tags is now an @ElementCollection (table pokemon_tags) instead of a
-- serialized column. Prod runs with ddl-auto=validate, so this table must exist
-- before deploying the new build. Dev (ddl-auto=update) creates it on its own.

CREATE TABLE pokemon_tags (
    pkmn_id INTEGER NOT NULL,
    tag     VARCHAR(255),
    CONSTRAINT fk_pokemon_tags_pkmn FOREIGN KEY (pkmn_id) REFERENCES pokemon (id)
) ENGINE=InnoDB;

-- The old pokemon.tags column holds Java-serialized data, so it can't be copied
-- over in SQL. Re-apply existing tags via PATCH /api/pokemon/{id}/tags after
-- deploy, then drop the legacy column once things look good:
-- ALTER TABLE pokemon DROP COLUMN tags;
