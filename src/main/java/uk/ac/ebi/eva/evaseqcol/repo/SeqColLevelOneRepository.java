package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.util.List;

@Repository
public interface SeqColLevelOneRepository extends JpaRepository<SeqColLevelOneEntity, String>,
        SeqColLevelOneCustomRepository {
    SeqColLevelOneEntity findSeqColLevelOneEntityByDigest(String digest);

    long countSeqColLevelOneEntitiesByDigest(String digest);

    void removeSeqColLevelOneEntityByDigest(String digest);

    void deleteAll();

    @Query(value = "select source_id, source_url, naming_convention, created_on from seqcol_md where digest = ?1", nativeQuery = true)
    List<Object[]> findMetadataBySeqColDigest(String digest);

    @Query(value = "select source_id, source_url, naming_convention, created_on from seqcol_md", nativeQuery = true)
    List<Object[]> findAllMetadata();
}
