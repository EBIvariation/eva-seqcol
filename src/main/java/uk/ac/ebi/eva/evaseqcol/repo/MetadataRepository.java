package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadata;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataId;

import java.util.List;

@Repository
public interface MetadataRepository extends JpaRepository<SeqColMetadata, SeqColMetadataId> {

    List<SeqColMetadata> findAllBySeqColDigest(String digest);
}
