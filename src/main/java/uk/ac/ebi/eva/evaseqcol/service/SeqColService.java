package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColComparisonResultEntity;
import uk.ac.ebi.eva.evaseqcol.datasource.NCBISeqColDataSource;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeqColService {

    private final NCBISeqColDataSource ncbiSeqColDataSource;
    private final SeqColLevelOneService levelOneService;
    private final SeqColLevelTwoService levelTwoService;
    private final SeqColExtendedDataService extendedDataService;
    private final DigestCalculator digestCalculator = new DigestCalculator();
    private final Logger logger = LoggerFactory.getLogger(SeqColService.class);

    @Autowired
    public SeqColService(NCBISeqColDataSource ncbiSeqColDataSource, SeqColLevelOneService levelOneService,
                         SeqColLevelTwoService levelTwoService, SeqColExtendedDataService extendedDataService) {
        this.ncbiSeqColDataSource = ncbiSeqColDataSource;
        this.levelOneService = levelOneService;
        this.levelTwoService = levelTwoService;
        this.extendedDataService = extendedDataService;
    }

    @Transactional
    /**
     * Insert full sequence collection data (level 1 entity, and the exploded data entities)
     * @return  The level 0 digest of the whole seqCol object*/
    public Optional<String> addFullSequenceCollection(SeqColLevelOneEntity levelOneEntity, List<SeqColExtendedDataEntity> extendedSeqColDataList) {
        long numSeqCols = levelOneService.countSeqColLevelOneEntitiesByDigest(levelOneEntity.getDigest());
        if (numSeqCols > 0) {
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        } else {
            SeqColLevelOneEntity levelOneEntity1 = levelOneService.addSequenceCollectionL1(levelOneEntity).get();
            extendedDataService.addAll(extendedSeqColDataList);

            return Optional.of(levelOneEntity1.getDigest());
        }
    }

    public Optional<? extends SeqColEntity> getSeqColByDigestAndLevel(String digest, Integer level) {
       if (level == 1) {
           return levelOneService.getSeqColLevelOneByDigest(digest);
       } else if (level == 2) {
            Optional<SeqColLevelOneEntity> seqColLevelOne = levelOneService.getSeqColLevelOneByDigest(digest);
            SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity().setDigest(digest);
            // Retrieving sequences
            String sequencesDigest = seqColLevelOne.get().getSeqColLevel1Object().getSequences();
            JSONExtData extendedSequences = extendedDataService.getSeqColExtendedDataEntityByDigest(sequencesDigest).get().getExtendedSeqColData();
            //Retrieving md5 sequences
           String sequencesMd5Digest = seqColLevelOne.get().getSeqColLevel1Object().getMd5DigestsOfSequences();
           JSONExtData extendedMd5Sequnces = extendedDataService.getSeqColExtendedDataEntityByDigest(sequencesMd5Digest).get().getExtendedSeqColData();
           // Retrieving legnths
           String lengthsDigest = seqColLevelOne.get().getSeqColLevel1Object().getLengths();
           JSONExtData extendedLengths = extendedDataService.getSeqColExtendedDataEntityByDigest(lengthsDigest).get().getExtendedSeqColData();
           // Retrieving names
           String namesDigest = seqColLevelOne.get().getSeqColLevel1Object().getNames();
           JSONExtData extendedNames = extendedDataService.getSeqColExtendedDataEntityByDigest(namesDigest).get().getExtendedSeqColData();

           levelTwoEntity.setSequences(extendedSequences.getObject());
           levelTwoEntity.setMd5Sequences(extendedMd5Sequnces.getObject());
           levelTwoEntity.setLengths(extendedLengths.getObject());
           levelTwoEntity.setNames(extendedNames.getObject());

           return Optional.of(levelTwoEntity);
       } else {
           logger.error("Could not find any seqCol object with digest " + digest + " on level " + level);
           return Optional.empty();
       }
    }

    /**
     * Fetch and insert all possible seqCol objects for the given assembly accession.
     * NOTE: All possible seqCol objects means with all possible/provided naming conventions that could be found in the
     * assembly report.
     * Return the list of level 0 digests of the inserted seqcol objects*/
    public List<String> fetchAndInsertAllSeqColByAssemblyAccession(
            String assemblyAccession) throws IOException, DuplicateSeqColException {
        List<String> insertedSeqColDigests = new ArrayList<>();
        Optional<Map<String, List<SeqColExtendedDataEntity>>> seqColDataMap = ncbiSeqColDataSource
                .getAllPossibleSeqColExtendedData(assemblyAccession);
        if (!seqColDataMap.isPresent()) {
            logger.warn("No seqCol data corresponding to assemblyAccession " + assemblyAccession + " could be found on NCBI datasource");
            // TODO RETURN SOMETHING
        }
        List<SeqColExtendedDataEntity> possibleSequencesNamesList = seqColDataMap.get().get("namesAttributes");
        List<SeqColExtendedDataEntity> sameValueAttributeList = seqColDataMap.get().get("sameValueAttributes");
        for (SeqColExtendedDataEntity extendedNamesEntity: possibleSequencesNamesList) {
            List<SeqColExtendedDataEntity> seqColExtendedDataEntities = new ArrayList<>(sameValueAttributeList);
            seqColExtendedDataEntities.add(extendedNamesEntity);
            SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(seqColExtendedDataEntities, extendedNamesEntity.getNamingConvention());
            Optional<String> seqColDigest = insertSeqColL1AndL2(levelOneEntity, seqColExtendedDataEntities);
            if (seqColDigest.isPresent()) {
                logger.info(
                        "Successfully inserted seqCol for assembly Accession " + assemblyAccession + " with naming convention " + extendedNamesEntity.getNamingConvention());
                insertedSeqColDigests.add(seqColDigest.get());
            } else {
                logger.warn("Could not insert seqCol for assembly Accession " + assemblyAccession + " with naming convention " + extendedNamesEntity.getNamingConvention());
            }
        }
        return insertedSeqColDigests;
    }

    @Transactional
    /**
     * Insert the given Level 1 seqCol entity and its corresponding extended level 2 data (names, lengths, sequences, ...)
     * Return the level 0 digest of the inserted seqCol*/
    public Optional<String> insertSeqColL1AndL2(SeqColLevelOneEntity levelOneEntity,
                                    List<SeqColExtendedDataEntity> seqColExtendedDataEntities) {
        if (isSeqColL1Present(levelOneEntity)) {
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        } else {
            Optional<String> level0Digest = addFullSequenceCollection(levelOneEntity, seqColExtendedDataEntities);
            return level0Digest;
        }
    }

    private boolean isSeqColL1Present(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColLevelOneEntity> existingSeqCol = levelOneService.getSeqColLevelOneByDigest(levelOneEntity.getDigest());
        return existingSeqCol.isPresent();
    }

    /**
     * Compare two seqCol L2 objects*/
    public SeqColComparisonResultEntity compareSeqCols(
            String seqColADigest, SeqColLevelTwoEntity seqColAEntity, String seqColBDigest, SeqColLevelTwoEntity seqColBEntity) {

        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();

        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);

        // "arrays" attribute
        try {
            Field[] seqColAFields = seqColAEntity.getClass().getDeclaredFields();
            Field[] seqColBFields = seqColBEntity.getClass().getDeclaredFields();
            List<String> seqColAFieldNames = getFieldsNames(seqColAFields);
            List<String> seqColBFieldNames = getFieldsNames(seqColBFields);
            List<String> seqColAUniqueFields = getUniqueFields(seqColAFieldNames, seqColBFieldNames);
            List<String> seqColBUniqueFields = getUniqueFields(seqColBFieldNames, seqColAFieldNames);
            List<String> seqColCommonFieldNames = getCommonFieldsDistinct(seqColAFieldNames, seqColBFieldNames);
            comparisonResult.putIntoArrays("a-only", seqColAUniqueFields);
            comparisonResult.putIntoArrays("b-only", seqColBUniqueFields);
            comparisonResult.putIntoArrays("a-and-b", seqColCommonFieldNames);

        } catch (NullPointerException e) {
            // TODO LOGGER MESSAGE
            System.out.println("Either seqColA or seqColB is not initialized");
            throw new RuntimeException(e.getMessage());
        }
        // "elements"
        List<String> seqColALengths = seqColAEntity.getLengths();
        List<String> seqColBLengths = seqColBEntity.getLengths();
        List<String> seqColANames = seqColAEntity.getNames();
        List<String> seqColBNames = seqColBEntity.getNames();
        List<String> seqColASequences = seqColAEntity.getSequences();
        List<String> seqColBSequences = seqColBEntity.getSequences();

        // "elements" attribute | "total"
        Integer seqColATotal = seqColAEntity.getSequences().size();
        Integer seqColBTotal = seqColBEntity.getSequences().size();
        comparisonResult.putIntoElements("total", "a", seqColATotal);
        comparisonResult.putIntoElements("total", "b", seqColBTotal);

        // "elements" attribute | "a-and-b"
        Integer commonLengthsCount = getCommonFieldsDistinct(seqColALengths, seqColBLengths).size();
        Integer commonNamesCount = getCommonFieldsDistinct(seqColANames, seqColBNames).size();
        Integer commonSequencesCount = getCommonFieldsDistinct(seqColASequences, seqColBSequences).size();
        comparisonResult.putIntoElements("a-and-b", "lengths", commonLengthsCount);
        comparisonResult.putIntoElements("a-and-b", "names", commonNamesCount);
        comparisonResult.putIntoElements("a-and-b", "sequences", commonSequencesCount);

        // "elements" attribute | "a-and-b-same-order"
        // LENGTHS
        if (lessThanTwoOverlappingElements(seqColALengths, seqColBLengths) || unbalancedDuplicatesPresent(seqColALengths, seqColBLengths)) {
            System.out.println("More than two overlapping elements: !!!");
            comparisonResult.putIntoElements("a-and-b-same-order", "lengths", null);
        } else {
            System.out.println("seqColALengths Size: " + seqColALengths.size() + " SECOND Element: " + seqColALengths.get(1));
            System.out.println("seqColBLengths Size: " + seqColBLengths.size() + " SECOND Element: " + seqColBLengths.get(1));
            boolean lengthsSameOrder = seqColALengths.equals(seqColBLengths);
            comparisonResult.putIntoElements("a-and-b-same-order", "lengths", lengthsSameOrder);
        }
        // NAMES
        if (lessThanTwoOverlappingElements(seqColANames, seqColBNames) || unbalancedDuplicatesPresent(seqColANames, seqColBNames)) {
            comparisonResult.putIntoElements("a-and-b-same-order", "names", null);
        } else {
            boolean namesSameOrder = seqColANames.equals(seqColBNames);
            comparisonResult.putIntoElements("a-and-b-same-order", "names", namesSameOrder);
        }
        // SEQUENCES
        if (lessThanTwoOverlappingElements(seqColASequences, seqColBSequences) || unbalancedDuplicatesPresent(seqColASequences, seqColBSequences)) {
            comparisonResult.putIntoElements("a-and-b-same-order", "sequences", null);
        } else {
            boolean sequencesSameOrder = seqColASequences.equals(seqColBSequences);
            comparisonResult.putIntoElements("a-and-b-same-order", "sequences", sequencesSameOrder);
        }

        return comparisonResult;
    }

    /**
     * Compare two seqCols given a level 0 digest of the first one and the L2 seqCol object of the second
     * */
    public SeqColComparisonResultEntity compareSeqCols(
            String seqColADigest, SeqColLevelTwoEntity seqColBEntity) throws IOException {

        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);
        if (!seqColAEntity.isPresent()) {
            throw new SeqColNotFoundException(seqColADigest);
        }
        SeqColLevelOneEntity seqColBL1 = levelOneService.constructSeqColLevelOne(seqColBEntity, null);
        String seqColBDigest = digestCalculator.getSha512Digest(seqColBL1.toString());
        return compareSeqCols(seqColADigest, seqColAEntity.get(), seqColBDigest, seqColBEntity);

    }

    /**
     * Compare two seqCols given their level 0 digests*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, String seqColBDigest) throws NoSuchFieldException {
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);
        Optional<SeqColLevelTwoEntity> seqColBEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColBDigest);
        if (!seqColAEntity.isPresent()) {
            throw new SeqColNotFoundException(seqColADigest);
        }
        if (!seqColBEntity.isPresent()) {
            throw new SeqColNotFoundException(seqColBDigest);
        }
        return compareSeqCols(seqColADigest, seqColAEntity.get(), seqColBDigest, seqColBEntity.get());
    }

    /**
     * Retrieve the field names out the Fields array*/
    public List<String> getFieldsNames(Field[] fields) {
        List<String> fieldNames = new ArrayList<>();
        for (Field field: fields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    /**
     * Return fields of list1 that are not contained in list2*/
    public List<String> getUniqueFields(List<String> list1, List<String> list2) {
        List<String> tempList = new ArrayList<>(list1);
        tempList.removeAll(list2);
        return tempList;
    }

    /**
     * Return the list of the common elements between seqColAFields and seqColBFields (with no duplicates)*/
    public List<String> getCommonFieldsDistinct(List<String> seqColAFields, List<String> seqColBFields) {
        List<String> commonFields = new ArrayList<>(seqColAFields);
        commonFields.retainAll(seqColBFields);
        List<String> commonFieldsDistinct = commonFields.stream().distinct().collect(Collectors.toList());
        return commonFieldsDistinct;
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean lessThanTwoOverlappingElements(List<String> list1, List<String> list2) {
        logger.info("less than two overlapping elements check: " + getCommonFieldsDistinct(list1, list2).size());
        return getCommonFieldsDistinct(list1, list2).size() < 2;
    }

    /**
     * Return true if there are unbalanced duplicates present
     * Example 1: A = [1, 2, 3, 4]
     *            B = [1, 2, 3, 4, 5, 6]
     *            No duplicates (balanced duplicates)
     *
     * Example 2: A = [1, 2, 2, 3, 4]
     *            B = [1, 2 ,3, 4]
     *            A' = [1, 2, 2, 3, 4]
     *            B' = [1, 2, 3, 4]
     *            Common elements:     {
     *                                   1: {A:1, B:1},
     *                                   2: {A:2, B:1}, -> Unbalanced duplicates
     *                                   3: {A:1, B:1},
     *                                   4: {A:1, B:1}
     *                                  }
     *            Unbalanced duplicates
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean unbalancedDuplicatesPresent(List<String> listA, List<String> listB) {
        List<String> commonElements = getCommonFieldsDistinct(listA, listB);
        Map<String, Map<String, Integer>> duplicatesCountMap = new HashMap<>();
        for (String element: commonElements) {
            Map<String, Integer> elementCount = new HashMap<>(); // Track the number of duplicates in each list for the same element
            elementCount.put("a", Collections.frequency(listA, element));
            elementCount.put("b", Collections.frequency(listB, element));
            duplicatesCountMap.put(element, elementCount);
        }
        for (Map<String, Integer> countMap: duplicatesCountMap.values()) {
            if (!Objects.equals(countMap.get("a"), countMap.get("b"))) {
                return true;
            }
        }
        return false;
    }
}
