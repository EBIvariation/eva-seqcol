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

}
