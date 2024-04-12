package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONIntegerListExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;
import uk.ac.ebi.eva.evaseqcol.utils.JSONStringListExtData;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeqColLevelOneService {

    private final SeqColLevelOneRepository repository;

    private DigestCalculator digestCalculator = new DigestCalculator();

    @Autowired
    public SeqColLevelOneService(SeqColLevelOneRepository repository) {
        this.repository = repository;
    }

    /**
     * Add a new Level 1 sequence collection object and save it to the
     * database*/
    @Transactional
    public Optional<SeqColLevelOneEntity> addSequenceCollectionL1(SeqColLevelOneEntity seqColLevelOne){
        if (repository.existsById(seqColLevelOne.getDigest())) {
            return Optional.empty();
        }
        return Optional.of(
                repository.save(seqColLevelOne)
        );
    }

    public Optional<SeqColLevelOneEntity> getSeqColLevelOneByDigest(String digest){
        return repository.findSeqColLevelOneEntityByDigest(digest);
    }

    public void removeSeqColLevelOneByDigest(String digest) {
        repository.removeSeqColLevelOneEntityByDigest(digest);
    }

    public void removeAllSeqCols() {
        repository.deleteAll();
    }

    public long countSeqColLevelOneEntitiesByDigest(String digest) {
        return repository.countSeqColLevelOneEntitiesByDigest(digest);
    }
    public List<SeqColLevelOneEntity> getAllSeqColLevelOneObjects(){
        return repository.findAll();
    }

    /**
     * Construct a seqCol level 1 entity out of three seqCol level 2 entities that
     * hold names, lengths and sequences objects
     * TODO: Change the signature of this method and make it accept metadata object instead of namingconvention*/
    public SeqColLevelOneEntity constructSeqColLevelOne(List<SeqColExtendedDataEntity<List<String>>> stringListExtendedDataEntities,
                                                        List<SeqColExtendedDataEntity<List<Integer>>> integerListExtendedDataEntities,
                                                        SeqColEntity.NamingConvention convention, String sourceId) throws IOException {
        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        SeqColMetadataEntity metadata = new SeqColMetadataEntity().setNamingConvention(convention)
                                                                  .setSourceIdentifier(sourceId); // TODO: this (metadata object) should be passed in the method parameter

        // Looping over List<String> types
        for (SeqColExtendedDataEntity<List<String>> dataEntity: stringListExtendedDataEntities) {
            switch (dataEntity.getAttributeType()) {
                case names:
                    jsonLevelOne.setNames(dataEntity.getDigest());
                    break;
                case sequences:
                    jsonLevelOne.setSequences(dataEntity.getDigest());
                    break;
                case md5DigestsOfSequences:
                    jsonLevelOne.setMd5DigestsOfSequences(dataEntity.getDigest());
                    break;
                case sortedNameLengthPairs:
                    jsonLevelOne.setSortedNameLengthPairs(dataEntity.getDigest());
                    break;
            }
        }

        // Looping over List<Integer> types
        for (SeqColExtendedDataEntity<List<Integer>> dataEntity: integerListExtendedDataEntities) {
            switch (dataEntity.getAttributeType()) {
                case lengths:
                    jsonLevelOne.setLengths(dataEntity.getDigest());
                    break;
            }
        }

        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        String digest0 = digestCalculator.getSha512Digest(levelOneEntity.toString());
        levelOneEntity.setDigest(digest0);
        levelOneEntity.addMetadata(metadata);
        return levelOneEntity;
    }

    /**
     * Construct a Level 1 seqCol out of a Level 2 seqCol*/
    public SeqColLevelOneEntity constructSeqColLevelOne(
            SeqColLevelTwoEntity levelTwoEntity, SeqColEntity.NamingConvention convention, String sourceId) throws IOException {
        DigestCalculator digestCalculator = new DigestCalculator();
        JSONExtData<List<String>> sequencesExtData = new JSONStringListExtData(levelTwoEntity.getSequences());
        JSONExtData<List<Integer>> lengthsExtData = new JSONIntegerListExtData(levelTwoEntity.getLengths());
        JSONExtData<List<String>> namesExtData = new JSONStringListExtData(levelTwoEntity.getNames());
        JSONExtData<List<String>> md5SequencesExtData = new JSONStringListExtData(levelTwoEntity.getMd5DigestsOfSequences());
        JSONExtData<List<String>> sortedNameLengthPairsData = new JSONStringListExtData(levelTwoEntity.getSortedNameLengthPairs());

        // Sequences
        SeqColExtendedDataEntity<List<String>> sequencesExtEntity = new SeqColExtendedDataEntity<>();
        sequencesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.sequences);
        sequencesExtEntity.setExtendedSeqColData(sequencesExtData);
        sequencesExtEntity.setDigest(digestCalculator.getSha512Digest(sequencesExtData.toString()));
        // Md5Sequences
        SeqColExtendedDataEntity<List<String>> md5SequencesExtEntity = new SeqColExtendedDataEntity<>();
        md5SequencesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);
        md5SequencesExtEntity.setExtendedSeqColData(md5SequencesExtData);
        md5SequencesExtEntity.setDigest(digestCalculator.getSha512Digest(md5SequencesExtData.toString()));
        // Lengths
        SeqColExtendedDataEntity<List<Integer>> lengthsExtEntity = new SeqColExtendedDataEntity<>();
        lengthsExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.lengths);
        lengthsExtEntity.setExtendedSeqColData(lengthsExtData);
        lengthsExtEntity.setDigest(digestCalculator.getSha512Digest(lengthsExtData.toString()));
        // Names
        SeqColExtendedDataEntity<List<String>> namesExtEntity = new SeqColExtendedDataEntity<>();
        namesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.names);
        namesExtEntity.setExtendedSeqColData(namesExtData);
        namesExtEntity.setDigest(digestCalculator.getSha512Digest(namesExtData.toString()));
        //sorted-name-length-pairs
        SeqColExtendedDataEntity<List<String>> sortedNameLengthPairsExtEntity = new SeqColExtendedDataEntity<>();
        sortedNameLengthPairsExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.sortedNameLengthPairs);
        sortedNameLengthPairsExtEntity.setExtendedSeqColData(sortedNameLengthPairsData);
        sortedNameLengthPairsExtEntity.setDigest(digestCalculator.getSha512Digest(sortedNameLengthPairsData.toString()));

        List<SeqColExtendedDataEntity<List<String>>> stringListExtendedDataEntities = Arrays.asList(
                sequencesExtEntity,
                md5SequencesExtEntity,
                namesExtEntity,
                sortedNameLengthPairsExtEntity
        );

        List<SeqColExtendedDataEntity<List<Integer>>> integerListExtendedDataEntities = Arrays.asList(
                lengthsExtEntity
        );

        return constructSeqColLevelOne(stringListExtendedDataEntities,integerListExtendedDataEntities, convention, sourceId);
    }

    /**
     * Construct and return a list of SeqColExtendedDataEntity<List<String>>, given these objects:
     *  extendedAttributesMap : {"extendedLengths" : SeqColExtendedDataEntity<List<Integer>>,
     *                           "extendedSequences" : SeqColExtendedDataEntity<List<String>>,
     *                           "extendedMd5Sequences" : SeqColExtendedDataEntity<List<String>>
     *                           }
     *  extendedNames : SeqColExtendedDataEntity<List<String>>,
     *  extendedSortedNameLengthPair : SeqColExtendedDataEntity<List<String>>.
     *
     *  The returned list contains: ["extendedSequences", "extendedMd5Sequences", "extendedNames", "extendedSortedNameLengthPair"]
     */
    public List<SeqColExtendedDataEntity<List<String>>> constructStringListExtDataEntities(Map<String, Object> extendedAttributesMap,
                                                                                           SeqColExtendedDataEntity<List<String>> extendedNames,
                                                                                           SeqColExtendedDataEntity<List<String>> extendedSortedNameLengthPair) {
        // Sequences
        SeqColExtendedDataEntity<List<String>> sequencesExtEntity =
                (SeqColExtendedDataEntity<List<String>>) extendedAttributesMap.get("extendedSequences");

        // Md5Sequences
        SeqColExtendedDataEntity<List<String>> md5SequencesExtEntity =
                (SeqColExtendedDataEntity<List<String>>) extendedAttributesMap.get("extendedMd5Sequences");

        List<SeqColExtendedDataEntity<List<String>>> stringListExtendedDataEntities = Arrays.asList(
                sequencesExtEntity,
                md5SequencesExtEntity,
                extendedNames,
                extendedSortedNameLengthPair
        );

        return stringListExtendedDataEntities;
    }

    /**
     * Construct and return a list of SeqColExtendedDataEntity<List<Integer>>, given these objects:
     *  extendedAttributesMap : {"extendedLengths" : SeqColExtendedDataEntity<List<Integer>>,
     *                           "extendedSequences" : SeqColExtendedDataEntity<List<String>>,
     *                           "extendedMd5Sequences" : SeqColExtendedDataEntity<List<String>>
     *                           }
     *
     *  The returned list contains: ["extendedLengths"]
     */
    public List<SeqColExtendedDataEntity<List<Integer>>> constructIntegerListExtDataEntities(Map<String, Object> extendedAttributesMap) {

        // lengths
        SeqColExtendedDataEntity<List<Integer>> lengthsExtEntity =
                (SeqColExtendedDataEntity<List<Integer>>) extendedAttributesMap.get("extendedLengths");

        List<SeqColExtendedDataEntity<List<Integer>>> integerListExtendedDataEntities = Arrays.asList(
                lengthsExtEntity
        );

        return integerListExtendedDataEntities;
    }

}