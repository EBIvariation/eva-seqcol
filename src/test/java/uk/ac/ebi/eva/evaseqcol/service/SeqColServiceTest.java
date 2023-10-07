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


    private final String TEST_DIGEST = "rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE"; // seqCol inserted by the SeqColWriter

    @Autowired
    private SeqColService seqColService;

    @Autowired
    private SeqColWriter seqColWriter;


    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14.0");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() throws IOException {
        //TODO: FIX
        //seqColWriter.write(); // This will write some seqCol objects to the database
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
    void A_And_B_Same_OrderTest() {
        List<String> listA1 = new ArrayList<>(Arrays.asList("chr1", "chr2", "chr3", "M"));
        List<String> listA2 = new ArrayList<>(Arrays.asList("1", "2", "2", "3"));
        List<String> listA3 = new ArrayList<>(Arrays.asList("ch1", "B", "ch2", "ch3"));
        List<String> listA4 = new ArrayList<>(Arrays.asList("ch1", "B", "ch2", "ch3"));
        List<String> listA5 = new ArrayList<>(Arrays.asList("1", "2", "2"));

        List<String> listB1 = new ArrayList<>(Arrays.asList("chr1", "chr2", "chr3"));
        List<String> listB2 = new ArrayList<>(Arrays.asList("1", "2", "2"));
        List<String> listB3 = new ArrayList<>(Arrays.asList("ch1", "A", "ch2", "ch3"));
        List<String> listB4 = new ArrayList<>(Arrays.asList("A", "ch1", "ch2", "ch3"));
        List<String> listB5 = new ArrayList<>(Arrays.asList("2", "1", "2"));

        assertTrue(seqColService.check_A_And_B_Same_Order(listA1, listB1));
        assertTrue(seqColService.check_A_And_B_Same_Order(listA2, listB2));
        assertTrue(seqColService.check_A_And_B_Same_Order(listA3, listB3));
        assertTrue(seqColService.check_A_And_B_Same_Order(listA4, listB4));
        assertFalse(seqColService.check_A_And_B_Same_Order(listA5, listB5));
    }

}