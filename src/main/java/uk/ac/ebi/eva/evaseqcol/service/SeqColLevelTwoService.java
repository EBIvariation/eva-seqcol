package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.util.ArrayList;
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
            logger.error("seqCol with digest: " + digest + " doesn't exists !");
            return Optional.empty();
        }
        // 2 DATABASE LOOKUPS
        List<SeqColExtendedDataEntity<?>> extendedIntegerAttributes = getIntegerExtendedAttributes(levelOneEntity.get());
        List<SeqColExtendedDataEntity<?>> extendedStringAttributes = getStringExtendedAttributes(levelOneEntity.get());
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        for (SeqColExtendedDataEntity<?> extendedData: extendedStringAttributes) {
            switch (extendedData.getAttributeType()) {
                case names:
                    levelTwoEntity.setNames((List<String>) extendedData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences((List<String>) extendedData.getExtendedSeqColData().getObject());
                    break;
                case md5DigestsOfSequences:
                    levelTwoEntity.setMd5DigestsOfSequences(
                            (List<String>) extendedData.getExtendedSeqColData().getObject());
                    break;
                case sortedNameLengthPairs:
                    levelTwoEntity.setSortedNameLengthPairs(
                            (List<String>) extendedData.getExtendedSeqColData().getObject());
                    break;
            }
        }

        for (SeqColExtendedDataEntity<?> extendedIntData: extendedIntegerAttributes) {
            levelTwoEntity.setLengths((List<Integer>) extendedIntData.getExtendedSeqColData().getObject());
        }
        return Optional.of(levelTwoEntity);
    }

    /**
     * Return the list of the extended (exploded) seqCol attributes; names, lengths and sequences
     * Given the corresponding seqCol level 1 object*/
    private List<SeqColExtendedDataEntity<?>> getStringExtendedAttributes(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColExtendedDataEntity<?>> extendedSequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getSequences());
        if (!extendedSequences.isPresent()) {
            throw new RuntimeException("Extended sequences data with digest: " + levelOneEntity.getSeqColLevel1Object().getSequences() + " not found");
        }
        extendedSequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.sequences);

        Optional<SeqColExtendedDataEntity<?>> extendedMD5Sequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences());
        if (!extendedMD5Sequences.isPresent()) {
            throw new RuntimeException("Extended md5 sequences data with digest:" + levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences() + " not found");
        }
        extendedMD5Sequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);


        Optional<SeqColExtendedDataEntity<?>> extendedNames = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getNames());
        if (!extendedNames.isPresent()) {
            throw new RuntimeException("Extended names data with digest: " + levelOneEntity.getSeqColLevel1Object().getNames() + " not found");
        }
        extendedNames.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.names);

        Optional<SeqColExtendedDataEntity<?>> extendedSortedNameLengthPairs = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getSortedNameLengthPairs());
        if (!extendedSortedNameLengthPairs.isPresent()) {
            throw new RuntimeException("Extended names data with digest: " + levelOneEntity.getSeqColLevel1Object().getNames() + " not found");
        }
        extendedSortedNameLengthPairs.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.sortedNameLengthPairs);

        return Arrays.asList(
                extendedSequences.get(),
                extendedMD5Sequences.get(),
                //extendedLengths.get(),
                extendedNames.get(),
                extendedSortedNameLengthPairs.get()
        );
    }

    private List<SeqColExtendedDataEntity<?>> getIntegerExtendedAttributes(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColExtendedDataEntity<?>> extendedLengths = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getLengths());
        if (!extendedLengths.isPresent()) {
            throw new RuntimeException("Extended lengths data with digest: " + levelOneEntity.getSeqColLevel1Object().getLengths() + " not found");
        }
        extendedLengths.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.lengths);

        List<SeqColExtendedDataEntity<?>> extendedIntAttributesList = new ArrayList<>();
        extendedIntAttributesList.add(extendedLengths.get());
        return extendedIntAttributesList;
    }

    public SeqColLevelTwoEntity constructSeqColL2(String level0Digest,
                                                  List<SeqColExtendedDataEntity<?>> stringExtendedDataEntities,
                                                  List<SeqColExtendedDataEntity<?>> integerExtendedDataEntities) {
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        levelTwoEntity.setDigest(level0Digest);
        for (SeqColExtendedDataEntity<?> strExtendedData: stringExtendedDataEntities) {
            switch (strExtendedData.getAttributeType()) {
                case names:
                    levelTwoEntity.setNames((List<String>) strExtendedData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences((List<String>) strExtendedData.getExtendedSeqColData().getObject());
                    break;
                case md5DigestsOfSequences:
                    levelTwoEntity.setMd5DigestsOfSequences(
                            (List<String>) strExtendedData.getExtendedSeqColData().getObject());
                    break;
                case sortedNameLengthPairs:
                    levelTwoEntity.setSortedNameLengthPairs(
                            (List<String>) strExtendedData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        for (SeqColExtendedDataEntity<?> intExtendedData: integerExtendedDataEntities) {
            levelTwoEntity.setLengths((List<Integer>) intExtendedData.getExtendedSeqColData().getObject());
        }
        return levelTwoEntity;
    }
}
