package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONExtDataTest {

    private JSONExtData<List<String>> JSONStringListExtDataObj;

    @BeforeEach
    void setUp() {
        JSONStringListExtDataObj = new JSONStringListExtData();
        List<String> arrElements = Arrays.asList("A", "B", "C");
        JSONStringListExtDataObj.setObject(arrElements);

    }

    @Test
    void testToString() {
        assertEquals("[\"A\",\"B\",\"C\"]", JSONStringListExtDataObj.toString());
    }
}