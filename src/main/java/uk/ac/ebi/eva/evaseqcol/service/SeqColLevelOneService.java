package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence operations on the level 1 seqCol objects and their metadata.
 */
@Service
public class SeqColLevelOneService {

    private final SeqColLevelOneRepository repository;

    public SeqColLevelOneService(SeqColLevelOneRepository repository) {
        this.repository = repository;
    }

    /**
     * Add a new Level 1 sequence collection object and save it to the database
     */
    public SeqColLevelOneEntity addSequenceCollectionL1(SeqColLevelOneEntity seqColLevelOne) {
        return repository.save(seqColLevelOne);
    }

    public Optional<SeqColLevelOneEntity> getSeqColLevelOneByDigest(String digest) {
        return repository.findById(digest);
    }

    public boolean existsByDigest(String digest) {
        return repository.existsById(digest);
    }

    public void removeAllSeqCols() {
        repository.deleteAll();
    }

    /**
     * Return a page of level 0 digests, optionally filtered on the level 1 attributes' digests
     * (filter key: attribute name, eg "names", filter value: attribute digest)
     */
    public Page<String> findDigests(Pageable pageable, Map<String, String> filters) {
        if (filters.isEmpty()) {
            return repository.findAllDigests(pageable);
        } else {
            return repository.findAllDigestsByJsonFilters(filters, pageable);
        }
    }

    public List<SeqColMetadataEntity> getMetadataBySeqcolDigest(String digest) {
        return repository.findMetadataBySeqColDigest(digest);
    }

    /**
     * Return true if at least one seqCol has been ingested from the given source (eg: assembly accession)
     */
    public boolean existsBySourceId(String sourceId) {
        return repository.existsMetadataWithSourceId(sourceId);
    }
}
