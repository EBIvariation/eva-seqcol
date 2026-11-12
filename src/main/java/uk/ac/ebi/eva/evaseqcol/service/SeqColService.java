package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.evaseqcol.dto.PaginatedResponse;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.exception.UnableToLoadServiceInfoException;
import uk.ac.ebi.eva.evaseqcol.utils.SeqColMapConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage and retrieval of complete seqCol objects.
 * @see SeqColIngestionService for the creation of seqCol objects from external sources
 * @see SeqColComparisonService for the comparison of seqCol objects
 */
@Service
public class SeqColService {

    private final Logger logger = LoggerFactory.getLogger(SeqColService.class);

    private final SeqColLevelOneService levelOneService;

    private final SeqColLevelTwoService levelTwoService;

    private final SeqColExtendedDataService extendedDataService;

    private final String serviceInfoFilePath;

    private volatile Map<String, Object> serviceInfo;

    public SeqColService(SeqColLevelOneService levelOneService, SeqColLevelTwoService levelTwoService,
                         SeqColExtendedDataService extendedDataService,
                         @Value("${service.info.file.path}") String serviceInfoFilePath) {
        this.levelOneService = levelOneService;
        this.levelTwoService = levelTwoService;
        this.extendedDataService = extendedDataService;
        this.serviceInfoFilePath = serviceInfoFilePath;
    }

    /**
     * Insert full sequence collection data (level 1 entity, and its extended data entities) in one transaction
     * @return The level 0 digest of the whole seqCol object
     * @throws DuplicateSeqColException if a seqCol with the same level 0 digest already exists
     */
    @Transactional
    public String addFullSequenceCollection(SeqColLevelOneEntity levelOneEntity, SeqColExtendedDataSet extendedData) {
        if (levelOneService.existsByDigest(levelOneEntity.getDigest())) {
            logger.warn("SeqCol with digest {} already exists !", levelOneEntity.getDigest());
            throw new DuplicateSeqColException(levelOneEntity.getDigest());
        }
        SeqColLevelOneEntity savedLevelOneEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        extendedDataService.addAll(extendedData.all());
        logger.info("Added seqCol object with digest: {}", savedLevelOneEntity.getDigest());
        return savedLevelOneEntity.getDigest();
    }

    public Optional<SeqColLevelOneEntity> getSeqColByDigestLevel1(String digest) {
        return levelOneService.getSeqColLevelOneByDigest(digest);
    }

    public Optional<SeqColLevelTwoEntity> getSeqColByDigestLevel2(String digest) {
        return levelTwoService.getSeqColLevelTwoByDigest(digest);
    }

    /**
     * Return the values of the given attribute, if the digest corresponds to this attribute in at least one seqCol
     */
    public Optional<List<?>> getSeqColAttribute(String digest, AttributeType attribute) {
        Optional<SeqColExtendedDataEntity<List<?>>> attributeDataEntity = extendedDataService.findByDigest(digest);
        if (attributeDataEntity.isEmpty()) {
            return Optional.empty();
        }
        // check if the digest belongs to the requested attribute
        Page<String> resultPage = levelOneService.findDigests(PageRequest.of(0, 1),
                                                              Map.of(attribute.getAttrVal(), digest));
        if (resultPage.getTotalElements() == 0) {
            return Optional.empty();
        }
        return Optional.of(attributeDataEntity.get().getExtendedSeqColData().getObject());
    }

    public PaginatedResponse<String> getSeqColList(int page, int pageSize, Map<String, String> filters) {
        return PaginatedResponse.fromPage(levelOneService.findDigests(PageRequest.of(page, pageSize), filters));
    }

    public List<SeqColMetadataEntity> getSeqColMetadataBySeqColDigest(String digest) {
        return levelOneService.getMetadataBySeqcolDigest(digest);
    }

    /**
     * Return the service info entity in a Map<String,Object> format. The file is read once and then cached.
     * @see 'https://seqcol.readthedocs.io/en/dev/specification/#21-service-info'
     * for more details about the service-info
     */
    public Map<String, Object> getServiceInfo() {
        if (serviceInfo == null) {
            try {
                serviceInfo = SeqColMapConverter.jsonToMap(serviceInfoFilePath);
            } catch (IOException e) {
                logger.error("Unable to load service-info file {}", serviceInfoFilePath, e);
                throw new UnableToLoadServiceInfoException(serviceInfoFilePath);
            }
        }
        return serviceInfo;
    }

    /**
     * Remove all seqCol entities (level 1 and the extended entities) from the database
     */
    @Transactional
    public void removeAllSeqCol() {
        logger.info("Removing all seqCol objects from the database !");
        levelOneService.removeAllSeqCols();
        extendedDataService.removeAllSeqColExtendedEntities();
    }
}
