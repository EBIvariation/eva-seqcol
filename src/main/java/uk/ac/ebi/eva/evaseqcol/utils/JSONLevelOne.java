package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JSONLevelOne implements Serializable {
    private String sequences;
    private String names;
    private String lengths;

    public JSONLevelOne setSequences(String sequences) {
        this.sequences = sequences;
        return this;
    }

    public JSONLevelOne setNames(String names) {
        this.names = names;
        return this;
    }

    public JSONLevelOne setLengths(String lengths) {
        this.lengths = lengths;
        return this;
    }
}
