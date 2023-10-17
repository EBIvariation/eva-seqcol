package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.util.List;

@Data
public class JSONIntegerExtData extends JSONExtData<List<Integer>> {


    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
        objectStr.append("[");
        // Lengths array, No quotes "...". Eg: [1111, 222, 333]
        for (int i=0; i<object.size()-1; i++) {
            objectStr.append(object.get(i));
            objectStr.append(",");
        }
        objectStr.append(object.get(object.size()-1));
        objectStr.append("]");
        return objectStr.toString();
    }

}
