package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSONStringListExtData extends JSONExtData<List<String>>{

    public JSONStringListExtData(List<String> object) {
        super(object);
    }

    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
                int arraySize = ((List<?>) object).size();
                    // Include quotes. Eg: ["aaa", "bbb", "ccc"].
                    objectStr.append("[");
                    for (int i=0; i<arraySize-1; i++) {
                        objectStr.append("\"");
                        objectStr.append(((List<?>) object).get(i));
                        objectStr.append("\"");
                        objectStr.append(",");
                    }
                    objectStr.append("\"");
                    objectStr.append(((List<?>) object).get(arraySize-1));
                    objectStr.append("\"");
                    objectStr.append("]");

                    return objectStr.toString();
            }

}
