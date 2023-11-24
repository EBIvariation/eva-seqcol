package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSONIntegerListExtData extends JSONExtData<List<Integer>>{

    public JSONIntegerListExtData(List<Integer> object) {
        super(object);
    }

    /**
     * The same as the Overridden toString method
     * // TODO: we can get rid of this method for List<Integer> types*/
    public static String toString(List<Integer> object) {
        return object.toString();
    }

    @Override
    public String toString() {
        return this.object.toString();
    }

}
