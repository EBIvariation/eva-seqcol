package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONLevelOne;

import java.util.List;
import java.util.Optional;

/**
 * Builds level 2 seqCol objects, either from the database (level 1 object + its extended data) or from an
 * in-memory set of extended data.
 */
@Service
public class SeqColLevelTwoService {

    private final SeqColExtendedDataService extendedDataService;

    private final SeqColLevelOneService levelOneService;

    public SeqColLevelTwoService(SeqColExtendedDataService extendedDataService, SeqColLevelOneService levelOneService) {
        this.extendedDataService = extendedDataService;
        this.levelOneService = levelOneService;
    }

    /**
     * Retrieve and construct the seqCol level 2 object
     * @param digest level 0 seqCol digest
     * @return empty if no seqCol with this digest exists
     */
    public Optional<SeqColLevelTwoEntity> getSeqColLevelTwoByDigest(String digest) {
        return levelOneService.getSeqColLevelOneByDigest(digest).map(this::getSeqColLevelTwo);
    }

    /**
     * Retrieve the extended data of the given level 1 object and construct the corresponding level 2 object
     * @throws uk.ac.ebi.eva.evaseqcol.exception.ExtendedDataNotFoundException if an attribute is missing in the db
     */
    public SeqColLevelTwoEntity getSeqColLevelTwo(SeqColLevelOneEntity levelOneEntity) {
        JSONLevelOne levelOne = levelOneEntity.getSeqColLevel1Object();
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity().setDigest(levelOneEntity.getDigest());
        for (AttributeType attributeType : AttributeType.values()) {
            SeqColExtendedDataEntity<List<?>> extendedData =
                    extendedDataService.getByDigest(levelOne.getDigest(attributeType));
            setAttribute(levelTwoEntity, attributeType, extendedData.getExtendedSeqColData().getObject());
        }
        return levelTwoEntity;
    }

    /**
     * Construct the level 2 object out of the given extended data
     */
    public SeqColLevelTwoEntity constructSeqColL2(String level0Digest, SeqColExtendedDataSet extendedData) {
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity().setDigest(level0Digest);
        for (SeqColExtendedDataEntity<?> dataEntity : extendedData.all()) {
            setAttribute(levelTwoEntity, dataEntity.getAttributeType(),
                         (List<?>) dataEntity.getExtendedSeqColData().getObject());
        }
        return levelTwoEntity;
    }

    @SuppressWarnings("unchecked")
    private void setAttribute(SeqColLevelTwoEntity levelTwoEntity, AttributeType attributeType, List<?> values) {
        switch (attributeType) {
            case names -> levelTwoEntity.setNames((List<String>) values);
            case sequences -> levelTwoEntity.setSequences((List<String>) values);
            case md5DigestsOfSequences -> levelTwoEntity.setMd5DigestsOfSequences((List<String>) values);
            case lengths -> levelTwoEntity.setLengths((List<Integer>) values);
            case sortedNameLengthPairs -> levelTwoEntity.setSortedNameLengthPairs((List<String>) values);
        }
    }
}
