package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSONIntegerExtData extends JSONExtData{
    private List<Integer> object;

    public JSONIntegerExtData(List<Integer> object){
        this.object = object;
    }
}
