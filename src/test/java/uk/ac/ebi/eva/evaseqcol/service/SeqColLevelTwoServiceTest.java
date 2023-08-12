package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColLevelTwoServiceTest {

    @Autowired
    private SeqColWriter seqColWriter;

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the UCSC convention
    private String LEVEL_0_DIGEST_UCSC = "RbymmIOGuL3Ki9XbV6fHKkCb316x5Rv9";

    // This is a level 0 digest of the seqCol inserted by the SeqColWriter following the GENBANK convention
    private String LEVEL_0_DIGEST_GENBANK = "eJ8GCVLEVtdnCN4OSqfkf6KoEOK9OUlr";

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() throws IOException {
        seqColWriter.write();
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
        assertTrue(!levelTwoEntityUcsc.get().getLengths().isEmpty());
        assertTrue(levelTwoEntityGenbank.isPresent());
        assertTrue(!levelTwoEntityGenbank.get().getLengths().isEmpty());
    }
}