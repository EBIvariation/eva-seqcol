package uk.ac.ebi.eva.evaseqcol.entities.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;
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

    public JSONLevelOne setSortedNameLengthPairs(String sortedNameLengthPairs) {
        this.sortedNameLengthPairs = sortedNameLengthPairs;
        return this;
    }

    /**
     * Return the digest of the given attribute
     */
    public String getDigest(AttributeType attributeType) {
        return switch (attributeType) {
            case names -> names;
            case sequences -> sequences;
            case md5DigestsOfSequences -> md5DigestsOfSequences;
            case lengths -> lengths;
            case sortedNameLengthPairs -> sortedNameLengthPairs;
        };
    }

    /**
     * Set the digest of the given attribute
     */
    public JSONLevelOne setDigest(AttributeType attributeType, String digest) {
        switch (attributeType) {
            case names -> names = digest;
            case sequences -> sequences = digest;
            case md5DigestsOfSequences -> md5DigestsOfSequences = digest;
            case lengths -> lengths = digest;
            case sortedNameLengthPairs -> sortedNameLengthPairs = digest;
        }
        return this;
    }
}
