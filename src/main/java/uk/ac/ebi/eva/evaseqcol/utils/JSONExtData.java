package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class JSONExtData<T> implements Serializable {
    protected T object;

    public JSONExtData(T object){
        this.object = object;
    }

    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
        try {
            if (List.class.isAssignableFrom(object.getClass())) { // Array attributes
                int arraySize = ((List<?>) object).size();
                if (isIntegerArray(object)) { // Lengths array, No quotes "...". Eg: [1111, 222, 333]
                    return object.toString();
                } else if (isStringArray(object)){ // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
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
                }
            } // else { Process single value attributes here. Spec still in progress}

            return objectStr.toString();
        } catch (NullPointerException e) {
            System.out.println("Cannot invoke object.toString() because object is null !!");
            return null;
        }

    }

    private boolean isStringArray(T object) {
        return  ((List<?>) object).stream().allMatch(t -> String.class.isAssignableFrom(t.getClass()));
    }

    private boolean isIntegerArray(T object) {
        return  ((List<?>) object).stream().allMatch(t -> Integer.class.isAssignableFrom(t.getClass()));
    }
}
