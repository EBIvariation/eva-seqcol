package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeqColExtendedDataRepository extends JpaRepository<SeqColExtendedDataEntity, String> {
    public SeqColExtendedDataEntity<?> findSeqColExtendedDataEntityByDigest(String digest);

    @Query(value = "SELECT c.object->>'object' as object from sequence_collections_l1 p " +
            "right join seqcol_extended_data c ON p.object->>'names' = c.digest" +
            " or p.object->>'sequences' = c.digest" +
            " or p.object->>'lengths' = c.digest" +
            " WHERE p.digest= :level0Digest", nativeQuery = true)
    // To be reviewed. PB: We should identify each object of the list (lengths, sequences or names)
    public Optional<List<String>> getSeqColExtendedDataByLevel0Digest(@Param("level0Digest") String seqColDigest);

    public Optional<SeqColExtendedDataEntity<?>> getSeqColExtendedDataEntityByDigest(String digest);

    void removeSeqColExtendedDataEntityByDigest(String digest);

    void deleteAll();
}
