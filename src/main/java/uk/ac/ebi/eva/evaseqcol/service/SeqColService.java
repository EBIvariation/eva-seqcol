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
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyAlreadyIngestedException;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.AttributeNotDefinedException;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.UnableToLoadServiceInfoException;
import uk.ac.ebi.eva.evaseqcol.model.IngestionResultEntity;
import uk.ac.ebi.eva.evaseqcol.model.InsertedSeqColEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONIntegerListExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONStringListExtData;
import uk.ac.ebi.eva.evaseqcol.utils.SeqColMapConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
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
    public Optional<String> addFullSequenceCollection(
            SeqColLevelOneEntity levelOneEntity,
            List<SeqColExtendedDataEntity<List<String>>> seqColStringListExtDataEntities,
            List<SeqColExtendedDataEntity<List<Integer>>> seqColIntegerListExtDataEntities
            ) {
        long numSeqCols = levelOneService.countSeqColLevelOneEntitiesByDigest(levelOneEntity.getDigest());
        if (numSeqCols > 0) {
            logger.warn("SeqCol with digest " + levelOneEntity.getDigest() + " already exists !");
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        } else {
            SeqColLevelOneEntity levelOneEntity1 = levelOneService.addSequenceCollectionL1(levelOneEntity).get();
            extendedDataService.addAll(seqColStringListExtDataEntities);
            extendedDataService.addAll(seqColIntegerListExtDataEntities);
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
                logger.warn("No seqCol corresponding to digest " + digest + " could be found in the db");
                throw new SeqColNotFoundException(digest);
            }
            SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity().setDigest(digest);
            // Retrieving sequences
            String sequencesDigest = seqColLevelOne.get().getSeqColLevel1Object().getSequences();
            JSONExtData<List<String>> extendedSequences = extendedDataService.<List<String>>getSeqColExtendedDataEntityByDigest(sequencesDigest).get().getExtendedSeqColData();
            //Retrieving md5 sequences
           String sequencesMd5Digest = seqColLevelOne.get().getSeqColLevel1Object().getMd5DigestsOfSequences();
           JSONExtData<List<String>> extendedMd5Sequnces = extendedDataService.<List<String>>getSeqColExtendedDataEntityByDigest(sequencesMd5Digest).get().getExtendedSeqColData();
           // Retrieving legnths
           String lengthsDigest = seqColLevelOne.get().getSeqColLevel1Object().getLengths();
           JSONExtData<List<Integer>> extendedLengths = extendedDataService.<List<Integer>>getSeqColExtendedDataEntityByDigest(lengthsDigest).get().getExtendedSeqColData();
           // Retrieving names
           String namesDigest = seqColLevelOne.get().getSeqColLevel1Object().getNames();
           JSONExtData<List<String>> extendedNames = extendedDataService.<List<String>>getSeqColExtendedDataEntityByDigest(namesDigest).get().getExtendedSeqColData();
           // Retrieving sortedNameLengthPairs
           String sortedNameLengthPairsDigest = seqColLevelOne.get().getSeqColLevel1Object().getSortedNameLengthPairs();
           JSONExtData<List<String>> extendedSortedNameLengthPairs = extendedDataService.<List<String>>
                   getSeqColExtendedDataEntityByDigest(sortedNameLengthPairsDigest).get().getExtendedSeqColData();

           levelTwoEntity.setSequences(extendedSequences.getObject());
           levelTwoEntity.setMd5DigestsOfSequences(extendedMd5Sequnces.getObject());
           levelTwoEntity.setLengths(extendedLengths.getObject());
           levelTwoEntity.setNames(extendedNames.getObject());
           levelTwoEntity.setSortedNameLengthPairs(extendedSortedNameLengthPairs.getObject());

           return Optional.of(levelTwoEntity);
       } else {
           logger.warn("Could not find any seqCol object with digest " + digest + " on level " + level);
           return Optional.empty();
       }
    }

    public List<SeqColMetadataEntity> getSeqColMetadataBySeqColDigest(String digest) {
        return levelOneService.getMetadataBySeqcolDigest(digest);
    }

    /**
     * Return the service info entity in a Map<String,Object> format
     * @see 'https://seqcol.readthedocs.io/en/dev/specification/#21-service-info'
     * for more details about the service-info*/
    public Map<String, Object> getServiceInfo() {
        try {
            return SeqColMapConverter.jsonToMap(SERVICE_INFO_FILE_PATH);
        } catch (IOException e) {
            throw new UnableToLoadServiceInfoException(SERVICE_INFO_FILE_PATH);
        }
    }

    /**
     * Full remove of the seqCol object (level one and its extended data)*/
    // TODO: REFACTOR
    /*@Transactional
    public <T> void deleteFullSeqCol(String digest, List<SeqColExtendedDataEntity<T>> extendedDataEntities) {
        levelOneService.removeSeqColLevelOneByDigest(digest);
        extendedDataService.removeSeqColExtendedDataEntities(extendedDataEntities);
    }*/

    /**
     * Remove all seqCol entities (level 1 and the extended entities) from the database*/
    @Transactional
    public void removeAllSeqCol() {
        logger.info("Removing all seqCol objects from the database !");
        levelOneService.removeAllSeqCols();
        extendedDataService.removeAllSeqColExtendedEntities();
    }

    public IngestionResultEntity fetchAndInsertAllSeqColInFastaFile(String accession, String fastaFileContent) throws IOException {
        Optional<Map<String, Object>> seqColDataMap = ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(accession, fastaFileContent);
        return createSeqColObjectsAndInsert(seqColDataMap, accession);
    }

    /**
     * Fetch and insert all possible seqCol objects for the given assembly accession.
     * NOTE: All possible seqCol objects means with all possible/provided naming conventions that could be found in the
     * assembly report.
     * Return the list of level 0 digests of the inserted seqcol objects*/
    public IngestionResultEntity fetchAndInsertAllSeqColByAssemblyAccession(String assemblyAccession) throws IOException {
        // Check for existing same source id
        boolean sourceIdExists = levelOneService.getAllMetadata().stream()
                .anyMatch(md -> md.getSourceIdentifier().equals(assemblyAccession));
        if (sourceIdExists) {
            logger.warn("Seqcol objects for assembly " + assemblyAccession + " have been already ingested... Nothing to ingest !");
            throw new AssemblyAlreadyIngestedException(assemblyAccession);
        }
        Optional<Map<String, Object>> seqColDataMap = ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(assemblyAccession);
        return createSeqColObjectsAndInsert(seqColDataMap, assemblyAccession);
    }


    public IngestionResultEntity createSeqColObjectsAndInsert(Optional<Map<String, Object>> seqColDataMap,
                                                              String assemblyAccession) throws IOException {
        if (!seqColDataMap.isPresent()) {
            logger.warn("No seqCol data corresponding to assemblyAccession " + assemblyAccession + " could be found on NCBI datasource");
            throw new AssemblyNotFoundException(assemblyAccession);
        }

        IngestionResultEntity ingestionResultEntity = new IngestionResultEntity();
        ingestionResultEntity.setAssemblyAccession(assemblyAccession);
        // Retrieving the Map's data
        List<SeqColExtendedDataEntity<List<String>>> possibleSequencesNamesList =
                (List<SeqColExtendedDataEntity<List<String>>>) seqColDataMap.get().get("namesAttributes");
        Map<String, Object> sameValueAttributesMap = (Map<String, Object>) seqColDataMap.get().get("sameValueAttributes");


        for (SeqColExtendedDataEntity<List<String>> extendedNamesEntity: possibleSequencesNamesList) {
            //List<SeqColExtendedDataEntity> seqColExtendedDataEntities = new ArrayList<>(sameValueAttributeList);
            // Retrieving the "extendedLengths" entity
            SeqColExtendedDataEntity<List<Integer>> extendedLengthsEntity =
                    (SeqColExtendedDataEntity<List<Integer>>) sameValueAttributesMap.get("extendedLengths");

            // Retrieving the "extendedSortedNameLengthPair" entity
            SeqColExtendedDataEntity<List<String>> extendedSortedNameLengthPair = SeqColExtendedDataEntity.
                    constructSeqColSortedNameLengthPairs(extendedNamesEntity, extendedLengthsEntity);

            // Constructing a list of seqColExtData that has the type List<String>
            List<SeqColExtendedDataEntity<List<String>>> seqColStringListExtDataEntities =
                    levelOneService.constructStringListExtDataEntities(sameValueAttributesMap, extendedNamesEntity,
                                                                       extendedSortedNameLengthPair);

            // Constructing a list of seqColExtData of type List<Integer>
            List<SeqColExtendedDataEntity<List<Integer>>> seqColIntegerListExtDataEntities =
                    levelOneService.constructIntegerListExtDataEntities(sameValueAttributesMap);

            // Constructing seqCol Level One object
            SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(
                    seqColStringListExtDataEntities, seqColIntegerListExtDataEntities, extendedNamesEntity.getNamingConvention(),
                    assemblyAccession);

            try {
                Optional<String> seqColDigest = insertSeqColL1AndL2( // TODO: Check for possible self invocation problem
                                                                     levelOneEntity, seqColStringListExtDataEntities, seqColIntegerListExtDataEntities);
                if (seqColDigest.isPresent()) {
                    logger.info(
                            "Successfully inserted seqCol for assembly Accession " + assemblyAccession + " with naming convention " + extendedNamesEntity.getNamingConvention());
                    InsertedSeqColEntity insertedSeqCol = new InsertedSeqColEntity(seqColDigest.get(), extendedNamesEntity.getNamingConvention().toString());
                    ingestionResultEntity.addInsertedSeqCol(insertedSeqCol);
                    ingestionResultEntity.incrementNumberOfInsertedSeqCols();
                } else {
                    logger.warn("Could not insert seqCol for assembly Accession " + assemblyAccession + " with naming convention " + extendedNamesEntity.getNamingConvention());
                }
            } catch (DuplicateSeqColException e) {
                logger.info("Seqcol for " + assemblyAccession + " and naming convention " + extendedNamesEntity.getNamingConvention() +
                " already exists. Skipping.");
            }
        }
        if (ingestionResultEntity.getNumberOfInsertedSeqcols() == 0) {
            logger.warn("Seqcol objects for assembly " + assemblyAccession + " have been already ingested");
            throw new AssemblyAlreadyIngestedException(assemblyAccession);
        } else {
            return ingestionResultEntity;
        }

    }

    @Transactional
    /**
     * Insert the given Level 1 seqCol entity and its corresponding extended level 2 data (names, lengths, sequences, ...)
     * Return the level 0 digest of the inserted seqCol*/
    public Optional<String> insertSeqColL1AndL2(SeqColLevelOneEntity levelOneEntity,
                                                List<SeqColExtendedDataEntity<List<String>>> seqColStringListExtDataEntities,
                                                List<SeqColExtendedDataEntity<List<Integer>>> seqColIntegerListExtDataEntities) {
        if (isSeqColL1Present(levelOneEntity)) {
            logger.warn("Could not insert seqCol with digest " + levelOneEntity.getDigest() + ". Already exists !");
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        } else {
            Optional<String> level0Digest = addFullSequenceCollection(levelOneEntity,
                                                                      seqColStringListExtDataEntities,
                                                                      seqColIntegerListExtDataEntities);
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
            logger.warn("No seqCol corresponding to digest " + seqColADigest + " could be found in the db");
            throw new SeqColNotFoundException(seqColADigest);
        }
        if (!seqColBEntity.isPresent()) {
            logger.warn("No seqCol corresponding to digest " + seqColBDigest + " could be found in the db");
            throw new SeqColNotFoundException(seqColBDigest);
        }
        return compareSeqCols(seqColADigest, seqColAEntity.get(), seqColBDigest, seqColBEntity.get());
    }

    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, SeqColLevelTwoEntity seqColAEntity,
                                                       String seqColBDigest, SeqColLevelTwoEntity seqColBEntity) {
        Map<String, List<?>> seqColAMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity);
        Map<String, List<?>> seqColBMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColBEntity);
        return compareSeqCols(seqColADigest, seqColAMap, seqColBDigest, seqColBMap);
    }

    /**
     * Compare two seqCol objects; an already saved one: seqColA, with pre-defined attributes,
     * and undefined one: seqColB (unknown attributes). BE CAREFUL: the order of the arguments matters!!.
     * Note: of course the seqCol minimal required attributes should be present*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, Map<String, List<?>> seqColBEntityMap) throws IOException {
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);

        // Calculating the seqColB level 0 digest
        String seqColBDigest = calculateSeqColLevelTwoMapDigest(seqColBEntityMap);

        // Converting seqColA object into a Map in order to handle attributes generically (
        Map<String, List<?>> seqColAEntityMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity.get());

        return compareSeqCols(seqColADigest, seqColAEntityMap, seqColBDigest, seqColBEntityMap);
    }

    /**
     * Compare two seqCol L2 objects*/
    public SeqColComparisonResultEntity compareSeqCols(
            String seqColADigest, Map<String,List<?>> seqColAEntityMap, String seqColBDigest, Map<String, List<?>> seqColBEntityMap) {

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
        comparisonResult.putIntoArrays("a_only", seqColAUniqueAttributes);
        comparisonResult.putIntoArrays("b_only", seqColBUniqueAttributes);
        comparisonResult.putIntoArrays("a_and_b", seqColCommonAttributes);

        // "array_elements" attribute | "a"
        for (String attribute: seqColAAttributeSet) {
            // Looping through each attribute of seqcolA, Eg: "sequences", "lengths", etc...
            comparisonResult.putIntoArrayElements("a_count", attribute, seqColAEntityMap.get(attribute).size());
        }

        // "array_elements" attribute | "b"
        for (String attribute: seqColBAttributeSet) {
            // Looping through each attribute of seqcolB, Eg: "sequences", "lengths", etc...
            comparisonResult.putIntoArrayElements("b_count", attribute, seqColBEntityMap.get(attribute).size());
        }

        // "array_elements" attribute | "a_and_b"
        List<String> commonSeqColAttributesValues = getCommonElementsDistinct(seqColAAttributesList, seqColBAttributesList); // eg: ["sequences", "lengths", ...]
        for (String element: commonSeqColAttributesValues) {
            Integer commonElementsCount = getCommonElementsCount(seqColAEntityMap.get(element), seqColBEntityMap.get(element));
            comparisonResult.putIntoArrayElements("a_and_b_count", element, commonElementsCount);
        }

        // "array_elements" attribute | "a_and_b_same_order"
        for (String attribute: commonSeqColAttributesValues) {
            if (lessThanTwoOverlappingElements(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))
                    || unbalancedDuplicatesPresent(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))){
                comparisonResult.putIntoArrayElements("a_and_b_same_order", attribute, null);
            } else {
                boolean attributeSameOrder = check_A_And_B_Same_Order(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute));
                comparisonResult.putIntoArrayElements("a_and_b_same_order", attribute, attributeSameOrder);
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
    public boolean check_A_And_B_Same_Order(List<?> elementsA, List<?> elementsB) {
        LinkedList<?> elementsALocal = new LinkedList<>(elementsA);
        LinkedList<?> elementsBLocal = new LinkedList<>(elementsB);
        List<?> commonElements = getCommonElementsDistinct(elementsALocal, elementsBLocal);
        elementsALocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)
        elementsBLocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)

        return elementsALocal.equals(elementsBLocal);
    }

    /**
     * Construct a seqCol level 2 (Map representation) out of the given seqColL2Map*/
    public Map<String, String> constructSeqColLevelOneMap(Map<String, List<?>> seqColL2Map) throws IOException {
        Map<String, String> seqColL1Map = new TreeMap<>();
        Set<String> seqColAttributes = seqColL2Map.keySet(); // The set of the seqCol attributes ("lengths", "sequences", etc.)
        for (String attribute: seqColAttributes) {
            try {
                String attributeDigest;
                attributeDigest= digestCalculator.getSha512Digest(
                        convertSeqColLevelTwoAttributeValuesToString(seqColL2Map.get(attribute),
                                                                     SeqColExtendedDataEntity.AttributeType.fromAttributeVal(
                                                                             attribute)));
                seqColL1Map.put(attribute, attributeDigest);
            } catch (AttributeNotDefinedException e) {
                logger.warn(e.getMessage());
            }
        }
        return seqColL1Map;
    }

    /**
     * Return the level 0 digest of the given seqColLevelTwoMap, which is in the form of a Map (undefined attributes)*/
    public String calculateSeqColLevelTwoMapDigest(Map<String, List<?>> seqColLevelTwoMap) throws IOException {
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

    /**
     * Return a normalized string representation of the given seqColL2Attribute
     * //TODO: we can find a better way to identify the given type in a more generic way*/
    private String convertSeqColLevelTwoAttributeValuesToString(List<?> seqColL2Attribute, SeqColExtendedDataEntity.AttributeType type) {
        switch (type) {
            case lengths: // List<Integer> type
                return new JSONIntegerListExtData((List<Integer>) seqColL2Attribute).toString();
            default: // List<String> types
                return new JSONStringListExtData((List<String>) seqColL2Attribute).toString();
        }
    }

    /**
     * Return a normalized seqCol representation of the given seqColLevelOneMap
     * Note: This method is the same as the toString method of the SeqColLevelOneEntity class
     * // TODO: remove code duplicates*/
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
    public List<String> getCommonElementsDistinct(List<?> seqColAFields, List<?> seqColBFields) {
        List<?> commonFields = new ArrayList<>(seqColAFields);
        commonFields.retainAll(seqColBFields);
        List<String> commonFieldsDistinct = (List<String>) commonFields.stream().distinct().collect(Collectors.toList());
        return commonFieldsDistinct;
    }

    /**
     * Return the number of common elements between listA and listB
     * */
    public Integer getCommonElementsCount(List<?> listA, List<?> listB) {
        Set<?> listALocal = new HashSet<>(listA); // we shouldn't be making changes on the actual lists
        Set<?> listBLocal = new HashSet<>(listB);
        listALocal.retainAll(listBLocal);
        return listALocal.size();
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean lessThanTwoOverlappingElements(List<?> list1, List<?> list2) {
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
    public boolean unbalancedDuplicatesPresent(List<?> listA, List<?> listB) {
        List<?> commonElements = getCommonElementsDistinct(listA, listB);
        Map<Object, Map<String, Integer>> duplicatesCountMap = new HashMap<>();
        for (Object element: commonElements) {
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
