package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;

import java.util.List;

@Repository
public interface MetadataRepository extends JpaRepository<SeqColMetadataEntity, Long> {

    @Query(value = "select source_id from seqcol_md",nativeQuery = true)
    List<String> findAllSourceIds();

}
