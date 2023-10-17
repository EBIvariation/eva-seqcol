package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.util.List;

@Data
public class JSONStringExtData extends JSONExtData<List<String>> {


    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
        // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
        for (int i = 0; i < object.size() - 1; i++) {
            objectStr.append("\"");
            objectStr.append(object.get(i));
            objectStr.append("\"");
            objectStr.append(",");
        }
        objectStr.append("\"");
        objectStr.append(object.get(object.size() - 1));
        objectStr.append("\"");
        objectStr.append("]");

        return objectStr.toString();
    }

}
