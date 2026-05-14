package org.antredesloutres.papi.repository.image;

import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.image.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Integer> {
    Optional<ImageMetadata> findByPkmnIdAndLanguage(Integer pkmnId, Language language);
}
