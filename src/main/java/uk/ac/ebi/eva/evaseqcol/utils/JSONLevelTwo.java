package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class JSONLevelTwo implements Serializable {
    private List<String> object; // Level 2 lengths array

    public JSONLevelTwo(List<String> object){
        this.object = object;
    }
}
