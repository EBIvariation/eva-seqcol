package uk.ac.ebi.eva.evaseqcol.datasource;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;

import java.io.IOException;
import java.util.Optional;

public interface AssemblyDataSource {
    Optional<AssemblyEntity> getAssemblyByAccession(String accession) throws IOException;
}
