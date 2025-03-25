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
        Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestLevel1(TEST_DIGEST);
        assertTrue(levelOneEntity.isPresent());
        Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestLevel2(TEST_DIGEST);
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
        Optional<? extends SeqColEntity> optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(base_fa_expected_digest);
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
        optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(diff_name_expected_digest);
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
        optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(diff_order_expected_digest);
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
        optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(pair_swap_expected_digest);
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
        optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(subset_expected_digest);
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
        optionalSeqColEntity = seqColService.getSeqColByDigestLevel1(swap_wo_expected_digest);
        seqColLevelOneEntity = (SeqColLevelOneEntity) optionalSeqColEntity.get();
        assertEquals(swap_wo_expected_digest, seqColLevelOneEntity.getDigest());

        // test get SeqColAttribute

        // attribute names
        Optional<List<String>> nameAttributeResult = seqColService.getSeqColAttribute("Fw1r9eRxfOZD98KKrhlYQNEdSRHoVxAG",
                SeqColExtendedDataEntity.AttributeType.names);
        assertEquals(Arrays.asList("chrX", "chr1", "chr2"), nameAttributeResult.get());

        // attribute sequences
        Optional<List<String>> sequenceAttributeResult = seqColService.getSeqColAttribute("0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr",
                SeqColExtendedDataEntity.AttributeType.sequences);
        assertEquals(Arrays.asList("SQ.iYtREV555dUFKg2_agSJW6suquUyPpMw", "SQ.YBbVX0dLKG1ieEDCiMmkrTZFt_Z5Vdaj",
                "SQ.AcLxtBuKEPk_7PGE_H4dGElwZHCujwH6"), sequenceAttributeResult.get());

        // attribute lengths
        Optional<List<String>> lengthAttributeResult = seqColService.getSeqColAttribute("cGRMZIb3AVgkcAfNv39RN7hnT5Chk7RX",
                SeqColExtendedDataEntity.AttributeType.lengths);
        assertEquals(Arrays.asList(8, 4, 4), lengthAttributeResult.get());

        // attribute md5
        Optional<List<String>> md5AttributeResult = seqColService.getSeqColAttribute("d7c3uC4RIphVHnBeQEwUKt3sIzm-XJ7l",
                SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);
        assertEquals(Arrays.asList("5f63cfaa3ef61f88c9635fb9d18ec945", "31fc6ca291a32fb9df82b85e5f077e31", "92c6a56c9e9459d8a42b96f7884710bc"), md5AttributeResult.get());

        // attribute sorted name length pairs
        Optional<List<String>> sortedNameLengthAttributeResult = seqColService.getSeqColAttribute("EXmqru5BC4Nu8beq86XdCJrEb6jg6-Z_",
                SeqColExtendedDataEntity.AttributeType.sortedNameLengthPairs);
        assertEquals(Arrays.asList("ESV_rcaJ-zgrfBr6sJ7J9kqldWX2gG_K", "I0_AywNhq4XLlcc1vZexw5cHygCK_bLh", "V2tENgwhEWrKc8UNgFzrMwx7H3JgoIuq"), sortedNameLengthAttributeResult.get());

        // not va vaid digest
        nameAttributeResult = seqColService.getSeqColAttribute("test_digest",
                SeqColExtendedDataEntity.AttributeType.names);
        assertFalse(nameAttributeResult.isPresent());

        // valid digest but belongs to some other attribute
        nameAttributeResult = seqColService.getSeqColAttribute("0uDQVLuHaOZi1u76LjV__yrVUIz9Bwhr",
                SeqColExtendedDataEntity.AttributeType.names);
        assertFalse(nameAttributeResult.isPresent());

        // test get list collection

        // page=0, pageSize=5, no filters  (get all results, 1st page contains 5)
        PaginatedResponse<String> seqColDigestList = seqColService.getSeqColList(0, 5, Collections.emptyMap());
        PaginatedResponse.PaginationInfo pagination = seqColDigestList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 8);
        assertEquals(seqColDigestList.getResults().size(), 5);
        assertEquals(Arrays.asList("AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX", "ySaGQd8xaXhhfyR5PsTBp4ggbXXVub7w",
                        "XZlrcEGi6mlopZ2uD8ObHkQB1d0oDwKk", "QvT5tAQ0B8Vkxd-qFftlzEk2QyfPtgOv",
                        "Tpdsg75D4GKCGEHtIiDSL9Zx-DSuX5V8"),
                seqColDigestList.getResults());

        // page=1, pageSize=5, no filters  (get all results, 2nd page contains 3 - page number starts from 0)
        seqColDigestList = seqColService.getSeqColList(1, 5, Collections.emptyMap());
        pagination = seqColDigestList.getPagination();
        assertEquals(pagination.getPage(), 1);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 8);
        assertEquals(seqColDigestList.getResults().size(), 3);
        assertEquals(Arrays.asList("UNGAdNDmBbQbHihecPPFxwTydTcdFKxL", "sv7GIP1K0qcskIKF3iaBmQpaum21vH74",
                        "aVzHaGFlUDUNF2IEmNdzS_A8lCY0stQH"),
                seqColDigestList.getResults());

        // page=0, pageSize=5, filter on sequences and names  (1 matching record)
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("sequences", "7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h");
        filterMap.put("names", "dOAOfPGkf3wAf3CUsbjVTKhY9Wq2DL6f");
        seqColDigestList = seqColService.getSeqColList(0, 5, filterMap);
        pagination = seqColDigestList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 1);
        assertEquals(seqColDigestList.getResults().size(), 1);
        assertEquals("Tpdsg75D4GKCGEHtIiDSL9Zx-DSuX5V8", seqColDigestList.getResults().get(0));

        // page=0, pageSize=5, page size beyond total results
        seqColDigestList = seqColService.getSeqColList(2, 5, filterMap);
        pagination = seqColDigestList.getPagination();
        assertEquals(pagination.getPage(), 2);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 1);
        assertEquals(seqColDigestList.getResults().size(), 0);

        // no matching record found
        filterMap.put("sequences", "7t6Ulz6OeUWu6FBxntbvFKOl8w3icl2h");
        filterMap.put("names", "wrong_value");
        seqColDigestList = seqColService.getSeqColList(0, 5, filterMap);
        pagination = seqColDigestList.getPagination();
        assertEquals(pagination.getPage(), 0);
        assertEquals(pagination.getPageSize(), 5);
        assertEquals(pagination.getTotal(), 0);
        assertEquals(seqColDigestList.getResults().size(), 0);
    }
}