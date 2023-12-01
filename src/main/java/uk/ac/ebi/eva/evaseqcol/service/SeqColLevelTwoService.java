package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColLevelTwoService {

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    private final Logger logger = LoggerFactory.getLogger(SeqColLevelTwoService.class);

    /**
     * Make 2 recursive lookups to retrieve and construct the seqCol level 2 object
     * @param digest: level 0 seqCol digest*/
    public Optional<SeqColLevelTwoEntity> getSeqColLevelTwoByDigest(String digest) {
        // 1 DATABASE LOOKUP
        Optional<SeqColLevelOneEntity> levelOneEntity = levelOneService.getSeqColLevelOneByDigest(digest);
        if (!levelOneEntity.isPresent()) {
            logger.warn("seqCol with digest: " + digest + " doesn't exists !");
            return Optional.empty();
        }
        // 2 DATABASE LOOKUPS
        List<SeqColExtendedDataEntity<List<String>>> extendedStringTypeAttributes = getStringTypeExtendedAttributes(levelOneEntity.get());
        List<SeqColExtendedDataEntity<List<Integer>>> extendedIntegerTypeAttributes = getIntegerTypeExtendedAttributes(levelOneEntity.get());
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        for (SeqColExtendedDataEntity<List<String>> extendedStringTypeData: extendedStringTypeAttributes) {
            switch (extendedStringTypeData.getAttributeType()) {
                case names:
                    levelTwoEntity.setNames(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case md5DigestsOfSequences:
                    levelTwoEntity.setMd5DigestsOfSequences(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case sortedNameLengthPairs:
                    levelTwoEntity.setSortedNameLengthPairs(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        for (SeqColExtendedDataEntity<List<Integer>> extendedIntegerTypeData: extendedIntegerTypeAttributes) {
            switch (extendedIntegerTypeData.getAttributeType()) {
                case lengths:
                    levelTwoEntity.setLengths(extendedIntegerTypeData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        return Optional.of(levelTwoEntity);
    }

    /**
     * Return the list of the extended (exploded) seqCol attributes with string type elements (List<String>)
     * ; names, lengths and sequences
     * Given the corresponding seqCol level 1 object*/
    private List<SeqColExtendedDataEntity<List<String>>> getStringTypeExtendedAttributes(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColExtendedDataEntity<List<String>>> extendedSequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getSequences());
        if (!extendedSequences.isPresent()) {
            throw new RuntimeException("Extended sequences data with digest: " + levelOneEntity.getSeqColLevel1Object().getSequences() + " not found");
        }
        extendedSequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.sequences);

        Optional<SeqColExtendedDataEntity<List<String>>> extendedMD5Sequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences());
        if (!extendedMD5Sequences.isPresent()) {
            throw new RuntimeException("Extended md5 sequences data with digest:" + levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences() + " not found");
        }
        extendedMD5Sequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);

        Optional<SeqColExtendedDataEntity<List<String>>> extendedNames = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getNames());
        if (!extendedNames.isPresent()) {
            throw new RuntimeException("Extended names data with digest: " + levelOneEntity.getSeqColLevel1Object().getNames() + " not found");
        }
        extendedNames.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.names);

        Optional<SeqColExtendedDataEntity<List<String>>> extendedSortedNameLengthPairs = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getSortedNameLengthPairs());
        if (!extendedSortedNameLengthPairs.isPresent()) {
            throw new RuntimeException("Extended names data with digest: " + levelOneEntity.getSeqColLevel1Object().getNames() + " not found");
        }
        extendedSortedNameLengthPairs.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.sortedNameLengthPairs);

        return Arrays.asList(
                extendedSequences.get(),
                extendedMD5Sequences.get(),
                extendedNames.get(),
                extendedSortedNameLengthPairs.get()
        );
    }

    private List<SeqColExtendedDataEntity<List<Integer>>> getIntegerTypeExtendedAttributes(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColExtendedDataEntity<List<Integer>>> extendedLengths = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getLengths());
        if (!extendedLengths.isPresent()) {
            throw new RuntimeException("Extended lengths data with digest: " + levelOneEntity.getSeqColLevel1Object().getLengths() + " not found");
        }
        extendedLengths.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.lengths);
        return Arrays.asList(
                extendedLengths.get()
        );
    }

    public SeqColLevelTwoEntity constructSeqColL2(String level0Digest,
                                                  List<SeqColExtendedDataEntity<List<String>>> extendedStringTypeDataEntities,
                                                  List<SeqColExtendedDataEntity<List<Integer>>> extendedIntegerTypeDataEntities) {
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        levelTwoEntity.setDigest(level0Digest);
        for (SeqColExtendedDataEntity<List<String>> extendedStringTypeData: extendedStringTypeDataEntities) {
            switch (extendedStringTypeData.getAttributeType()) {
                case names:
                    levelTwoEntity.setNames(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case md5DigestsOfSequences:
                    levelTwoEntity.setMd5DigestsOfSequences(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
                case sortedNameLengthPairs:
                    levelTwoEntity.setSortedNameLengthPairs(extendedStringTypeData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        for (SeqColExtendedDataEntity<List<Integer>> extendedIntegerTypeData: extendedIntegerTypeDataEntities) {
            switch (extendedIntegerTypeData.getAttributeType()) {
                case lengths:
                    levelTwoEntity.setLengths(extendedIntegerTypeData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        return levelTwoEntity;
    }
}
