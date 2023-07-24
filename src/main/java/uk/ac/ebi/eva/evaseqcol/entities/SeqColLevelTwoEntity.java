package uk.ac.ebi.eva.evaseqcol.entities;

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
    private List<String> lengths;

    public SeqColLevelTwoEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }
}
