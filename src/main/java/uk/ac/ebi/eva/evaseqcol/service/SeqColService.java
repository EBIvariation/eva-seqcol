package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.ac.ebi.eva.evaseqcol.exception.UnableToLoadServiceInfoException;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.SeqColMapConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeqColService {

    @Value("${service.info.file.path}")
    private String SERVICE_INFO_FILE_PATH;
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
            logger.error("SeqCol with digest " + levelOneEntity.getDigest() + " already exists !");
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        } else {
            SeqColLevelOneEntity levelOneEntity1 = levelOneService.addSequenceCollectionL1(levelOneEntity).get();
            extendedDataService.addAll(extendedSeqColDataList);
            logger.info("Added seqCol object with digest: " + levelOneEntity1.getDigest());
            return Optional.of(levelOneEntity1.getDigest());
        }
    }

    public Optional<? extends SeqColEntity> getSeqColByDigestAndLevel(String digest, Integer level) {
       if (level == 1) {
           return levelOneService.getSeqColLevelOneByDigest(digest);
       } else if (level == 2) {
            Optional<SeqColLevelOneEntity> seqColLevelOne = levelOneService.getSeqColLevelOneByDigest(digest);
            if (!seqColLevelOne.isPresent()) {
                logger.error("No seqCol corresponding to digest " + digest + " could be found in the db");
                throw new SeqColNotFoundException(digest);
            }
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
           // Retrieving sortedNameLengthPairs
           String sortedNameLengthPairsDigest = seqColLevelOne.get().getSeqColLevel1Object().getSortedNameLengthPairs();
           JSONExtData extendedSortedNameLengthPairs = extendedDataService.
                   getSeqColExtendedDataEntityByDigest(sortedNameLengthPairsDigest).get().getExtendedSeqColData();

           levelTwoEntity.setSequences(extendedSequences.getObject());
           levelTwoEntity.setMd5DigestsOfSequences(extendedMd5Sequnces.getObject());
           levelTwoEntity.setLengths(extendedLengths.getObject());
           levelTwoEntity.setNames(extendedNames.getObject());
           levelTwoEntity.setSortedNameLengthPairs(extendedSortedNameLengthPairs.getObject());

           return Optional.of(levelTwoEntity);
       } else {
           logger.error("Could not find any seqCol object with digest " + digest + " on level " + level);
           return Optional.empty();
       }
    }

    /**
     * Return the service info entity in a Map<String,Object> format
     * @see 'https://seqcol.readthedocs.io/en/dev/specification/#21-service-info'
     * for more details about the service-info*/
    public Map<String, Object> getServiceInfo() {
        try {
            Map<String, Object> serviceInfoMap = SeqColMapConverter.jsonToMap(SERVICE_INFO_FILE_PATH);
            return serviceInfoMap;
        } catch (IOException e) {
            throw new UnableToLoadServiceInfoException(SERVICE_INFO_FILE_PATH);
        }
    }

    /**
     * Full remove of the seqCol object (level one and its extended data)*/
    @Transactional
    public void deleteFullSeqCol(String digest, List<SeqColExtendedDataEntity> extendedDataEntities) {
        levelOneService.removeSeqColLevelOneByDigest(digest);
        extendedDataService.removeSeqColExtendedDataEntities(extendedDataEntities);
    }

    /**
     * Remove all seqCol entities (level 1 and the extended entities) from the database*/
    @Transactional
    public void removeAllSeqCol() {
        logger.info("Removing all seqCol objects from the database !");
        levelOneService.removeAllSeqCols();
        extendedDataService.removeAllSeqColExtendedEntities();
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
            return insertedSeqColDigests;
        }
        List<SeqColExtendedDataEntity> possibleSequencesNamesList = seqColDataMap.get().get("namesAttributes");
        List<SeqColExtendedDataEntity> sameValueAttributeList = seqColDataMap.get().get("sameValueAttributes");
        for (SeqColExtendedDataEntity extendedNamesEntity: possibleSequencesNamesList) {
            List<SeqColExtendedDataEntity> seqColExtendedDataEntities = new ArrayList<>(sameValueAttributeList);
            SeqColExtendedDataEntity extendedLengthsEntity = retrieveExtendedLengthEntity(seqColExtendedDataEntities);
            SeqColExtendedDataEntity seqColSortedNameLengthPairEntity = SeqColExtendedDataEntity.
                    constructSeqColSortedNameLengthPairs(extendedNamesEntity, extendedLengthsEntity);
            seqColExtendedDataEntities.add(extendedNamesEntity);
            seqColExtendedDataEntities.add(seqColSortedNameLengthPairEntity);
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

    /**
     * Return the extended data entity that corresponds to the seqCol lengths attribute*/
    public SeqColExtendedDataEntity retrieveExtendedLengthEntity(List<SeqColExtendedDataEntity> extendedDataEntities) {
        for (SeqColExtendedDataEntity entity: extendedDataEntities) {
            if (entity.getAttributeType() == SeqColExtendedDataEntity.AttributeType.lengths) {
                return entity;
            }
        }
        return null;
    }
    @Transactional
    /**
     * Insert the given Level 1 seqCol entity and its corresponding extended level 2 data (names, lengths, sequences, ...)
     * Return the level 0 digest of the inserted seqCol*/
    public Optional<String> insertSeqColL1AndL2(SeqColLevelOneEntity levelOneEntity,
                                    List<SeqColExtendedDataEntity> seqColExtendedDataEntities) {
        if (isSeqColL1Present(levelOneEntity)) {
            logger.error("Could not insert seqCol with digest " + levelOneEntity.getDigest() + ". Already exists !");
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
     * Compare two seqCols given their level 0 digests (Used to compare two local seqCol objects)*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, String seqColBDigest) throws NoSuchFieldException {
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);
        Optional<SeqColLevelTwoEntity> seqColBEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColBDigest);
        if (!seqColAEntity.isPresent()) {
            logger.error("No seqCol corresponding to digest " + seqColADigest + " could be found in the db");
            throw new SeqColNotFoundException(seqColADigest);
        }
        if (!seqColBEntity.isPresent()) {
            logger.error("No seqCol corresponding to digest " + seqColBDigest + " could be found in the db");
            throw new SeqColNotFoundException(seqColBDigest);
        }
        return compareSeqCols(seqColADigest, seqColAEntity.get(), seqColBDigest, seqColBEntity.get());
    }

    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, SeqColLevelTwoEntity seqColAEntity,
                                                       String seqColBDigest, SeqColLevelTwoEntity seqColBEntity) {
        Map<String, List<String>> seqColAMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity);
        Map<String, List<String>> seqColBMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColBEntity);
        return compareSeqCols(seqColADigest, seqColAMap, seqColBDigest, seqColBMap);
    }

    /**
     * Compare two seqCol objects; an already saved one: seqColA, with pre-defined attributes,
     * and undefined one: seqColB (unknown attributes). BE CAREFUL: the order of the arguments matters!!.
     * Note: of course the seqCol minimal required attributes should be present*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, Map<String, List<String>> seqColBEntityMap) throws IOException {
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);

        // Calculating the seqColB level 0 digest
        String seqColBDigest = calculateSeqColLevelTwoMapDigest(seqColBEntityMap);

        // Converting seqColA object into a Map in order to handle attributes generically (
        Map<String, List<String>> seqColAEntityMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity.get());

        return compareSeqCols(seqColADigest, seqColAEntityMap, seqColBDigest, seqColBEntityMap);
    }

    /**
     * Compare two seqCol L2 objects*/
    public SeqColComparisonResultEntity compareSeqCols(
            String seqColADigest, Map<String,List<String>> seqColAEntityMap, String seqColBDigest, Map<String, List<String>> seqColBEntityMap) {

        logger.info("Comparing seqCol " + seqColADigest + " and seqCol " + seqColBDigest);
        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();

        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);


        // Getting each seqCol object's attributes list
        Set<String> seqColAAttributeSet = seqColAEntityMap.keySet(); // The set of attributes in seqColAEntity
        Set<String> seqColBAttributeSet = seqColBEntityMap.keySet(); // The set of attributes in seqColBEntity
        List<String> seqColAAttributesList = new ArrayList<>(seqColAAttributeSet); // For better data manipulation
        List<String> seqColBAttributesList = new ArrayList<>(seqColBAttributeSet); // For better data manipulation

        // "arrays" attribute
        List<String> seqColAUniqueAttributes = getUniqueElements(seqColAAttributesList, seqColBAttributesList);
        List<String> seqColBUniqueAttributes = getUniqueElements(seqColBAttributesList, seqColAAttributesList);
        List<String> seqColCommonAttributes = getCommonElementsDistinct(seqColAAttributesList, seqColBAttributesList);
        comparisonResult.putIntoArrays("a-only", seqColAUniqueAttributes);
        comparisonResult.putIntoArrays("b-only", seqColBUniqueAttributes);
        comparisonResult.putIntoArrays("a-and-b", seqColCommonAttributes);

        // "elements" attribute | "total"
        Integer seqColATotal = seqColAEntityMap.get("lengths").size();
        Integer seqColBTotal = seqColBEntityMap.get("lengths").size();
        comparisonResult.putIntoElements("total", "a", seqColATotal);
        comparisonResult.putIntoElements("total", "b", seqColBTotal);

        // "elements" attribute | "a-and-b"
        List<String> commonSeqColAttributesValues = getCommonElementsDistinct(seqColAAttributesList, seqColBAttributesList); // eg: ["sequences", "lengths", ...]
        for (String element: commonSeqColAttributesValues) {
            Integer commonElementsCount = getCommonElementsCount(seqColAEntityMap.get(element), seqColBEntityMap.get(element));
            comparisonResult.putIntoElements("a-and-b", element, commonElementsCount);
        }

        // "elements" attribute | "a-and-b-same-order"
        for (String attribute: commonSeqColAttributesValues) {
            if (lessThanTwoOverlappingElements(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))
                    || unbalancedDuplicatesPresent(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))){
                comparisonResult.putIntoElements("a-and-b-same-order", attribute, null);
            } else {
                boolean attributeSameOrder = check_A_And_B_Same_Order(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute));
                comparisonResult.putIntoElements("a-and-b-same-order", attribute, attributeSameOrder);
            }
        }

        return comparisonResult;
    }

    /**
     * Check whether the array of elements of elementsA are in the same order as the ones in elementsB.
     * Example 1:
     *      A = ["ch1", "B", "ch2", "ch3"],
     *      B = ["ch1", "A", "ch2", "ch3"],
     *      Common = ["ch1", "ch2", "ch3"] # Common elements between A and B
     *      ==> A: indexOf("ch1") < indexOf("ch2") < indexOf("ch3") and B: indexOf("ch1") < indexOf("ch2") < indexOf("ch3")
     *      ==> Same order elements
     * Example 1:
     *      A = ["ch1", "B", "ch2", "ch3"]
     *      B = ["A", "ch1", "ch2", "ch3"]
     *      Common = ["ch1", "ch2", "ch3"]
     *      ==> A: indexOf("ch1") < indexOf("ch2") < indexOf("ch3") and B: indexOf("ch1") < indexOf("ch2") < indexOf("ch3")
     *      ==> Same order elements
     * NOTE: Assuming that the method List.retainAll() preserves the order in the original list (no counterexample at the moment)
     * @see "https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification" */
    public boolean check_A_And_B_Same_Order(List<String> elementsA, List<String> elementsB) {
        LinkedList<String> elementsALocal = new LinkedList<>(elementsA);
        LinkedList<String> elementsBLocal = new LinkedList<>(elementsB);
        List<String> commonElements = getCommonElementsDistinct(elementsALocal, elementsBLocal);
        elementsALocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)
        elementsBLocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)

        return elementsALocal.equals(elementsBLocal);
    }

    /**
     * Construct a seqCol level 2 (Map representation) out of the given seqColL2Map*/
    public Map<String, String> constructSeqColLevelOneMap(Map<String, List<String>> seqColL2Map) throws IOException {
        Map<String, String> seqColL1Map = new TreeMap<>();
        Set<String> seqColAttributes = seqColL2Map.keySet(); // The set of the seqCol attributes ("lengths", "sequences", etc.)
        for (String attribute: seqColAttributes) {
            String attributeDigest = digestCalculator.getSha512Digest(
                    convertSeqColLevelTwoAttributeValuesToString(seqColL2Map.get(attribute)));
            seqColL1Map.put(attribute, attributeDigest);
        }
        return seqColL1Map;
    }

    /**
     * Return the level 0 digest of the given seqColLevelTwoMap, which is in the form of a Map (undefined attributes)*/
    public String calculateSeqColLevelTwoMapDigest(Map<String, List<String>> seqColLevelTwoMap) throws IOException {
        Map<String, String> seqColLevelOne = constructSeqColLevelOneMap(seqColLevelTwoMap);
        String levelZeroDigest = calculateSeqColLevelOneMapDigest(seqColLevelOne);
        return levelZeroDigest;
    }

    /**
     * Return the level 0 digest of the given seqColLevelOneMap, which is in the form of a Map (undefined attributes)*/
    public String calculateSeqColLevelOneMapDigest(Map<String, String> seqColLevelOneMap) throws IOException {
        String seqColStandardRepresentation = convertSeqColLevelOneAttributeToString(seqColLevelOneMap);
        String levelZeroDigest = digestCalculator.getSha512Digest(seqColStandardRepresentation);
        return levelZeroDigest;
    }

    private boolean onlyDigits(String str) {
        String regex = "[0-9]+";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * Return a normalized string representation of the given seqColL2Attribute
     * Note: This is the same method as the toString of the JSONExtData class*/
    private String convertSeqColLevelTwoAttributeValuesToString(List<String> seqColL2Attribute) {
        StringBuilder objectStr = new StringBuilder();
        objectStr.append("[");
        if (onlyDigits(seqColL2Attribute.get(0).toString())) { // Lengths array, No quotes "...". Eg: [1111, 222, 333]
            for (int i=0; i<seqColL2Attribute.size()-1; i++) {
                objectStr.append(seqColL2Attribute.get(i));
                objectStr.append(",");
            }
            objectStr.append(seqColL2Attribute.get(seqColL2Attribute.size()-1));
            objectStr.append("]");
        } else { // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
            for (int i=0; i<seqColL2Attribute.size()-1; i++) {
                objectStr.append("\"");
                objectStr.append(seqColL2Attribute.get(i));
                objectStr.append("\"");
                objectStr.append(",");
            }
            objectStr.append("\"");
            objectStr.append(seqColL2Attribute.get(seqColL2Attribute.size()-1));
            objectStr.append("\"");
            objectStr.append("]");
        }
        return objectStr.toString();
    }

    /**
     * Return a normalized seqCol representation of the given seqColLevelOneMap
     * Note: This method is the same as the toString method of the SeqColLevelOneEntity class*/
    private String convertSeqColLevelOneAttributeToString(Map<String, String> seqColLevelOneMap) {
        StringBuilder seqColStringRepresentation = new StringBuilder();
        seqColStringRepresentation.append("{");
        for (String attribute: seqColLevelOneMap.keySet()) {
            if (!attribute.equals("sequences") && !attribute.equals("lengths") && !attribute.equals("names")) {
                continue; // Only "sequences", "lengths" and "names" intervene in the level zero digest calculation
            }
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(attribute);
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(":");
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(seqColLevelOneMap.get(attribute));
            seqColStringRepresentation.append("\","); // Pay attention for the last ","
        }
        // remove the last comma
        seqColStringRepresentation.replace(seqColStringRepresentation.length()-1, seqColStringRepresentation.length(), "");
        seqColStringRepresentation.append("}");
        return seqColStringRepresentation.toString();
    }

    /**
     * Return fields of list1 that are not contained in list2*/
    public List<String> getUniqueElements(List<String> list1, List<String> list2) {
        List<String> tempList = new ArrayList<>(list1);
        tempList.removeAll(list2);
        return tempList;
    }

    /**
     * Return the list of the common elements between seqColAFields and seqColBFields (with no duplicates)*/
    public List<String> getCommonElementsDistinct(List<String> seqColAFields, List<String> seqColBFields) {
        List<String> commonFields = new ArrayList<>(seqColAFields);
        commonFields.retainAll(seqColBFields);
        List<String> commonFieldsDistinct = commonFields.stream().distinct().collect(Collectors.toList());
        return commonFieldsDistinct;
    }

    /**
     * Return the number of common elements between listA and listB
     * Note: Time complexity for this method is about O(n²)*/
    public Integer getCommonElementsCount(List<String> listA, List<String> listB) {
        List<String> listALocal = new ArrayList<>(listA); // we shouldn't be making changes on the actual lists
        List<String> listBLocal = new ArrayList<>(listB);
        int count = 0;
        // Looping over the smallest list will sometimes be time saver
        if (listALocal.size() < listBLocal.size()) {
            for (String element : listALocal) {
                if (listBLocal.contains(element)) {
                    count ++;
                    listBLocal.remove(element);
                }
            }
        } else {
            for (String element : listBLocal) {
                if (listALocal.contains(element)) {
                    count++;
                    listALocal.remove(element);
                }
            }
        }
        return count;
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean lessThanTwoOverlappingElements(List<String> list1, List<String> list2) {
        return getCommonElementsDistinct(list1, list2).size() < 2;
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
        List<String> commonElements = getCommonElementsDistinct(listA, listB);
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
