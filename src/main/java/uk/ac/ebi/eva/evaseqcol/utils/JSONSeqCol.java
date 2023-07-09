package uk.ac.ebi.eva.evaseqcol.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JSONSeqCol implements Serializable {
    private String sequences;
    private String names;
    private String lengths;

    public JSONSeqCol setSequences(String sequences) {
        this.sequences = sequences;
        return this;
    }

    public JSONSeqCol setNames(String names) {
        this.names = names;
        return this;
    }

    public JSONSeqCol setLengths(String lengths) {
        this.lengths = lengths;
        return this;
    }
}
