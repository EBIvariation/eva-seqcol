package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.datasource.NCBISeqColDataSource;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.duplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColService {

    private final NCBISeqColDataSource ncbiSeqColDataSource;
    private final SeqColLevelOneService levelOneService;
    private final SeqColExtendedDataService extendedDataService;
    private final Logger logger = LoggerFactory.getLogger(SeqColService.class);

    @Autowired
    public SeqColService(NCBISeqColDataSource ncbiSeqColDataSource, SeqColLevelOneService levelOneService,
                         SeqColExtendedDataService extendedDataService) {
        this.ncbiSeqColDataSource = ncbiSeqColDataSource;
        this.levelOneService = levelOneService;
        this.extendedDataService = extendedDataService;
    }

    @Transactional
    /**
     * Insert full sequence collection data (level 1 entity, and the exploded data entities)
     * @return  The level 0 digest of the whole seqCol object*/
    public Optional<String> addFullSequenceCollection(SeqColLevelOneEntity levelOneEntity, List<SeqColExtendedDataEntity> extendedSeqColDataList) {
        long numSeqCols = levelOneService.countSeqColLevelOneEntitiesByDigest(levelOneEntity.getDigest());
        if (numSeqCols > 0) {
            throw new duplicateSeqColException(levelOneEntity.getDigest());
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
           // Retrieving legnths
           String lengthsDigest = seqColLevelOne.get().getSeqColLevel1Object().getLengths();
           JSONExtData extendedLengths = extendedDataService.getSeqColExtendedDataEntityByDigest(lengthsDigest).get().getExtendedSeqColData();
           // Retrieving names
           String namesDigest = seqColLevelOne.get().getSeqColLevel1Object().getNames();
           JSONExtData extendedNames = extendedDataService.getSeqColExtendedDataEntityByDigest(namesDigest).get().getExtendedSeqColData();

           levelTwoEntity.setSequences(extendedSequences.getObject());
           levelTwoEntity.setLengths(extendedLengths.getObject());
           levelTwoEntity.setNames(extendedNames.getObject());

           return Optional.of(levelTwoEntity);
       } else {
           logger.error("Could not find any seqCol object with digest " + digest + " on level " + level);
           return Optional.empty();
       }
    }
    public void fetchAndInsertSeqCol(String accession, SeqColEntity.NamingConvention namingConvention) throws IOException {
        Optional<List<SeqColExtendedDataEntity>> fetchExtendedDataEntities = ncbiSeqColDataSource.getSeqColExtendedDataListByAccession(
                accession, namingConvention);
        if (!fetchExtendedDataEntities.isPresent()) {
            throw new RuntimeException("No seqCol data corresponding to accession " + accession + " could be found on NCBI datasource");
        }
        SeqColLevelOneEntity levelOneEntity = ncbiSeqColDataSource.constructSeqColLevelOne(
                fetchExtendedDataEntities.get(), namingConvention);
        insertSeqColL1AndL2(levelOneEntity, fetchExtendedDataEntities.get());
        logger.info("Successfully inserted seqCol for accession " + accession);
    }

    @Transactional
    public void insertSeqColL1AndL2(SeqColLevelOneEntity levelOneEntity,
                                    List<SeqColExtendedDataEntity> seqColExtendedDataEntities) {
        if (isSeqColL1Present(levelOneEntity)) {
            throw new duplicateSeqColException(levelOneEntity.getDigest());
        } else {
            addFullSequenceCollection(levelOneEntity, seqColExtendedDataEntities);
        }

    }

    private boolean isSeqColL1Present(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColLevelOneEntity> existingSeqCol = levelOneService.getSeqColLevelOneByDigest(levelOneEntity.getDigest());
        return existingSeqCol.isPresent();
    }
}
