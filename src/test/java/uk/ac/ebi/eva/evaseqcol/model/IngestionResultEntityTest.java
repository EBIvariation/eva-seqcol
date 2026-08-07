package uk.ac.ebi.eva.evaseqcol.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngestionResultEntityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IngestionResultEntity entity;

    @BeforeEach
    void setUp() {
        entity = new IngestionResultEntity();
    }

    @Test
    void testDefaultConstructor() {
        assertNull(entity.getAssemblyAccession());
        assertEquals(0, entity.getNumberOfInsertedSeqcols());
        assertNotNull(entity.getInsertedSeqcols());
        assertTrue(entity.getInsertedSeqcols().isEmpty());
        assertNull(entity.getErrorMessage());
    }

    @Test
    void testAllArgsConstructor() {
        InsertedSeqColEntity inserted = new InsertedSeqColEntity("digest123", "GENBANK");
        IngestionResultEntity fullEntity = new IngestionResultEntity(
                "GCA_000001405.28",
                1,
                java.util.Collections.singletonList(inserted),
                null
        );

        assertEquals("GCA_000001405.28", fullEntity.getAssemblyAccession());
        assertEquals(1, fullEntity.getNumberOfInsertedSeqcols());
        assertEquals(1, fullEntity.getInsertedSeqcols().size());
        assertNull(fullEntity.getErrorMessage());
    }

    @Test
    void testSetAssemblyAccession() {
        entity.setAssemblyAccession("GCA_000001405.28");
        assertEquals("GCA_000001405.28", entity.getAssemblyAccession());
    }

    @Test
    void testAddInsertedSeqCol() {
        InsertedSeqColEntity inserted1 = new InsertedSeqColEntity("digest1", "GENBANK");
        InsertedSeqColEntity inserted2 = new InsertedSeqColEntity("digest2", "ENA");

        entity.addInsertedSeqCol(inserted1);
        assertEquals(1, entity.getInsertedSeqcols().size());

        entity.addInsertedSeqCol(inserted2);
        assertEquals(2, entity.getInsertedSeqcols().size());
        assertEquals("digest1", entity.getInsertedSeqcols().get(0).getDigest());
        assertEquals("digest2", entity.getInsertedSeqcols().get(1).getDigest());
    }

    @Test
    void testIncrementNumberOfInsertedSeqCols() {
        assertEquals(0, entity.getNumberOfInsertedSeqcols());

        entity.incrementNumberOfInsertedSeqCols();
        assertEquals(1, entity.getNumberOfInsertedSeqcols());

        entity.incrementNumberOfInsertedSeqCols();
        entity.incrementNumberOfInsertedSeqCols();
        assertEquals(3, entity.getNumberOfInsertedSeqcols());
    }

    @Test
    void testSetErrorMessage() {
        entity.setErrorMessage("Assembly not found");
        assertEquals("Assembly not found", entity.getErrorMessage());
    }

    @Test
    void testJsonSerializationSnakeCaseProperties() throws JsonProcessingException {
        entity.setAssemblyAccession("GCA_000001405.28");
        entity.addInsertedSeqCol(new InsertedSeqColEntity("abc123", "GENBANK"));
        entity.incrementNumberOfInsertedSeqCols();

        String json = objectMapper.writeValueAsString(entity);

        // Verify snake_case property names
        assertTrue(json.contains("\"assembly_accession\""));
        assertTrue(json.contains("\"num_inserted_seqcols\""));
        assertTrue(json.contains("\"inserted_seqcols\""));
        assertTrue(json.contains("\"error_message\""));

        // Verify values
        assertTrue(json.contains("GCA_000001405.28"));
        assertTrue(json.contains("abc123"));
    }

    @Test
    void testJsonDeserialization() throws JsonProcessingException {
        String json = "{\"assembly_accession\":\"GCA_123\",\"num_inserted_seqcols\":2," +
                "\"inserted_seqcols\":[{\"digest\":\"d1\",\"naming_convention\":\"ENA\"}]," +
                "\"error_message\":null}";

        IngestionResultEntity deserialized = objectMapper.readValue(json, IngestionResultEntity.class);

        assertEquals("GCA_123", deserialized.getAssemblyAccession());
        assertEquals(2, deserialized.getNumberOfInsertedSeqcols());
        assertEquals(1, deserialized.getInsertedSeqcols().size());
        assertNull(deserialized.getErrorMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        IngestionResultEntity entity1 = new IngestionResultEntity();
        entity1.setAssemblyAccession("GCA_000001405.28");
        entity1.setNumberOfInsertedSeqcols(1);

        IngestionResultEntity entity2 = new IngestionResultEntity();
        entity2.setAssemblyAccession("GCA_000001405.28");
        entity2.setNumberOfInsertedSeqcols(1);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testTypicalIngestionWorkflow() {
        // Simulate a typical ingestion workflow
        entity.setAssemblyAccession("GCA_000001405.28");

        // Add multiple seqcols for different naming conventions
        String[] conventions = {"GENBANK", "ENA", "UCSC"};
        for (int i = 0; i < conventions.length; i++) {
            InsertedSeqColEntity inserted = new InsertedSeqColEntity("digest" + i, conventions[i]);
            entity.addInsertedSeqCol(inserted);
            entity.incrementNumberOfInsertedSeqCols();
        }

        assertEquals(3, entity.getNumberOfInsertedSeqcols());
        assertEquals(3, entity.getInsertedSeqcols().size());
        assertNull(entity.getErrorMessage());
    }
}
