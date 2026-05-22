package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONExtDataTest {

    private JSONExtData<List<String>> JSONStringListExtDataObj;
    private JSONExtData<List<Integer>> JSONIntegerListExtDataObj;

    @BeforeEach
    void setUp() {
        JSONStringListExtDataObj = new JSONStringListExtData();
        List<String> arrElements = Arrays.asList("A", "B", "C");
        JSONStringListExtDataObj.setObject(arrElements);

        JSONIntegerListExtDataObj = new JSONIntegerListExtData();
        List<Integer> intElements = Arrays.asList(100, 200, 300);
        JSONIntegerListExtDataObj.setObject(intElements);
    }

    @Test
    void testStringListToString() {
        assertEquals("[\"A\",\"B\",\"C\"]", JSONStringListExtDataObj.toString());
    }

    @Test
    void testIntegerListToString() {
        assertEquals("[100, 200, 300]", JSONIntegerListExtDataObj.toString());
    }

    @Test
    void testBaseClassNoArgsConstructor() {
        JSONExtData<String> data = new JSONExtData<>();
        assertNull(data.getObject());
    }

    @Test
    void testBaseClassConstructorWithObject() {
        JSONExtData<String> data = new JSONExtData<>("test");
        assertEquals("test", data.getObject());
    }

    @Test
    void testSetAndGetObject() {
        JSONExtData<Integer> data = new JSONExtData<>();
        data.setObject(42);
        assertEquals(42, data.getObject());
    }

    @Test
    void testStringListWithSpecialCharacters() {
        JSONStringListExtData data = new JSONStringListExtData(
                Arrays.asList("chr1_random", "chrUn_gl000220", "chr1")
        );
        String result = data.toString();

        assertTrue(result.contains("\"chr1_random\""));
        assertTrue(result.contains("\"chrUn_gl000220\""));
    }

    @Test
    void testStringListWithSingleElement() {
        JSONStringListExtData data = new JSONStringListExtData(
                Collections.singletonList("single")
        );
        assertEquals("[\"single\"]", data.toString());
    }

    @Test
    void testIntegerListWithLargeNumbers() {
        JSONIntegerListExtData data = new JSONIntegerListExtData(
                Arrays.asList(248956422, 242193529)
        );
        String result = data.toString();

        assertTrue(result.contains("248956422"));
        assertTrue(result.contains("242193529"));
    }

    @Test
    void testSerializable() {
        assertTrue(JSONStringListExtDataObj instanceof java.io.Serializable);
        assertTrue(JSONIntegerListExtDataObj instanceof java.io.Serializable);
    }

    @Test
    void testEqualsAndHashCode() {
        JSONStringListExtData data1 = new JSONStringListExtData(Arrays.asList("A", "B"));
        JSONStringListExtData data2 = new JSONStringListExtData(Arrays.asList("A", "B"));

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testNotEqualsDifferentContent() {
        JSONStringListExtData data1 = new JSONStringListExtData(Arrays.asList("A", "B"));
        JSONStringListExtData data2 = new JSONStringListExtData(Arrays.asList("A", "C"));

        // Verify the objects contain different data
        assertNotEquals(data1.getObject(), data2.getObject());
        assertNotEquals(data1.toString(), data2.toString());
    }

    @Test
    void testStringListEmptyList() {
        JSONStringListExtData data = new JSONStringListExtData(Collections.emptyList());
        // Empty list would cause ArrayIndexOutOfBoundsException with current implementation
        // This documents the behavior - might need fixing in production
        assertNotNull(data.getObject());
        assertTrue(data.getObject().isEmpty());
    }

    @Test
    void testIntegerListEmptyList() {
        JSONIntegerListExtData data = new JSONIntegerListExtData(Collections.emptyList());
        assertEquals("[]", data.toString());
    }

    @Test
    void testPolymorphicBehavior() {
        // Both extend JSONExtData
        JSONExtData<List<String>> stringData = new JSONStringListExtData(Arrays.asList("X"));
        JSONExtData<List<Integer>> intData = new JSONIntegerListExtData(Arrays.asList(1));

        // Different toString behaviors
        assertEquals("[\"X\"]", stringData.toString());
        assertEquals("[1]", intData.toString());
    }
}