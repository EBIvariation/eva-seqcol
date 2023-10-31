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
}
