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
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.duplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
        SeqColLevelOneEntity levelOneEntity1 = levelOneService.addSequenceCollectionL1(levelOneEntity).get();
        extendedDataService.addAll(extendedSeqColDataList);
        // TODO: HANDLE EXCEPTIONS
        return Optional.of(levelOneEntity1.getDigest());
    }


    public SeqColEntity getSeqColByDigest(String digest, Integer level) {
        // TODO: implement the join query to fetch the level 2 with one single db query
        return null;
    }
    public void fetchAndInsertSeqCol(String accession, SeqColEntity.NamingConvention namingConvention) throws IOException {
        Optional<List<SeqColExtendedDataEntity>> fetchExtendedDataEntities = ncbiSeqColDataSource.getSeqColExtendedDataListByAccession(
                accession, namingConvention);
        if (!fetchExtendedDataEntities.isPresent()) {
            throw new SeqColNotFoundException(accession);
        }
        SeqColLevelOneEntity levelOneEntity = ncbiSeqColDataSource.constructSeqColLevelOne(
                fetchExtendedDataEntities.get(), namingConvention);
        insertSeqColL1AndL2(fetchExtendedDataEntities.get(), levelOneEntity);
        logger.info("Successfully inserted seqCol for accession " + accession);
    }

    @Transactional
    public void insertSeqColL1AndL2(List<SeqColExtendedDataEntity> seqColExtendedDataEntities,
                                    SeqColLevelOneEntity levelOneEntity) {
        if (isSeqColL1Present(levelOneEntity)) {
            throw new duplicateSeqColException(levelOneEntity.getDigest());
        } else {
            levelOneService.addSequenceCollectionL1(levelOneEntity);
            extendedDataService.addAll(seqColExtendedDataEntities);
        }

    }

    private boolean isSeqColL1Present(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColLevelOneEntity> existingSeqCol = levelOneService.getSeqColLevelOneByDigest(levelOneEntity.getDigest());
        return existingSeqCol.isPresent();
    }
    private boolean isSeqColExtDataPresent(SeqColExtendedDataEntity extendedDataEntity) {
        Optional<SeqColExtendedDataEntity> existingSeqColExtData = extendedDataService.getExtendedAttributeByDigest(
                extendedDataEntity.getDigest());
        return existingSeqColExtData.isPresent();
    }
}
