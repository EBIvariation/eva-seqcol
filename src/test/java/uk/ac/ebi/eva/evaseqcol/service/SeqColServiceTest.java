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

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColServiceTest {


    private final String TEST_DIGEST = "eJ8GCVLEVtdnCN4OSqfkf6KoEOK9OUlr";

    @Autowired
    private SeqColService seqColService;

    @Autowired
    private SeqColWriter seqColWriter;


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
        seqColWriter.write(); // This will write some seqCol objects to the database
    }

    @AfterEach
    void tearDown() {
        seqColWriter.clearData();
    }

    @Test
    @Transactional
    void getSeqColByDigestAndLevelTest() {
        Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestAndLevel(TEST_DIGEST, 1);
        assertTrue(levelOneEntity.isPresent());
        Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestAndLevel(TEST_DIGEST, 2);
        assertTrue(levelTwoEntity.isPresent());
    }

    @Test
    void unbalancedDuplicatesPresentTest() {
        List<String> A = Arrays.asList("1", "2", "2", "3", "4");
        List<String> B = Arrays.asList("1", "2", "3", "4", "5", "6");

        List<String> A1 = Arrays.asList("1", "2", "3", "4");
        List<String> B1 = Arrays.asList("1", "2", "3", "4", "5", "6");

        List<String> A2 = Arrays.asList("1", "2", "1");
        List<String> B2 = Arrays.asList("2", "1", "1");

        List<String> A3 = Arrays.asList("1", "2", "1");
        List<String> B3 = Arrays.asList("1", "2");

        assertTrue(seqColService.unbalancedDuplicatesPresent(A,B));
        assertFalse(seqColService.unbalancedDuplicatesPresent(A1, B1));
        assertFalse(seqColService.unbalancedDuplicatesPresent(A2, B2));
        assertTrue(seqColService.unbalancedDuplicatesPresent(A3, B3));
    }

    @Test
    void getCommonFieldsDistinctTest() {
        List<String> A1 = new ArrayList<>();
        List<String> B1 = new ArrayList<>();
        A1.add("1");A1.add("2");A1.add("1"); // A1 = ["1", "2", "1"]
        B1.add("1");B1.add("1");B1.add("2"); // B1 = ["1", "1", "2"]

        List<String> A2 = new ArrayList<>();
        List<String> B2 = new ArrayList<>();
        A2.add("1");A2.add("2");A2.add("1"); // A2 = ["1", "2", "1"]
        B2.add("1"); B2.add("2");            // B2 = ["1", "2"]

        Integer common1Count = seqColService.getCommonElementsCount(A1, A2);
        Integer common2Count = seqColService.getCommonElementsCount(A2, B2);

        assertEquals(3, common1Count);
        assertEquals(2, common2Count);

    }
}