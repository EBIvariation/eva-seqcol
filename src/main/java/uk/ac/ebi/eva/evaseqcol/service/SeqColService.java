package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;

import java.util.List;
import java.util.Optional;

@Service
public class SeqColService {

    @Autowired
    SeqColLevelOneService levelOneService;

    @Autowired
    SeqColExtendedDataService extendedDataService;

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
}
