package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSONIntegerExtData extends JSONExtData<Integer>{

    public JSONIntegerExtData(List<Integer> object) {
        super(object);
    }
}
