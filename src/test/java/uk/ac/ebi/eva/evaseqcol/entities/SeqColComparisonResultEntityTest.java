package uk.ac.ebi.eva.evaseqcol.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeqColComparisonResultEntityTest {

    private SeqColComparisonResultEntity entity;

    @BeforeEach
    void setUp() {
        entity = new SeqColComparisonResultEntity();
    }

    @Test
    void testDefaultConstructorInitializesCollections() {
        assertNotNull(entity.getDigests());
        assertNotNull(entity.getAttributes());
        assertNotNull(entity.getArray_elements());

        // Verify array_elements has all required sub-maps
        assertTrue(entity.getArray_elements().containsKey("a_count"));
        assertTrue(entity.getArray_elements().containsKey("b_count"));
        assertTrue(entity.getArray_elements().containsKey("a_and_b_count"));
        assertTrue(entity.getArray_elements().containsKey("a_and_b_same_order"));
    }

    @Test
    void testPutIntoDigests() {
        entity.putIntoDigests("a", "digest_a_123");
        entity.putIntoDigests("b", "digest_b_456");

        assertEquals("digest_a_123", entity.getDigests().get("a"));
        assertEquals("digest_b_456", entity.getDigests().get("b"));
        assertEquals(2, entity.getDigests().size());
    }

    @Test
    void testDigestsAreSorted() {
        // Add in reverse order
        entity.putIntoDigests("b", "digest_b");
        entity.putIntoDigests("a", "digest_a");

        // TreeMap should maintain sorted order
        String[] keys = entity.getDigests().keySet().toArray(new String[0]);
        assertEquals("a", keys[0]);
        assertEquals("b", keys[1]);
    }

    @Test
    void testPutIntoArrays() {
        List<String> aOnly = Arrays.asList("attr1", "attr2");
        List<String> bOnly = Arrays.asList("attr3");
        List<String> aAndB = Arrays.asList("sequences", "names", "lengths");

        entity.putIntoArrays("a_only", aOnly);
        entity.putIntoArrays("b_only", bOnly);
        entity.putIntoArrays("a_and_b", aAndB);

        assertEquals(aOnly, entity.getAttributes().get("a_only"));
        assertEquals(bOnly, entity.getAttributes().get("b_only"));
        assertEquals(aAndB, entity.getAttributes().get("a_and_b"));
    }

    @Test
    void testAttributesAreSorted() {
        entity.putIntoArrays("b_only", Arrays.asList("x"));
        entity.putIntoArrays("a_only", Arrays.asList("y"));
        entity.putIntoArrays("a_and_b", Arrays.asList("z"));

        String[] keys = entity.getAttributes().keySet().toArray(new String[0]);
        // TreeMap maintains alphabetical order
        assertEquals("a_and_b", keys[0]);
        assertEquals("a_only", keys[1]);
        assertEquals("b_only", keys[2]);
    }

    @Test
    void testPutIntoArrayElementsWithIntegerValue() {
        entity.putIntoArrayElements("a_count", "sequences", 10);
        entity.putIntoArrayElements("a_count", "names", 10);
        entity.putIntoArrayElements("b_count", "sequences", 8);

        assertEquals(10, entity.getArray_elements().get("a_count").get("sequences"));
        assertEquals(10, entity.getArray_elements().get("a_count").get("names"));
        assertEquals(8, entity.getArray_elements().get("b_count").get("sequences"));
    }

    @Test
    void testPutIntoArrayElementsWithBooleanValue() {
        entity.putIntoArrayElements("a_and_b_same_order", "sequences", true);
        entity.putIntoArrayElements("a_and_b_same_order", "names", false);
        entity.putIntoArrayElements("a_and_b_same_order", "lengths", null);

        assertEquals(true, entity.getArray_elements().get("a_and_b_same_order").get("sequences"));
        assertEquals(false, entity.getArray_elements().get("a_and_b_same_order").get("names"));
        assertNull(entity.getArray_elements().get("a_and_b_same_order").get("lengths"));
    }

    @Test
    void testArrayElementsSubMapsAreSorted() {
        entity.putIntoArrayElements("a_count", "names", 5);
        entity.putIntoArrayElements("a_count", "lengths", 5);
        entity.putIntoArrayElements("a_count", "sequences", 5);

        String[] keys = entity.getArray_elements().get("a_count").keySet().toArray(new String[0]);
        // TreeMap maintains alphabetical order
        assertEquals("lengths", keys[0]);
        assertEquals("names", keys[1]);
        assertEquals("sequences", keys[2]);
    }

    @Test
    void testCompleteComparisonScenario() {
        // Simulate a complete comparison result
        String digestA = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq";
        String digestB = "rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE";

        // Set digests
        entity.putIntoDigests("a", digestA);
        entity.putIntoDigests("b", digestB);

        // Set attributes
        entity.putIntoArrays("a_only", Arrays.asList("custom_attr"));
        entity.putIntoArrays("b_only", Arrays.asList());
        entity.putIntoArrays("a_and_b", Arrays.asList("sequences", "names", "lengths"));

        // Set array element counts
        entity.putIntoArrayElements("a_count", "sequences", 25);
        entity.putIntoArrayElements("a_count", "names", 25);
        entity.putIntoArrayElements("a_count", "lengths", 25);

        entity.putIntoArrayElements("b_count", "sequences", 24);
        entity.putIntoArrayElements("b_count", "names", 24);
        entity.putIntoArrayElements("b_count", "lengths", 24);

        entity.putIntoArrayElements("a_and_b_count", "sequences", 24);
        entity.putIntoArrayElements("a_and_b_count", "names", 24);
        entity.putIntoArrayElements("a_and_b_count", "lengths", 24);

        entity.putIntoArrayElements("a_and_b_same_order", "sequences", true);
        entity.putIntoArrayElements("a_and_b_same_order", "names", true);
        entity.putIntoArrayElements("a_and_b_same_order", "lengths", true);

        // Verify complete structure
        assertEquals(digestA, entity.getDigests().get("a"));
        assertEquals(digestB, entity.getDigests().get("b"));

        assertEquals(1, entity.getAttributes().get("a_only").size());
        assertEquals(0, entity.getAttributes().get("b_only").size());
        assertEquals(3, entity.getAttributes().get("a_and_b").size());

        assertEquals(25, entity.getArray_elements().get("a_count").get("sequences"));
        assertEquals(24, entity.getArray_elements().get("b_count").get("sequences"));
        assertEquals(24, entity.getArray_elements().get("a_and_b_count").get("sequences"));
        assertEquals(true, entity.getArray_elements().get("a_and_b_same_order").get("sequences"));
    }

    @Test
    void testIdenticalSeqColsComparison() {
        // When comparing identical seqCols
        String sameDigest = "identicalDigest123";

        entity.putIntoDigests("a", sameDigest);
        entity.putIntoDigests("b", sameDigest);

        entity.putIntoArrays("a_only", Arrays.asList());
        entity.putIntoArrays("b_only", Arrays.asList());
        entity.putIntoArrays("a_and_b", Arrays.asList("sequences", "names", "lengths"));

        entity.putIntoArrayElements("a_count", "sequences", 10);
        entity.putIntoArrayElements("b_count", "sequences", 10);
        entity.putIntoArrayElements("a_and_b_count", "sequences", 10);
        entity.putIntoArrayElements("a_and_b_same_order", "sequences", true);

        // All elements match
        assertEquals(entity.getArray_elements().get("a_count").get("sequences"),
                entity.getArray_elements().get("b_count").get("sequences"));
        assertEquals(entity.getArray_elements().get("a_count").get("sequences"),
                entity.getArray_elements().get("a_and_b_count").get("sequences"));
    }

    @Test
    void testNullSameOrderValue() {
        // When less than 2 overlapping elements or unbalanced duplicates
        entity.putIntoArrayElements("a_and_b_same_order", "sequences", null);

        assertNull(entity.getArray_elements().get("a_and_b_same_order").get("sequences"));
    }

    @Test
    void testOverwriteDigestValue() {
        entity.putIntoDigests("a", "original_digest");
        entity.putIntoDigests("a", "new_digest");

        assertEquals("new_digest", entity.getDigests().get("a"));
        assertEquals(1, entity.getDigests().size());
    }

    @Test
    void testEqualsAndHashCode() {
        SeqColComparisonResultEntity entity1 = new SeqColComparisonResultEntity();
        entity1.putIntoDigests("a", "digest1");

        SeqColComparisonResultEntity entity2 = new SeqColComparisonResultEntity();
        entity2.putIntoDigests("a", "digest1");

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testEmptyArraysInAttributes() {
        entity.putIntoArrays("a_only", Arrays.asList());
        entity.putIntoArrays("b_only", Arrays.asList());

        assertTrue(entity.getAttributes().get("a_only").isEmpty());
        assertTrue(entity.getAttributes().get("b_only").isEmpty());
    }
}
