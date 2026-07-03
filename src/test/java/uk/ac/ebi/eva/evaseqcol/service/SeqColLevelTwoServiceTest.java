package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;
import uk.ac.ebi.eva.evaseqcol.utils.AbstractIntegrationTest;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integration")
class SeqColLevelTwoServiceTest extends AbstractIntegrationTest {

    @Autowired
    private SeqColWriter seqColWriter;

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the UCSC convention
    private String LEVEL_0_DIGEST_UCSC = "AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX";

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the GENBANK convention
    private String LEVEL_0_DIGEST_GENBANK = "ySaGQd8xaXhhfyR5PsTBp4ggbXXVub7w";

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    @BeforeEach
    void setUp() throws IOException {
        seqColWriter.clearData();
        seqColWriter.create();
    }

    @AfterEach
    void tearDown() {
        seqColWriter.clearData();
    }

    @Test
    @Transactional
    void getSeqColLevelTwoByDigest() {
        Optional<SeqColLevelTwoEntity> levelTwoEntityUcsc = levelTwoService.getSeqColLevelTwoByDigest(LEVEL_0_DIGEST_UCSC);
        Optional<SeqColLevelTwoEntity> levelTwoEntityGenbank = levelTwoService.getSeqColLevelTwoByDigest(LEVEL_0_DIGEST_GENBANK);
        assertTrue(levelTwoEntityUcsc.isPresent());
        assertFalse(levelTwoEntityUcsc.get().getLengths().isEmpty());
        assertTrue(levelTwoEntityGenbank.isPresent());
        assertFalse(levelTwoEntityGenbank.get().getLengths().isEmpty());
    }
}