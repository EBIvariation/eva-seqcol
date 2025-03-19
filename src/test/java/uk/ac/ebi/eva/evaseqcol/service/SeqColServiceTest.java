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
import uk.ac.ebi.eva.evaseqcol.dto.PaginatedResponse;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColServiceTest {


    private final String TEST_DIGEST = "AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX"; // seqCol inserted by the SeqColWriter

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
        seqColWriter.create(); // This will write some seqCol objects to the database
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

        assertTrue(seqColService.unbalancedDuplicatesPresent(A, B));
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

    @Test
    void commonElementsCountTest() {
        List<String> listA1 = new ArrayList<>(Arrays.asList("chr1", "chr2", "chr3", "A"));
        List<String> listA2 = new ArrayList<>(Arrays.asList("chr1", "chr5", "chr3", "M", "A"));

        assertEquals(3, seqColService.getCommonElementsCount(listA1, listA2));
    }

    @Test
    @Transactional
    void testIngestSeqColFastaFile() throws IOException {
        // base_fa
        String base_fa_expected_digest = "XZlrcEGi6mlopZ2uD8ObHkQB1d0oDwKk";
        String base_fa_acc = "base_fa_acc";
        String base_fa_fasta = ">chrX\n" +
                "TTGGGGAA\n" +
                ">chr1\n" +
                "GGAA\n" +
                ">chr2\n" +
                "GCGC";
        seqColService.fetchAndInsertAllSeqColInFastaFile(base_fa_acc, base_fa_fasta);
        Optional<? extends SeqColEntity> optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(base_fa_expected_digest, 1);
        SeqColLevelOneEntity seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(base_fa_expected_digest, seqColLevelOneEntity.getDigest());

        // different_names
        String diff_name_expected_digest = "QvT5tAQ0B8Vkxd-qFftlzEk2QyfPtgOv";
        String diff_name_acc = "diff_name_acc";
        String diff_name_fasta = ">X\n" +
                "TTGGGGAA\n" +
                ">1\n" +
                "GGAA\n" +
                ">2\n" +
                "GCGC";
        seqColService.fetchAndInsertAllSeqColInFastaFile(diff_name_acc, diff_name_fasta);
        optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(diff_name_expected_digest, 1);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(diff_name_expected_digest, seqColLevelOneEntity.getDigest());

        // different_order
        String diff_order_expected_digest = "Tpdsg75D4GKCGEHtIiDSL9Zx-DSuX5V8";
        String diff_order_acc = "diff_order_acc";
        String diff_order_fasta = ">chr1\n" +
                "GGAA\n" +
                ">chr2\n" +
                "GCGC\n" +
                ">chrX\n" +
                "TTGGGGAA";
        seqColService.fetchAndInsertAllSeqColInFastaFile(diff_order_acc, diff_order_fasta);
        optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(diff_order_expected_digest, 1);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(diff_order_expected_digest, seqColLevelOneEntity.getDigest());

        // pair_swap
        String pair_swap_expected_digest = "UNGAdNDmBbQbHihecPPFxwTydTcdFKxL";
        String pair_swap_acc = "pair_swap_acc";
        String pair_swap_fasta = ">chr2\n" +
                "TTGGGGAA\n" +
                ">chr1\n" +
                "GGAA\n" +
                ">chrX\n" +
                "GCGC";
        seqColService.fetchAndInsertAllSeqColInFastaFile(pair_swap_acc, pair_swap_fasta);
        optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(pair_swap_expected_digest, 1);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(pair_swap_expected_digest, seqColLevelOneEntity.getDigest());

        // subset
        String subset_expected_digest = "sv7GIP1K0qcskIKF3iaBmQpaum21vH74";
        String subset_acc = "subset_acc";
        String subset_fasta = ">chrX\n" +
                "TTGGGGAA\n" +
                ">chr1\n" +
                "GGAA";
        seqColService.fetchAndInsertAllSeqColInFastaFile(subset_acc, subset_fasta);
        optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(subset_expected_digest, 1);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(subset_expected_digest, seqColLevelOneEntity.getDigest());

        // swap_wo
        String swap_wo_expected_digest = "aVzHaGFlUDUNF2IEmNdzS_A8lCY0stQH";
        String swap_wo_acc = "swap_wo_acc";
        String swap_wo_fasta = ">chrX\n" +
                "TTGGGGAA\n" +
                ">chr2\n" +
                "GGAA\n" +
                ">chr1\n" +
                "GCGC";
        seqColService.fetchAndInsertAllSeqColInFastaFile(swap_wo_acc, swap_wo_fasta);
        optionalSeqColEntity = seqColService.getSeqColByDigestAndLevel(swap_wo_expected_digest, 1);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(swap_wo_expected_digest, seqColLevelOneEntity.getDigest());

        // test get SeqColAttribute

        // attribute names
        List<String> nameAttributeResult = seqColService.getSeqColAttribute("XZlrcEGi6mlopZ2uD8ObHkQB1d0oDwKk",
                SeqColExtendedDataEntity.AttributeType.names);
        assertEquals(Arrays.asList("chrX", "chr1", "chr2"), nameAttributeResult);

        // attribute sequences
        List<String> sequenceAttributeResult = seqColService.getSeqColAttribute("aVzHaGFlUDUNF2IEmNdzS_A8lCY0stQH",
                SeqColExtendedDataEntity.AttributeType.sequences);
        assertEquals(Arrays.asList("SQ.iYtREV555dUFKg2_agSJW6suquUyPpMw", "SQ.YBbVX0dLKG1ieEDCiMmkrTZFt_Z5Vdaj",
                "SQ.AcLxtBuKEPk_7PGE_H4dGElwZHCujwH6"), sequenceAttributeResult);

        // attribute lengths
        List<String> lengthAttributeResult = seqColService.getSeqColAttribute("sv7GIP1K0qcskIKF3iaBmQpaum21vH74",
                SeqColExtendedDataEntity.AttributeType.lengths);
        assertEquals(Arrays.asList(8, 4), lengthAttributeResult);

        // test get list collection

        // page=0, pageSize=5, no filters  (get all results, 1st page contains 5)
        PaginatedResponse<SeqColLevelOneEntity> seqColList = seqColService.getSeqColList(0, 5, Collections.emptyMap());
        PaginatedResponse.PaginationInfo pagination = seqColList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 8);
        assertEquals(seqColList.getResults().size(), 5);
        assertEquals(Arrays.asList("dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6",
                        "dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6",
                        "0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr",
                        "0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr",
                        "7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h"),
                seqColList.getResults().stream()
                        .map(levelOneEntity -> levelOneEntity.getSeqColLevel1Object().getSequences())
                        .collect(Collectors.toList())
        );

        // page=1, pageSize=5, no filters  (get all results, 2nd page contains 3 - page number starts from 0)
        seqColList = seqColService.getSeqColList(1, 5, Collections.emptyMap());
        pagination = seqColList.getPagination();
        assertEquals(pagination.getPage(), 1);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 8);
        assertEquals(seqColList.getResults().size(), 3);
        assertEquals(Arrays.asList("0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr", "3ZP38SZcoc9wN7jsRyNSP9mQ1a3TUoUF",
                        "0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr"),
                seqColList.getResults().stream()
                        .map(levelOneEntity -> levelOneEntity.getSeqColLevel1Object().getSequences())
                        .collect(Collectors.toList())
        );

        // page=0, pageSize=5, filter on sequences and names  (1 matching record)
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("sequences", "7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h");
        filterMap.put("names", "dOAOfPGkf3wAf3CUsbjVTKhY9Wq2DL6f");
        seqColList = seqColService.getSeqColList(0, 5, filterMap);
        pagination = seqColList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 1);
        assertEquals(seqColList.getResults().size(), 1);
        assertEquals("7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h", seqColList.getResults().get(0).getSeqColLevel1Object().getSequences());
        assertEquals("dOAOfPGkf3wAf3CUsbjVTKhY9Wq2DL6f", seqColList.getResults().get(0).getSeqColLevel1Object().getNames());

        // page=0, pageSize=5, page size beyond total results
        seqColList = seqColService.getSeqColList(2, 5, filterMap);
        pagination = seqColList.getPagination();
        assertEquals(pagination.getPage(), 2);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 1);
        assertEquals(seqColList.getResults().size(), 0);

        // no matching record found
        filterMap.put("sequences", "7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h");
        filterMap.put("names", "wrong_value");
        seqColList = seqColService.getSeqColList(0, 5, filterMap);
        pagination = seqColList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 0);
        assertEquals(seqColList.getResults().size(), 0);
    }
}