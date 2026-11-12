package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.exception.ExtendedDataNotFoundException;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColExtendedDataRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations on the extended (level 2) seqCol attributes.
 */
@Service
public class SeqColExtendedDataService {

    private final SeqColExtendedDataRepository repository;

    public SeqColExtendedDataService(SeqColExtendedDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Add a seqCol's attribute; names, lengths, sequences, etc. to the database
     */
    public <T> SeqColExtendedDataEntity<T> addSeqColExtendedData(SeqColExtendedDataEntity<T> seqColExtendedData) {
        return repository.save(seqColExtendedData);
    }

    @Transactional
    public List<SeqColExtendedDataEntity<?>> addAll(List<SeqColExtendedDataEntity<?>> seqColExtendedDataList) {
        return repository.saveAll(seqColExtendedDataList);
    }

    /**
     * Return the extended data object for the given digest
     */
    public <T> Optional<SeqColExtendedDataEntity<T>> findByDigest(String digest) {
        return repository.findByDigest(digest);
    }

    /**
     * Return the extended data object for the given digest
     * @throws ExtendedDataNotFoundException if it does not exist
     */
    public <T> SeqColExtendedDataEntity<T> getByDigest(String digest) {
        return this.<T>findByDigest(digest).orElseThrow(() -> new ExtendedDataNotFoundException(digest));
    }

    public void removeAllSeqColExtendedEntities() {
        repository.deleteAll();
    }
}
