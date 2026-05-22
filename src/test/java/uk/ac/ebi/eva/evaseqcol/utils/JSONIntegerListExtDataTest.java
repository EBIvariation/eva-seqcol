package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONIntegerListExtDataTest {

    @Test
    void testNoArgsConstructor() {
        JSONIntegerListExtData data = new JSONIntegerListExtData();

        assertNull(data.getObject());
    }

    @Test
    void testConstructorWithList() {
        List<Integer> lengths = Arrays.asList(248956422, 242193529, 198295559);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        assertEquals(lengths, data.getObject());
        assertEquals(3, data.getObject().size());
    }

    @Test
    void testSetAndGetObject() {
        JSONIntegerListExtData data = new JSONIntegerListExtData();
        List<Integer> lengths = Arrays.asList(100, 200, 300);

        data.setObject(lengths);

        assertEquals(lengths, data.getObject());
    }

    @Test
    void testToString() {
        List<Integer> lengths = Arrays.asList(1000, 2000, 3000);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        String result = data.toString();

        // Integer list toString uses standard Java format
        assertEquals("[1000, 2000, 3000]", result);
    }

    @Test
    void testToStringWithSingleElement() {
        List<Integer> lengths = Collections.singletonList(16569);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        assertEquals("[16569]", data.toString());
    }

    @Test
    void testToStringWithLargeNumbers() {
        // Test with realistic chromosome lengths
        List<Integer> lengths = Arrays.asList(248956422, 242193529);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        String result = data.toString();

        assertTrue(result.contains("248956422"));
        assertTrue(result.contains("242193529"));
    }

    @Test
    void testToStringEmptyList() {
        List<Integer> emptyList = Collections.emptyList();
        JSONIntegerListExtData data = new JSONIntegerListExtData(emptyList);

        assertEquals("[]", data.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        List<Integer> lengths = Arrays.asList(100, 200, 300);

        JSONIntegerListExtData data1 = new JSONIntegerListExtData(lengths);
        JSONIntegerListExtData data2 = new JSONIntegerListExtData(lengths);

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testNotEqualsDifferentContent() {
        JSONIntegerListExtData data1 = new JSONIntegerListExtData(Arrays.asList(100, 200));
        JSONIntegerListExtData data2 = new JSONIntegerListExtData(Arrays.asList(100, 300));

        // Verify the objects contain different data
        assertNotEquals(data1.getObject(), data2.getObject());
        assertNotEquals(data1.toString(), data2.toString());
    }

    @Test
    void testInheritanceFromJSONExtData() {
        JSONIntegerListExtData data = new JSONIntegerListExtData(Arrays.asList(1, 2, 3));

        // Verify it's an instance of the parent class
        assertTrue(data instanceof JSONExtData);
    }

    @Test
    void testWithNegativeNumbers() {
        // Edge case: negative numbers (though unlikely in real usage)
        List<Integer> lengths = Arrays.asList(-1, 0, 1);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        assertEquals("[-1, 0, 1]", data.toString());
    }

    @Test
    void testWithZeroValues() {
        List<Integer> lengths = Arrays.asList(0, 0, 0);
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        assertEquals("[0, 0, 0]", data.toString());
    }

    @Test
    void testWithManyElements() {
        // Test with many elements like a real genome would have
        List<Integer> lengths = Arrays.asList(
                248956422, 242193529, 198295559, 190214555, 181538259,
                170805979, 159345973, 145138636, 138394717, 133797422
        );
        JSONIntegerListExtData data = new JSONIntegerListExtData(lengths);

        assertEquals(10, data.getObject().size());
        String result = data.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    void testModifyingObjectAfterConstruction() {
        List<Integer> originalLengths = Arrays.asList(100, 200);
        JSONIntegerListExtData data = new JSONIntegerListExtData(originalLengths);

        // The list is not defensively copied, so modifications affect the data
        // This test documents the current behavior
        List<Integer> newLengths = Arrays.asList(300, 400, 500);
        data.setObject(newLengths);

        assertEquals(newLengths, data.getObject());
        assertEquals(3, data.getObject().size());
    }
}
