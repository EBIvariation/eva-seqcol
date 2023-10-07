package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class JSONStringExtData extends JSONExtData<String>{

    public JSONStringExtData(List<String> object) {
        super(object);
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

    /**
     * Check whether the given list contains only digits (in a form of strings)*/
    private boolean onlyDigitsStringList(List<String> list) {
        return list.isEmpty() || list.stream()
                                     .allMatch(this::onlyDigits);
    }

    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder();
        objectStr.append("[");
        // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
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

        return objectStr.toString();
    }
}
