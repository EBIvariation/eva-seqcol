package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColComparisonResultEntity;
import uk.ac.ebi.eva.evaseqcol.datasource.NCBISeqColDataSource;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColService {

    private final NCBISeqColDataSource ncbiSeqColDataSource;
    private final SeqColLevelOneService levelOneService;
    private final SeqColLevelTwoService levelTwoService;
    private final SeqColExtendedDataService extendedDataService;
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

    public SeqColComparisonResultEntity compareSeqCols1(String seqColADigest, String seqColBDigest) throws NoSuchFieldException {
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);
        Optional<SeqColLevelTwoEntity> seqColBEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColBDigest);
        // TODO HANDLE NULL EXCEPTIONS

        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();
        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);

        // "arrays" attribute
        try {
            Field[] seqColAFields = seqColAEntity.get().getClass().getDeclaredFields();
            Field[] seqColBFields = seqColBEntity.get().getClass().getDeclaredFields();
            List<String> seqColAFieldNames = getFieldsNames(seqColAFields);
            List<String> seqColBFieldNames = getFieldsNames(seqColBFields);
            List<String> seqColAUniqueFields = getUniqueFields(seqColAFieldNames, seqColBFieldNames);
            List<String> seqColBUniqueFields = getUniqueFields(seqColBFieldNames, seqColAFieldNames);
            List<String> seqColCommonFieldNames = getCommonFields(seqColAFieldNames, seqColBFieldNames);
            comparisonResult.putIntoArrays("a-only", seqColAUniqueFields);
            comparisonResult.putIntoArrays("b-only", seqColBUniqueFields);
            comparisonResult.putIntoArrays("a-and-b", seqColCommonFieldNames);

        } catch (NullPointerException e) {
            // TODO LOGGER MESSAGE
            System.out.println("Either seqColA or seqColB is not initialized");
            throw new RuntimeException(e.getMessage());
        }
        // "elements"
        List<String> seqColALengths = seqColAEntity.get().getLengths();
        List<String> seqColBLengths = seqColBEntity.get().getLengths();
        List<String> seqColANames = seqColAEntity.get().getNames();
        List<String> seqColBNames = seqColBEntity.get().getNames();
        List<String> seqColASequences = seqColAEntity.get().getSequences();
        List<String> seqColBSequences = seqColBEntity.get().getSequences();


        // "elements" attribute | "total"
        Integer seqColATotal = seqColAEntity.get().getSequences().size();
        Integer seqColBTotal = seqColBEntity.get().getSequences().size();
        comparisonResult.putIntoElements("total", "a", seqColATotal);
        comparisonResult.putIntoElements("total", "b", seqColBTotal);

        // "elements" attribute | "a-and-b"
        // TODO: NOTE: WE'LL CHANGE THE ALGORITHM FOR THE 'LENGTHS' WHEN WE CHANGE IT INTO 'INTEGER' TYPE
        Integer commonLengths = getCommonFields(seqColALengths, seqColBLengths).size();
        Integer commonNames = getCommonFields(seqColANames, seqColBNames).size();
        Integer commonSequences = getCommonFields(seqColASequences, seqColBSequences).size();
        comparisonResult.putIntoElements("a-and-b", "lengths", commonLengths);
        comparisonResult.putIntoElements("a-and-b", "names", commonNames);
        comparisonResult.putIntoElements("a-and-b", "sequences", commonSequences);

        // "elements" attribute | "a-and-b-same-order"
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
