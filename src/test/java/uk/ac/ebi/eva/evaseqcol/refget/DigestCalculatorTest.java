package uk.ac.ebi.eva.evaseqcol.refget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DigestCalculatorTest {

    private DigestCalculator digestCalculator = new DigestCalculator();
    private final String SEQUENCES = "EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe";
    private final String LENGTHS = "5K4odB173rjao1Cnbk5BnvLt9V7aPAa2";
    private final String NAMES = "g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp";

    private final String DIGEST = "S3LCyI788LE6vq89Tc_LojEcsMZRixzP";
    private SeqColLevelOneEntity levelOneEntity;

    private final String ARRAY_TEST = "[248956422, 242193529, 198295559]";
    private final String ARRAY_DIGEST = "5K4odB173rjao1Cnbk5BnvLt9V7aPAa2";


    @BeforeEach
    void setUp() {
        levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne().setSequences(SEQUENCES)
                                                      .setNames(NAMES).setLengths(LENGTHS);
        levelOneEntity.setObject(jsonLevelOne);
    }

    @Test
    void getDigest() throws IOException {
        String res1 = digestCalculator.getDigest(levelOneEntity.toString());
        String res2 = digestCalculator.getDigest(ARRAY_TEST);
        assertEquals(DIGEST, res1);
        assertEquals(ARRAY_DIGEST, res2);
    }
}