package uk.ac.ebi.eva.evaseqcol.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JSONLevelOne implements Serializable {
    private String sequences;
    @JsonProperty("md5_sequences")
    private String md5DigestsOfSequences;
    private String names;
    private String lengths;
    @JsonProperty("sorted_name_length_pairs")
    private String sortedNameLengthPairs;

    public JSONLevelOne setSequences(String sequences) {
        this.sequences = sequences;
        return this;
    }

    public JSONLevelOne setMd5DigestsOfSequences(String md5DigestsOfSequences) {
        this.md5DigestsOfSequences = md5DigestsOfSequences;
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
