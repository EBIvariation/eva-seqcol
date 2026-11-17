package uk.ac.ebi.eva.evaseqcol.datasource;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;

import java.util.List;

/**
 * The seqCol data extracted from a source (assembly report + FASTA, or FASTA only).
 * The sequences, md5 sequences and lengths are shared by all the seqCol objects of the source; one seqCol object
 * is created per entry of {@code namesPerNamingConvention}.
 */
public record SeqColSourceData(
        SeqColExtendedDataEntity<List<String>> sequences,
        SeqColExtendedDataEntity<List<String>> md5Sequences,
        SeqColExtendedDataEntity<List<Integer>> lengths,
        List<SeqColExtendedDataEntity<List<String>>> namesPerNamingConvention) {
}
