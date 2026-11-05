package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.NamingConvention;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONLevelOne;

import java.io.IOException;

/**
 * Builds level 1 seqCol entities (attribute digests + level 0 digest + metadata).
 */
@Component
public class SeqColLevelOneFactory {

    private final SeqColDigestService digestService;

    private final SeqColExtendedDataFactory extendedDataFactory;

    public SeqColLevelOneFactory(SeqColDigestService digestService, SeqColExtendedDataFactory extendedDataFactory) {
        this.digestService = digestService;
        this.extendedDataFactory = extendedDataFactory;
    }

    /**
     * Construct a seqCol level 1 entity out of its extended (level 2) data
     */
    public SeqColLevelOneEntity create(SeqColExtendedDataSet extendedData, NamingConvention convention,
                                       String sourceId) throws IOException {
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        for (SeqColExtendedDataEntity<?> dataEntity : extendedData.all()) {
            jsonLevelOne.setDigest(dataEntity.getAttributeType(), dataEntity.getDigest());
        }

        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        levelOneEntity.addMetadata(new SeqColMetadataEntity()
                                           .setNamingConvention(convention)
                                           .setSourceIdentifier(sourceId));
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        levelOneEntity.setDigest(digestService.calculateLevelZeroDigest(jsonLevelOne));
        return levelOneEntity;
    }

    /**
     * Construct a seqCol level 1 entity out of a level 2 object
     */
    public SeqColLevelOneEntity create(SeqColLevelTwoEntity levelTwoEntity, NamingConvention convention,
                                       String sourceId) throws IOException {
        return create(extendedDataFactory.extendedDataSet(levelTwoEntity), convention, sourceId);
    }
}
