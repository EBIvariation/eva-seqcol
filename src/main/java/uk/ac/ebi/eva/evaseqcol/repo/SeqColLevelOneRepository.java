package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

@Repository
public interface SeqColLevelOneRepository extends JpaRepository<SeqColLevelOneEntity, String> {
    public SeqColLevelOneEntity findSeqColLevelOneEntityByDigest(String digest);

}
