package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class SideTests {

    @Test
    void test() {
        JSONExtData<List<Integer>> intArray = new JSONExtData<>();
        JSONExtData<List<String>> strArray = new JSONExtData<>();
        intArray.setObject(Arrays.asList(
                1111,
                2222,
                3333
        ));
        strArray.setObject(Arrays.asList(
                "AAAA",
                "BBBB",
                "CCCC"
        ));
        System.out.println(intArray); // Output: [1111, 2222, 3333]
        System.out.println(strArray); // Output: ["AAAA","BBBB","CCCC"]
     }
}
