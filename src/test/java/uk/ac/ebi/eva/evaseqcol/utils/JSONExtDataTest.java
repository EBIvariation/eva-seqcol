package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONExtDataTest {

    private JSONExtData JSONExtDataObj;

    @BeforeEach
    void setUp() {
        JSONExtDataObj = new JSONExtData();
        List<String> arrElements = Arrays.asList("A", "B", "C");
        JSONExtDataObj.setObject(arrElements);

    }

    @Test
    void testToString() {
        assertEquals("[\"A\",\"B\",\"C\"]", JSONExtDataObj.toString());
    }
}