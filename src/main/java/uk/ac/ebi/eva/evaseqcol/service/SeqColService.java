package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

@Service
public class SeqColService {

    @Autowired
    SeqColLevelOneService levelOneService;

    @Autowired
    SeqColLevelTwoService levelTwoService;

    @Transactional
    public void addSequenceCollection(SeqColLevelOneEntity levelOneEntity, SeqColLevelTwoEntity levelTwoEntity) {
        levelOneService.addSequenceCollectionL1(levelOneEntity);
        levelTwoService.addSequenceCollectionL2(levelTwoEntity);
        // TODO: HANDLE EXCEPTIONS
    }
}
