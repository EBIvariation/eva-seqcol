package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.util.Optional;

@Repository
public interface SeqColLevelOneRepository extends JpaRepository<SeqColLevelOneEntity, String> {
    Optional<SeqColLevelOneEntity> findSeqColLevelOneEntityByDigest(String digest);
    long countSeqColLevelOneEntitiesByDigest(String digest);

    void removeSeqColLevelOneEntityByDigest(String digest);

    void deleteAll();

}
