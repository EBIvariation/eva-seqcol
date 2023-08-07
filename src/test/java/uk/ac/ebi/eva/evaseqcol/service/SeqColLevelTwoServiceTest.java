package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColLevelTwoServiceTest {

    @Autowired
    private SeqColWriter seqColWriter;

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the UCSC convention
    private String LEVEL_0_DIGEST_UCSC = "RbymmIOGuL3Ki9XbV6fHKkCb316x5Rv9";

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the GENBANK convention
    private String LEVEL_0_DIGEST_GENBANK = "eJ8GCVLEVtdnCN4OSqfkf6KoEOK9OUlr";

    @Autowired
    private SeqColLevelTwoService levelTwoService;


    @Test
    @Transactional
    void getSeqColLevelTwoByDigest() throws IOException {
        seqColWriter.write();
        Optional<SeqColLevelTwoEntity> levelTwoEntityUcsc = levelTwoService.getSeqColLevelTwoByDigest(LEVEL_0_DIGEST_UCSC);
        Optional<SeqColLevelTwoEntity> levelTwoEntityGenbank = levelTwoService.getSeqColLevelTwoByDigest(LEVEL_0_DIGEST_GENBANK);
        assertTrue(levelTwoEntityUcsc.isPresent());
        assertTrue(!levelTwoEntityUcsc.get().getLengths().isEmpty());
        assertTrue(levelTwoEntityGenbank.isPresent());
        assertTrue(!levelTwoEntityGenbank.get().getLengths().isEmpty());
        seqColWriter.clearData();
    }
}