package uk.ac.ebi.eva.evaseqcol.refget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DigestCalculatorTest {

    private DigestCalculator digestCalculator = new DigestCalculator();
    private final String SEQUENCES = "EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe";
    private final String LENGTHS = "5K4odB173rjao1Cnbk5BnvLt9V7aPAa2";
    private final String NAMES = "g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp";

    private final String DIGEST = "viVlP5M2pi4N8qiLiRkc4xEykrcPBzbB";
    private SeqColLevelOneEntity levelOneEntity;

    private final String ARRAY_TEST = "[248956422, 242193529, 198295559]";
    private final String ARRAY_DIGEST = "5K4odB173rjao1Cnbk5BnvLt9V7aPAa2";

    private final String sequences_array = "[\"MD5-sqdfsdodshijfsd354768\",\"MD5-fjroptkgqsdfsd5f7sdlp\",\"MD5-sdpohgnjkisqdj,fiokjz\"]";

    @BeforeEach
    void setUp() {
        levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne().setSequences(SEQUENCES)
                                                      .setNames(NAMES).setLengths(LENGTHS);
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
    }

    @Test
    void getDigest() throws IOException {
        String res1 = digestCalculator.getSha512Digest(levelOneEntity.toString());
        String res2 = digestCalculator.getSha512Digest(ARRAY_TEST);
        String res3 = digestCalculator.getSha512Digest(sequences_array);
        assertEquals(DIGEST, res1);
        assertEquals(ARRAY_DIGEST, res2);
        assertEquals("J8ovDQ2uLIxaOIQvDEQeGf4d5Yp5QJV-", res3);
    }
}