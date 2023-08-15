package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColLevelOneService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class JSONLevelOneTest {


    @Autowired
    private SeqColLevelOneService levelOneService;

    @Test
    void newAttributeTest() {
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        jsonLevelOne.setLengths("llllll");
        jsonLevelOne.setSequences("ssssss");
        jsonLevelOne.setNames("nnnnnn");
        Map<String, String> levelOneObject = new HashMap<>();
        levelOneObject.put("sequences2", "s2s2s2");
        levelOneObject.put("lengths2", "l2l2l2l");
        levelOneObject.put("names2", "n2n2n");

        /*levelOneObject.put("lengths", "l1l1l1l1l1");
        levelOneObject.put("names", "nnnnnnn");
        levelOneObject.put("sequences", "sssssss");
        jsonLevelOne.setLevelOneObject(levelOneObject);*/
        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        levelOneEntity.setDigest("xxxxxxxxxxxxx555555");
        levelOneEntity.setNamingConvention(SeqColEntity.NamingConvention.ENA);
        Optional<SeqColLevelOneEntity> resultEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertTrue(resultEntity.isPresent());
    }
}