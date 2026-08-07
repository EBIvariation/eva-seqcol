package uk.ac.ebi.eva.evaseqcol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONLevelOneTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testNoArgsConstructor() {
        JSONLevelOne levelOne = new JSONLevelOne();

        assertNull(levelOne.getSequences());
        assertNull(levelOne.getMd5DigestsOfSequences());
        assertNull(levelOne.getNames());
        assertNull(levelOne.getLengths());
        assertNull(levelOne.getSortedNameLengthPairs());
    }

    @Test
    void testAllArgsConstructor() {
        JSONLevelOne levelOne = new JSONLevelOne(
                "seq_digest",
                "md5_digest",
                "names_digest",
                "lengths_digest",
                "sorted_pairs_digest"
        );

        assertEquals("seq_digest", levelOne.getSequences());
        assertEquals("md5_digest", levelOne.getMd5DigestsOfSequences());
        assertEquals("names_digest", levelOne.getNames());
        assertEquals("lengths_digest", levelOne.getLengths());
        assertEquals("sorted_pairs_digest", levelOne.getSortedNameLengthPairs());
    }

    @Test
    void testFluentSetters() {
        JSONLevelOne levelOne = new JSONLevelOne()
                .setSequences("sequences_abc")
                .setMd5DigestsOfSequences("md5_xyz")
                .setNames("names_123")
                .setLengths("lengths_456");

        assertEquals("sequences_abc", levelOne.getSequences());
        assertEquals("md5_xyz", levelOne.getMd5DigestsOfSequences());
        assertEquals("names_123", levelOne.getNames());
        assertEquals("lengths_456", levelOne.getLengths());
    }

    @Test
    void testFluentSettersReturnSameInstance() {
        JSONLevelOne levelOne = new JSONLevelOne();

        JSONLevelOne returnedFromSequences = levelOne.setSequences("test");
        JSONLevelOne returnedFromMd5 = returnedFromSequences.setMd5DigestsOfSequences("test2");
        JSONLevelOne returnedFromNames = returnedFromMd5.setNames("test3");
        JSONLevelOne returnedFromLengths = returnedFromNames.setLengths("test4");

        // All should be the same instance
        assertSame(levelOne, returnedFromSequences);
        assertSame(levelOne, returnedFromMd5);
        assertSame(levelOne, returnedFromNames);
        assertSame(levelOne, returnedFromLengths);
    }

    @Test
    void testJsonSerializationSnakeCase() throws JsonProcessingException {
        JSONLevelOne levelOne = new JSONLevelOne(
                "seq_digest",
                "md5_digest",
                "names_digest",
                "lengths_digest",
                "sorted_pairs_digest"
        );

        String json = objectMapper.writeValueAsString(levelOne);

        // Verify snake_case property names
        assertTrue(json.contains("\"md5_sequences\""));
        assertTrue(json.contains("\"sorted_name_length_pairs\""));

        // Verify camelCase is NOT used
        assertFalse(json.contains("\"md5DigestsOfSequences\""));
        assertFalse(json.contains("\"sortedNameLengthPairs\""));

        // Verify standard property names
        assertTrue(json.contains("\"sequences\""));
        assertTrue(json.contains("\"names\""));
        assertTrue(json.contains("\"lengths\""));
    }

    @Test
    void testJsonDeserialization() throws JsonProcessingException {
        String json = "{" +
                "\"sequences\":\"seq_abc\"," +
                "\"md5_sequences\":\"md5_def\"," +
                "\"names\":\"names_ghi\"," +
                "\"lengths\":\"lengths_jkl\"," +
                "\"sorted_name_length_pairs\":\"sorted_mno\"" +
                "}";

        JSONLevelOne levelOne = objectMapper.readValue(json, JSONLevelOne.class);

        assertEquals("seq_abc", levelOne.getSequences());
        assertEquals("md5_def", levelOne.getMd5DigestsOfSequences());
        assertEquals("names_ghi", levelOne.getNames());
        assertEquals("lengths_jkl", levelOne.getLengths());
        assertEquals("sorted_mno", levelOne.getSortedNameLengthPairs());
    }

    @Test
    void testJsonSerializationRoundTrip() throws JsonProcessingException {
        JSONLevelOne original = new JSONLevelOne(
                "digest1",
                "digest2",
                "digest3",
                "digest4",
                "digest5"
        );

        String json = objectMapper.writeValueAsString(original);
        JSONLevelOne deserialized = objectMapper.readValue(json, JSONLevelOne.class);

        assertEquals(original, deserialized);
    }

    @Test
    void testEqualsAndHashCode() {
        JSONLevelOne level1 = new JSONLevelOne("a", "b", "c", "d", "e");
        JSONLevelOne level2 = new JSONLevelOne("a", "b", "c", "d", "e");
        JSONLevelOne level3 = new JSONLevelOne("x", "b", "c", "d", "e");

        assertEquals(level1, level2);
        assertEquals(level1.hashCode(), level2.hashCode());
        assertNotEquals(level1, level3);
    }

    @Test
    void testSerializable() {
        JSONLevelOne levelOne = new JSONLevelOne("a", "b", "c", "d", "e");

        // Verify it implements Serializable
        assertTrue(levelOne instanceof java.io.Serializable);
    }

    @Test
    void testWithRealDigestValues() {
        // Test with realistic SHA-512 digest values (base64url encoded)
        String seqDigest = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq";
        String md5Digest = "rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE";
        String namesDigest = "XYZ123abc-def456_ghi789";
        String lengthsDigest = "ABC789xyz-mno012_pqr345";
        String sortedPairsDigest = "QRS456uvw-xyz789_abc012";

        JSONLevelOne levelOne = new JSONLevelOne(
                seqDigest, md5Digest, namesDigest, lengthsDigest, sortedPairsDigest
        );

        assertEquals(seqDigest, levelOne.getSequences());
        assertEquals(md5Digest, levelOne.getMd5DigestsOfSequences());
    }

    @Test
    void testPartiallyPopulated() {
        // Only required fields set (sequences, names, lengths)
        JSONLevelOne levelOne = new JSONLevelOne()
                .setSequences("seq")
                .setNames("names")
                .setLengths("lengths");

        assertNotNull(levelOne.getSequences());
        assertNotNull(levelOne.getNames());
        assertNotNull(levelOne.getLengths());
        assertNull(levelOne.getMd5DigestsOfSequences());
        assertNull(levelOne.getSortedNameLengthPairs());
    }

    @Test
    void testJsonWithNullValues() throws JsonProcessingException {
        JSONLevelOne levelOne = new JSONLevelOne();
        levelOne.setSequences("seq_only");

        String json = objectMapper.writeValueAsString(levelOne);

        // Null values should be serialized
        assertTrue(json.contains("\"sequences\":\"seq_only\""));
    }

    @Test
    void testLombokGeneratedSettersOverridden() {
        // The class has custom fluent setters that override Lombok
        // Verify they work as expected
        JSONLevelOne levelOne = new JSONLevelOne();

        // Custom fluent setters return this
        assertSame(levelOne, levelOne.setSequences("test"));

        // Lombok @Data also generates standard setters via reflection
        // but our custom ones take precedence
        levelOne.setSequences("updated");
        assertEquals("updated", levelOne.getSequences());
    }

    @Test
    void testToString() {
        JSONLevelOne levelOne = new JSONLevelOne("s", "m", "n", "l", "p");

        String toString = levelOne.toString();

        // Lombok @Data generates toString
        assertNotNull(toString);
        assertTrue(toString.contains("sequences"));
        assertTrue(toString.contains("names"));
        assertTrue(toString.contains("lengths"));
    }
}
