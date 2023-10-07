package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.exception.ExtendedDataNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColExtendedDataRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColExtendedDataService {

    @Autowired
    private SeqColExtendedDataRepository repository;

    /**
     * Add a seqCol's attribute; names, lengths or sequences, to the database*/
    public Optional<SeqColExtendedDataEntity<?>> addSeqColExtendedData(SeqColExtendedDataEntity<?> seqColExtendedData){
        try {
            SeqColExtendedDataEntity<?> seqCol = repository.save(seqColExtendedData);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Return the list of all extended data objects (level 2) given the seqcol's level 0 digest*/
    public Optional<List<String>> getSeqColExtendedDataByLevelZeroDigest(String digestL0) {
        Optional<List<String>> seqColExtendedDataList =  repository.getSeqColExtendedDataByLevel0Digest(digestL0);
        if (seqColExtendedDataList.isPresent()) {
            return seqColExtendedDataList;
        }else {
            throw new SeqColNotFoundException(digestL0);
        }
    }

    /**
     * Return the extended data object (level 2) that corresponds to the given digest*/
    public Optional<SeqColExtendedDataEntity<?>> getSeqColExtendedDataEntityByDigest(String digest) {
        Optional<SeqColExtendedDataEntity<?>> extendedDataEntity = repository.getSeqColExtendedDataEntityByDigest(digest);
        if (extendedDataEntity.isPresent()) {
            return extendedDataEntity;
        } else {
            throw new ExtendedDataNotFoundException(digest);
        }
    }

    @Transactional
    public List<SeqColExtendedDataEntity<? extends Object>> addAll(List<SeqColExtendedDataEntity<?>> seqColExtendedDataList) {
        return repository.saveAll(seqColExtendedDataList);
    }

    /**
     * Remove all seqCol extended data entities that corresponds to the seqCol
     * that has the given level0Digest*/
    @Transactional
    public void removeSeqColExtendedDataEntities(List<SeqColExtendedDataEntity<?>> extendedDataEntities) {
        for (SeqColExtendedDataEntity<?> entity: extendedDataEntities) {
            removeSeqColExtendedDataEntityByDigest(entity.getDigest());
        }
    }

    /**
     * Remove one extended data entity by its digest.
     * NOTE!: The given digest is not the seqCol level 0 digest*/
    public void removeSeqColExtendedDataEntityByDigest(String digest) {
        repository.removeSeqColExtendedDataEntityByDigest(digest);
    }

    public void removeAllSeqColExtendedEntities() {
        repository.deleteAll();
    }

    /**
     * Return the extendedData object for the given digest*/
    public Optional<SeqColExtendedDataEntity<?>> getExtendedAttributeByDigest(String digest) {
        SeqColExtendedDataEntity<?> dataEntity = repository.findSeqColExtendedDataEntityByDigest(digest);
        return Optional.of(dataEntity);
    }

    /**
     * Return the 5 seqCol extended data objects (names, lengths, sequences, sequencesMD5 and sorted-name-length-pair)
     * of the given assembly and naming convention*/
    // TODO: Change logic
    /*public List<SeqColExtendedDataEntity> constructExtendedSeqColDataList(
            AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
            SeqColEntity.NamingConvention convention) throws IOException {
        SeqColExtendedDataEntity extendedLengthsEntity = SeqColExtendedDataEntity
                .constructSeqColLengthsObject(assemblyEntity);
        SeqColExtendedDataEntity extendedNamesEntity = SeqColExtendedDataEntity
                .constructSeqColNamesObjectByNamingConvention(assemblyEntity, convention);

        return Arrays.asList(
                SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(assemblySequenceEntity),
                extendedNamesEntity,
                extendedLengthsEntity,
                SeqColExtendedDataEntity.constructSeqColSortedNameLengthPairs(extendedNamesEntity, extendedLengthsEntity)
        );
    }*/

}
