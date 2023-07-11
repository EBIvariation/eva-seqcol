package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class JSONExtData implements Serializable {
    private List<String> object;

    public JSONExtData(List<String> object){
        this.object = object;
    }

    private boolean onlyDigits(String str) {
        String regex = "[0-9]+";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }

    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
        objectStr.append("[");
        if (onlyDigits(object.get(0).toString())) { // Lengths array, No quotes "...". Eg: [1111, 222, 333]
            for (int i=0; i<object.size()-1; i++) {
               objectStr.append(object.get(i));
               objectStr.append(",");
            }
            objectStr.append(object.get(object.size()-1));
            objectStr.append("]");
        } else { // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
            for (int i=0; i<object.size()-1; i++) {
                objectStr.append("\"");
                objectStr.append(object.get(i));
                objectStr.append("\"");
                objectStr.append(",");
            }
            objectStr.append("\"");
            objectStr.append(object.get(object.size()-1));
            objectStr.append("\"");
            objectStr.append("]");
        }
        return objectStr.toString();
    }
}
