package uk.ac.ebi.eva.evaseqcol.digests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColDigestService;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONLevelOne;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigestCalculatorTest {

    private final DigestCalculator digestCalculator = new DigestCalculator();

    private final SeqColDigestService digestService = new SeqColDigestService(digestCalculator);
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
        // Level 0 digest: only the inherent attributes (names and sequences) are used
        String res1 = digestService.calculateLevelZeroDigest(levelOneEntity.getSeqColLevel1Object());
        String res2 = digestCalculator.getSha512Digest(ARRAY_TEST);
        String res3 = digestCalculator.getSha512Digest(sequences_array);
        assertEquals(DIGEST, res1);
        assertEquals(ARRAY_DIGEST, res2);
        assertEquals("J8ovDQ2uLIxaOIQvDEQeGf4d5Yp5QJV-", res3);
    }

    @Test
    void getDigestIsThreadSafe() throws IOException {
        List<String> inputs = IntStream.range(0, 50).mapToObj(i -> "[" + i + ", " + (i * 7) + "]").toList();
        Map<String, String> expected = new HashMap<>();
        for (String input : inputs) {
            expected.put(input, digestCalculator.getSha512Digest(input));
        }
        IntStream.range(0, 20_000).parallel().forEach(i -> {
            String input = inputs.get(i % inputs.size());
            try {
                assertEquals(expected.get(input), digestCalculator.getSha512Digest(input));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
