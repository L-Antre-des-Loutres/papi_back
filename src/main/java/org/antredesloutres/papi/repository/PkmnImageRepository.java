package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.PkmnImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PkmnImageRepository extends JpaRepository<PkmnImage, Long> {

    List<PkmnImage> findByPkmn_Id(Integer pkmnId);

    Optional<PkmnImage> findByPkmn_IdAndName(Integer pkmnId, String name);

    Optional<PkmnImage> findByPkmn_IdAndMainTrue(Integer pkmnId);

    @Query("SELECT i FROM PkmnImage i WHERE i.pkmn.id = :pkmnId AND :tag MEMBER OF i.tags")
    List<PkmnImage> findByPkmnIdAndTag(@Param("pkmnId") Integer pkmnId, @Param("tag") String tag);

    @Modifying
    @Query("UPDATE PkmnImage i SET i.main = false WHERE i.pkmn.id = :pkmnId")
    void clearMainForPkmn(@Param("pkmnId") Integer pkmnId);

    boolean existsByPkmn_IdAndId(Integer pkmnId, Long imageId);
}
