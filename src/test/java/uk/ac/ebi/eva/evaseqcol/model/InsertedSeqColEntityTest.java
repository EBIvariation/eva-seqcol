package uk.ac.ebi.eva.evaseqcol.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertedSeqColEntityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testNoArgsConstructor() {
        InsertedSeqColEntity entity = new InsertedSeqColEntity();

        assertNull(entity.getDigest());
        assertNull(entity.getNamingConvention());
    }

    @Test
    void testAllArgsConstructor() {
        InsertedSeqColEntity entity = new InsertedSeqColEntity("3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq", "GENBANK");

        assertEquals("3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq", entity.getDigest());
        assertEquals("GENBANK", entity.getNamingConvention());
    }

    @Test
    void testSettersAndGetters() {
        InsertedSeqColEntity entity = new InsertedSeqColEntity();

        entity.setDigest("abc123def456");
        entity.setNamingConvention("ENA");

        assertEquals("abc123def456", entity.getDigest());
        assertEquals("ENA", entity.getNamingConvention());
    }

    @Test
    void testJsonSerializationSnakeCase() throws JsonProcessingException {
        InsertedSeqColEntity entity = new InsertedSeqColEntity("digest123", "UCSC");

        String json = objectMapper.writeValueAsString(entity);

        // Verify snake_case for naming_convention
        assertTrue(json.contains("\"naming_convention\""));
        assertFalse(json.contains("\"namingConvention\""));

        // Verify values
        assertTrue(json.contains("\"digest123\""));
        assertTrue(json.contains("\"UCSC\""));
    }

    @Test
    void testJsonDeserialization() throws JsonProcessingException {
        String json = "{\"digest\":\"myDigest\",\"naming_convention\":\"GENBANK\"}";

        InsertedSeqColEntity entity = objectMapper.readValue(json, InsertedSeqColEntity.class);

        assertEquals("myDigest", entity.getDigest());
        assertEquals("GENBANK", entity.getNamingConvention());
    }

    @Test
    void testEqualsAndHashCode() {
        InsertedSeqColEntity entity1 = new InsertedSeqColEntity("digest1", "ENA");
        InsertedSeqColEntity entity2 = new InsertedSeqColEntity("digest1", "ENA");
        InsertedSeqColEntity entity3 = new InsertedSeqColEntity("digest2", "ENA");

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1, entity3);
    }

    @Test
    void testDifferentNamingConventions() {
        String[] conventions = {"ENA", "GENBANK", "UCSC", "TEST"};

        for (String convention : conventions) {
            InsertedSeqColEntity entity = new InsertedSeqColEntity("digest", convention);
            assertEquals(convention, entity.getNamingConvention());
        }
    }

    @Test
    void testToString() {
        InsertedSeqColEntity entity = new InsertedSeqColEntity("testDigest", "GENBANK");

        String toString = entity.toString();

        // Lombok @Data generates toString
        assertTrue(toString.contains("testDigest"));
        assertTrue(toString.contains("GENBANK"));
    }
}
