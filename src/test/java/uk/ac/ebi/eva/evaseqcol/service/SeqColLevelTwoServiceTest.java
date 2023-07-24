package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColLevelTwoServiceTest {

    private String LEVEL_0_DIGEST = "Y2ujWD8fTeC86uKbL22N2jyMYrcX0cN0";

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    @Test
    @Disabled
    // Disabled on GitHub. You can enable it locally when you have the seqCol with the given digest saved
    void getSeqColLevelTwoByDigest() {
        Optional<SeqColLevelTwoEntity> levelTwoEntity = levelTwoService.getSeqColLevelTwoByDigest(LEVEL_0_DIGEST);
        assertTrue(levelTwoEntity.isPresent());
        assertTrue(levelTwoEntity.get().getLengths().size() > 0);
    }
}