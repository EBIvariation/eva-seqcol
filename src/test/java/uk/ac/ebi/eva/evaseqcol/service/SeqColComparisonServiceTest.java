package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of the comparison helpers; they don't need the database
 */
class SeqColComparisonServiceTest {

    private final SeqColComparisonService comparisonService = new SeqColComparisonService(null, null);

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

        assertTrue(comparisonService.unbalancedDuplicatesPresent(A, B));
        assertFalse(comparisonService.unbalancedDuplicatesPresent(A1, B1));
        assertFalse(comparisonService.unbalancedDuplicatesPresent(A2, B2));
        assertTrue(comparisonService.unbalancedDuplicatesPresent(A3, B3));
    }

    @Test
    void isSameOrderTest() {
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

        assertTrue(comparisonService.isSameOrder(listA1, listB1));
        assertTrue(comparisonService.isSameOrder(listA2, listB2));
        assertTrue(comparisonService.isSameOrder(listA3, listB3));
        assertTrue(comparisonService.isSameOrder(listA4, listB4));
        assertFalse(comparisonService.isSameOrder(listA5, listB5));
    }

    @Test
    void commonElementsCountTest() {
        List<String> listA1 = new ArrayList<>(Arrays.asList("chr1", "chr2", "chr3", "A"));
        List<String> listA2 = new ArrayList<>(Arrays.asList("chr1", "chr5", "chr3", "M", "A"));

        assertEquals(3, comparisonService.getCommonElementsCount(listA1, listA2));
    }
}
