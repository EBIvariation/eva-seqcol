package uk.ac.ebi.eva.evaseqcol.datasource;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;


public interface SeqColDataSource {
    Optional<Map<String, Object>> getAllPossibleSeqColExtendedData(String accession) throws IOException;
}
