package uk.ac.ebi.eva.evaseqcol.entities.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONStringListExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONExtData;

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