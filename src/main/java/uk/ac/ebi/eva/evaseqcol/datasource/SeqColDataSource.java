package uk.ac.ebi.eva.evaseqcol.datasource;

import java.io.IOException;
import java.util.Optional;

public interface SeqColDataSource {

    /**
     * Fetch the seqCol data of the given assembly accession, with all the naming conventions available
     */
    Optional<SeqColSourceData> getAllPossibleSeqColExtendedData(String accession) throws IOException;
}
