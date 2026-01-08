package uk.ac.ebi.eva.evaseqcol.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameLengthPairEntityTest {

    @Test
    void testNoArgsConstructor() {
        NameLengthPairEntity entity = new NameLengthPairEntity();

        assertNull(entity.getName());
        assertNull(entity.getLength());
    }

    @Test
    void testAllArgsConstructor() {
        NameLengthPairEntity entity = new NameLengthPairEntity("chr1", 248956422);

        assertEquals("chr1", entity.getName());
        assertEquals(248956422, entity.getLength());
    }

    @Test
    void testSettersAndGetters() {
        NameLengthPairEntity entity = new NameLengthPairEntity();

        entity.setName("chrX");
        entity.setLength(156040895);

        assertEquals("chrX", entity.getName());
        assertEquals(156040895, entity.getLength());
    }

    @Test
    void testToStringFormat() {
        NameLengthPairEntity entity = new NameLengthPairEntity("chr1", 248956422);

        String result = entity.toString();

        // Verify the custom toString format
        assertEquals("{    \"length\":\"248956422\",    \"name\":\"chr1\"}", result);
    }

    @Test
    void testToStringWithDifferentValues() {
        // Test with small values
        NameLengthPairEntity smallEntity = new NameLengthPairEntity("chrM", 16569);
        assertTrue(smallEntity.toString().contains("\"length\":\"16569\""));
        assertTrue(smallEntity.toString().contains("\"name\":\"chrM\""));

        // Test with large chromosome length
        NameLengthPairEntity largeEntity = new NameLengthPairEntity("chr1", 248956422);
        assertTrue(largeEntity.toString().contains("\"length\":\"248956422\""));
    }

    @Test
    void testToStringJsonStructure() {
        NameLengthPairEntity entity = new NameLengthPairEntity("test", 100);
        String result = entity.toString();

        // Verify it starts and ends correctly
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));

        // Verify it contains length before name (as per implementation)
        int lengthIndex = result.indexOf("\"length\"");
        int nameIndex = result.indexOf("\"name\"");
        assertTrue(lengthIndex < nameIndex, "length should appear before name in toString");
    }

    @Test
    void testEqualsAndHashCode() {
        NameLengthPairEntity entity1 = new NameLengthPairEntity("chr1", 1000);
        NameLengthPairEntity entity2 = new NameLengthPairEntity("chr1", 1000);
        NameLengthPairEntity entity3 = new NameLengthPairEntity("chr2", 1000);
        NameLengthPairEntity entity4 = new NameLengthPairEntity("chr1", 2000);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1, entity4);
    }

    @Test
    void testWithNullValues() {
        NameLengthPairEntity entity = new NameLengthPairEntity();
        entity.setName(null);
        entity.setLength(null);

        assertNull(entity.getName());
        assertNull(entity.getLength());
    }

    @Test
    void testWithSpecialCharactersInName() {
        NameLengthPairEntity entity = new NameLengthPairEntity("chr1_random", 50000);

        assertEquals("chr1_random", entity.getName());
        assertTrue(entity.toString().contains("\"name\":\"chr1_random\""));
    }

    @Test
    void testWithZeroLength() {
        NameLengthPairEntity entity = new NameLengthPairEntity("empty", 0);

        assertEquals(0, entity.getLength());
        assertTrue(entity.toString().contains("\"length\":\"0\""));
    }

    @Test
    void testTypicalChromosomeData() {
        // Test with realistic human chromosome data
        String[][] chromosomes = {
                {"chr1", "248956422"},
                {"chr2", "242193529"},
                {"chrX", "156040895"},
                {"chrY", "57227415"},
                {"chrM", "16569"}
        };

        for (String[] chrData : chromosomes) {
            NameLengthPairEntity entity = new NameLengthPairEntity(
                    chrData[0],
                    Integer.parseInt(chrData[1])
            );
            assertEquals(chrData[0], entity.getName());
            assertEquals(Integer.parseInt(chrData[1]), entity.getLength());
        }
    }
}
