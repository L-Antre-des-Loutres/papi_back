package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.request.PkmnImageRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.antredesloutres.papi.repository.PkmnImageRepository;
import org.antredesloutres.papi.repository.PkmnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PkmnImageService {

    private final PkmnImageRepository pkmnImageRepository;
    private final PkmnRepository pkmnRepository;

    @Transactional(readOnly = true)
    public List<PkmnImage> getImages(Integer pkmnId) {
        requirePkmnExists(pkmnId);
        return pkmnImageRepository.findByPkmn_Id(pkmnId);
    }

    @Transactional(readOnly = true)
    public List<PkmnImage> getImagesByTag(Integer pkmnId, String tag) {
        requirePkmnExists(pkmnId);
        return pkmnImageRepository.findByPkmnIdAndTag(pkmnId, tag);
    }

    @Transactional(readOnly = true)
    public Optional<PkmnImage> getImageByName(Integer pkmnId, String name) {
        requirePkmnExists(pkmnId);
        return pkmnImageRepository.findByPkmn_IdAndName(pkmnId, name);
    }

    @Transactional(readOnly = true)
    public Optional<PkmnImage> getMainImage(Integer pkmnId) {
        requirePkmnExists(pkmnId);
        return pkmnImageRepository.findByPkmn_IdAndMainTrue(pkmnId);
    }

    @Transactional
    public PkmnImage addImage(Integer pkmnId, PkmnImageRequest req) {
        Pkmn pkmn = pkmnRepository.findById(pkmnId)
                .orElseThrow(() -> new EntityNotFoundException("Pokemon", pkmnId));

        if (req.main()) {
            pkmnImageRepository.clearMainForPkmn(pkmnId);
        }

        PkmnImage image = new PkmnImage();
        image.setPkmn(pkmn);
        image.setUrl(req.url());
        image.setName(req.name());
        image.setTags(req.tags() != null ? req.tags() : Set.of());
        image.setMain(req.main());
        image.setAddedAt(Instant.now());

        return pkmnImageRepository.save(image);
    }

    @Transactional
    public PkmnImage updateImage(Integer pkmnId, Long imageId, PkmnImageRequest req) {
        PkmnImage image = requireImageBelongsToPkmn(pkmnId, imageId);

        if (req.main() && !image.isMain()) {
            pkmnImageRepository.clearMainForPkmn(pkmnId);
        }

        image.setUrl(req.url());
        image.setName(req.name());
        image.setTags(req.tags() != null ? req.tags() : Set.of());
        image.setMain(req.main());

        return pkmnImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Integer pkmnId, Long imageId) {
        requireImageBelongsToPkmn(pkmnId, imageId);
        pkmnImageRepository.deleteById(imageId);
    }

    @Transactional
    public PkmnImage promoteToMain(Integer pkmnId, Long imageId) {
        PkmnImage image = requireImageBelongsToPkmn(pkmnId, imageId);
        pkmnImageRepository.clearMainForPkmn(pkmnId);
        image.setMain(true);
        return pkmnImageRepository.save(image);
    }

    private void requirePkmnExists(Integer pkmnId) {
        if (!pkmnRepository.existsById(pkmnId)) {
            throw new EntityNotFoundException("Pokemon", pkmnId);
        }
    }

    private PkmnImage requireImageBelongsToPkmn(Integer pkmnId, Long imageId) {
        requirePkmnExists(pkmnId);
        PkmnImage image = pkmnImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("PkmnImage", imageId));
        if (!image.getPkmn().getId().equals(pkmnId)) {
            throw new EntityNotFoundException("PkmnImage", imageId);
        }
        return image;
    }
}
