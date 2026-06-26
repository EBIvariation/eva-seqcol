package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class JSONExtData<T> implements Serializable {
    protected T object;

    public JSONExtData(T object) {
        this.object = object;
    }
}
