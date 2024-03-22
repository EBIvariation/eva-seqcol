package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadata;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataId;
import uk.ac.ebi.eva.evaseqcol.repo.MetadataRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MetadataService {

    @Autowired
    private MetadataRepository repository;

    public Optional<SeqColMetadata> addMetadata(SeqColMetadata metadata) {
        if (repository.existsById(new SeqColMetadataId(metadata.getSeqColDigest(), metadata.getSourceIdentifier()))){
            return Optional.empty();
        }
        return Optional.of(
                repository.save(metadata)
        );
    }

    /**
     * Return the list of all metadata entries for the seqCol object with the given digest*/
    public Optional<List<SeqColMetadata>> getAllMetadataForSeqColByDigest(String seqColDigest) {
        return Optional.of(
                repository.findAllBySeqColDigest(seqColDigest)
        );
    }
}
