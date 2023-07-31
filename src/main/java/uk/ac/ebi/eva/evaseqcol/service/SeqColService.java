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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Return the level 0 digest of the inserted seqcol*/
    public Optional<String> fetchAndInsertSeqColByAssemblyAccession(
            String assemblyAccession, SeqColEntity.NamingConvention namingConvention) throws IOException, DuplicateSeqColException {
        Optional<List<SeqColExtendedDataEntity>> fetchExtendedDataEntities = ncbiSeqColDataSource.getSeqColExtendedDataListByAccession(
                assemblyAccession, namingConvention);
        if (!fetchExtendedDataEntities.isPresent()) {
            throw new RuntimeException(
                    "No seqCol data corresponding to assemblyAccession " + assemblyAccession + " could be found on NCBI datasource");
        }
        SeqColLevelOneEntity levelOneEntity = ncbiSeqColDataSource.constructSeqColLevelOne(
                fetchExtendedDataEntities.get(), namingConvention);
        Optional<String> level0Digest = insertSeqColL1AndL2(levelOneEntity, fetchExtendedDataEntities.get());
        logger.info("Successfully inserted seqCol for assemblyAccession " + assemblyAccession);
        return level0Digest;
    }

    @Transactional
    /**
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
            String seqColADigest, SeqColLevelTwoEntity seqColAEntity, String seqColBDigest, SeqColLevelTwoEntity seqColBEntity) throws InvocationTargetException, IllegalAccessException {

        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();

        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);

        // "arrays" attribute
        setArraysAttribute(seqColAEntity, seqColBEntity, comparisonResult);

        // "elements"
        List<String> seqColALengths = seqColAEntity.getLengths();
        List<String> seqColBLengths = seqColBEntity.getLengths();
        List<String> seqColANames = seqColAEntity.getNames();
        List<String> seqColBNames = seqColBEntity.getNames();
        List<String> seqColASequences = seqColAEntity.getSequences();
        List<String> seqColBSequences = seqColBEntity.getSequences();

        // "elements" attribute | "total"
        setTotalElementsAttribute(seqColAEntity, seqColBEntity, comparisonResult);

        // "elements" attribute | "a-and-b"
        setA_and_B_ElementsAttribute(seqColAEntity, seqColBEntity, comparisonResult);

        // "elements" attribute | "a-and-b-same-order"
        // TODO: MAKE THIS GENERIC
        // LENGTHS
        if (!(lessThanTwoOverlappingElements(seqColALengths, seqColBLengths) && unbalancedDuplicatesPresent(seqColALengths, seqColBLengths))) {
            boolean lengthsSameOrder = seqColALengths.equals(seqColBLengths);
            comparisonResult.putIntoElements("a-and-b-same-order", "lengths", lengthsSameOrder);
        } else {
            comparisonResult.putIntoElements("a-and-b-same-order", "lengths", null);
        }
        // NAMES
        if (!(lessThanTwoOverlappingElements(seqColANames, seqColBNames) && unbalancedDuplicatesPresent(seqColANames, seqColBNames))) {
            boolean namesSameOrder = seqColANames.equals(seqColBNames);
            comparisonResult.putIntoElements("a-and-b-same-order", "names", namesSameOrder);
        } else {
            comparisonResult.putIntoElements("a-and-b-same-order", "names", null);
        }
        // SEQUENCES
        if (!(lessThanTwoOverlappingElements(seqColASequences, seqColBSequences) && unbalancedDuplicatesPresent(seqColASequences, seqColBSequences))) {
            boolean sequencesSameOrder = seqColASequences.equals(seqColBSequences);
            comparisonResult.putIntoElements("a-and-b-same-order", "sequences", sequencesSameOrder);
        } else {
            comparisonResult.putIntoElements("a-and-b-same-order", "sequences", null);
        }

        return comparisonResult;
    }

    private <T extends SeqColEntity> void setArraysAttribute(
            T seqColAEntity, T seqColBEntity, SeqColComparisonResultEntity comparisonResult) {
        Field[] seqColAFields = getFields(seqColAEntity);
        Field[] seqColBFields = getFields(seqColBEntity);
        List<Field> seqColCommonFields = getCommonFields(seqColAEntity, seqColBEntity);
        List<String> seqColAFieldsNames = getFieldsNames(seqColAFields);
        List<String> seqColBFieldsNames = getFieldsNames(seqColBFields);
        List<String> commonFieldsNames = getFieldsNames(seqColCommonFields);
        // a-only
        comparisonResult.putIntoArrays("a-only", seqColAFieldsNames);
        // b-only
        comparisonResult.putIntoArrays("b-only", seqColBFieldsNames);
        // a-and-b
        comparisonResult.putIntoArrays("a-and-b", commonFieldsNames);
    }

    private <T extends SeqColEntity> void setTotalElementsAttribute(
            T seqColAEntity, T seqColBEntity, SeqColComparisonResultEntity comparisonResult) throws InvocationTargetException, IllegalAccessException {
        Method[] seqColAMethods = getAllMethods(seqColAEntity);
        Method[] seqColBMethods = getAllMethods(seqColAEntity);
        List<Method> seqColAGetters = getGetters(seqColAMethods);
        List<Method> seqColBGetters = getGetters(seqColBMethods);
        List<String> seqColASequencesList = (List<String>) seqColAGetters.get(0).invoke(seqColAEntity, null);
        List<String> seqColBSequencesList = (List<String>) seqColBGetters.get(0).invoke(seqColBEntity, null);
        Integer totalA = seqColASequencesList.size();
        Integer totalB = seqColBSequencesList.size();
        comparisonResult.putIntoElements("total", "a", totalA);
        comparisonResult.putIntoElements("total", "b", totalB);
        System.out.println("totalA: " + totalA);
        System.out.println("totalB: " + totalB);
    }

    private <T extends SeqColEntity> void setA_and_B_ElementsAttribute(
            T seqColAEntity,T seqColBEntity, SeqColComparisonResultEntity comparisonResult) {
        List<Field> commonFields = getCommonFields(seqColAEntity, seqColBEntity);
        List<Method> commonGetters = getCommonGetters(seqColAEntity, seqColBEntity);
        Map<Field, Method> fieldGetterMap = getFieldsGetterMap(commonFields, commonGetters);
        for (Map.Entry<Field, Method> fieldMethodEntry: fieldGetterMap.entrySet()) {
            comparisonResult.putIntoElements(
                    "a-and-b", fieldMethodEntry.getKey().getName(), fieldMethodEntry.getValue().getName());
        }
    }

    private <T extends SeqColEntity> Field[] getFields(T seqCol) {
        Field[] fields = seqCol.getClass().getFields();
        return fields;
    }

    /**
     * Return the list of field names out of the given Fields list*/
    private List<String> getFieldsNames(List<Field> fields) {
        List<String> fieldNames = new ArrayList<>();
        for (Field field: fields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    /**
     * Return an array of all the methods of the given seqCol object*/
    private <T extends SeqColEntity> Method[] getAllMethods(T seqCol) {
        Class<?> seqColClass = seqCol.getClass();
        Method[] seqColAMethods = seqColClass.getDeclaredMethods();
        return seqColAMethods;
    }

    /**
     * Map each field with the corresponding getter*/
    private Map<Field, Method> getFieldsGetterMap(List<Field> fields, List<Method> methods) {
        if (fields.size() != methods.size()) {
            // TODO: LOGGER HERE
            System.out.println("cannot create fieldGetterMap. fields and methods are in different sizes");
            return null;
        }
        Map<Field, Method> fieldMethodMap = new HashMap<>();
        for (Field field: fields) {
            for (Method method: methods) {
                if (field.getName().equals(method.getName().substring(3))) {
                    fieldMethodMap.put(field, method);
                }
            }
        }
        return fieldMethodMap;
    }
    /**
     * Return the list of common fields between seqColA and seqColB*/
    private <T extends SeqColEntity> List<Field> getCommonFields(T seqColAEntity, T seqColBEntity) {
        Field[] seqColAFields = seqColAEntity.getClass().getDeclaredFields();
        Field[] seqColBFields = seqColBEntity.getClass().getDeclaredFields();
        List<Field> commonFields = new ArrayList<>();
        for (Field seqColAField : seqColAFields) {
            for (Field seqColBField : seqColBFields) {
                if (seqColAField.getName().equals(seqColBField.getName())) {
                    commonFields.add(seqColAField);
                }
            }
        }
        return commonFields;
    }

    /**
     * Return common getters between the two given seqCols*/
    private <T extends SeqColEntity> List<Method> getCommonGetters(T seqColA, T seqColB) {
        Method[] seqColAMethods = getAllMethods(seqColA);
        Method[] seqColBMethods = getAllMethods(seqColB);
        List<Method> seqColAGetters = getGetters(seqColAMethods);
        List<Method> seqColBGetters = getGetters(seqColBMethods);
        List<Method> commonGetters = new ArrayList<>();
        for (Method getterA: seqColAGetters) {
            if (seqColBGetters.contains(getterA)) {
                commonGetters.add(getterA);
            }
        }
        return commonGetters;
    }

    /**
     * Return the list of getters of the given array of methods*/
    private List<Method> getGetters(Method[] methods) {
        List<Method> getters = new ArrayList<>();
        for (Method method: methods) {
            if (isGetter(method)) {
                getters.add(method);
            }
        }
        return getters;
    }
    /**
     * Check whether the given method is a getter or not*/
    boolean isGetter(Method method) {
        return method.getName().startsWith("get");
    }

    /**
     * Compare two seqCols given a level 0 digest of the first one and the L2 seqCol object of the second
     * // TODO: REMOVE THE NAMING CONVENTION FROM THE METHOD */
    public SeqColComparisonResultEntity compareSeqCols(
            String seqColADigest, SeqColLevelTwoEntity seqColBEntity, SeqColEntity.NamingConvention convention) throws IOException, InvocationTargetException, IllegalAccessException {

        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);
        if (!seqColAEntity.isPresent()) {
            throw new SeqColNotFoundException(seqColADigest);
        }
        SeqColLevelOneEntity seqColBL1 = levelOneService.constructSeqColLevelOne(seqColBEntity, convention);
        String seqColBDigest = digestCalculator.getSha512Digest(seqColBL1.toString());
        return compareSeqCols(seqColADigest, seqColAEntity.get(), seqColBDigest, seqColBEntity);

    }

    /**
     * Compare two seqCols given their level 0 digests*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, String seqColBDigest) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
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
     * Return the common fields between seqColA and seqColB*/
    public List<String> getCommonFields(List<String> seqColAFields, List<String> seqColBFields) {
        List<String> commonFields = new ArrayList<>(seqColAFields);
        commonFields.retainAll(seqColBFields);
        return commonFields;
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean lessThanTwoOverlappingElements(List<String> list1, List<String> list2) {
        return getCommonFields(list1, list2).size() < 2;
    }

    /**
     * Return true if there are unbalanced duplicates present
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean unbalancedDuplicatesPresent(List<String> list1, List<String> list2) {
        // TODO: UNDERSTAND THE CONCEPT IN THE SEQCOL-SPECS
        return false;
    }
}
