package uk.ac.ebi.eva.evaseqcol.datasource;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequencesEntity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public interface AssemblySequencesDataSource {
    Optional<AssemblySequencesEntity> getAssemblySequencesByAccession(String accession)
            throws IOException, NoSuchAlgorithmException;
}
