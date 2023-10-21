package uk.ac.ebi.eva.evaseqcol.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeqColLevelTwoEntity extends SeqColEntity{

    private List<String> sequences;
    private List<String> names;
    private List<Integer> lengths;
    @JsonProperty("md5_sequences")
    private List<String> md5DigestsOfSequences;
    @JsonProperty("sorted_name_length_pairs")
    private List<String> sortedNameLengthPairs;

    public SeqColLevelTwoEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public SeqColLevelTwoEntity setNamingConvention(NamingConvention convention) {
        this.namingConvention = convention;
        return this;
    }
}
