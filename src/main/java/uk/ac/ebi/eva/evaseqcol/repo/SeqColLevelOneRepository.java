package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;

import java.util.List;

public interface SeqColLevelOneRepository extends JpaRepository<SeqColLevelOneEntity, String>,
        SeqColLevelOneCustomRepository {

    @Query(value = "SELECT digest FROM sequence_collections_L1", nativeQuery = true)
    Page<String> findAllDigests(Pageable pageable);

    @Query("select m from SeqColLevelOneEntity s join s.metadata m where s.digest = :digest")
    List<SeqColMetadataEntity> findMetadataBySeqColDigest(@Param("digest") String digest);

    @Query("select count(m) > 0 from SeqColLevelOneEntity s join s.metadata m where m.sourceIdentifier = :sourceId")
    boolean existsMetadataWithSourceId(@Param("sourceId") String sourceId);
}
