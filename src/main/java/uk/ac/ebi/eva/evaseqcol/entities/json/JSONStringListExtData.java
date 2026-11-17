package uk.ac.ebi.eva.evaseqcol.entities.json;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSONStringListExtData extends JSONExtData<List<String>> {

    public JSONStringListExtData(List<String> object) {
        super(object);
    }

    /**
     * JSON array representation used to compute the digest. Eg: ["aaa","bbb","ccc"]
     */
    @Override
    public String toString() {
        StringBuilder objectStr = new StringBuilder("[");
        for (int i = 0; i < object.size(); i++) {
            if (i > 0) {
                objectStr.append(",");
            }
            objectStr.append("\"").append(object.get(i)).append("\"");
        }
        return objectStr.append("]").toString();
    }

}
