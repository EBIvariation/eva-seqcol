package uk.ac.ebi.eva.evaseqcol.entities;

import java.util.List;

/**
 * The complete set of extended (level 2) attributes that make up one seqCol object.
 */
public record SeqColExtendedDataSet(
        SeqColExtendedDataEntity<List<String>> sequences,
        SeqColExtendedDataEntity<List<String>> md5Sequences,
        SeqColExtendedDataEntity<List<String>> names,
        SeqColExtendedDataEntity<List<Integer>> lengths,
        SeqColExtendedDataEntity<List<String>> sortedNameLengthPairs) {

    /**
     * Return all the attributes of this set, in no particular order
     */
    public List<SeqColExtendedDataEntity<?>> all() {
        return List.of(sequences, md5Sequences, names, lengths, sortedNameLengthPairs);
    }
}
