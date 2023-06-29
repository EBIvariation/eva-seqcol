package uk.ac.ebi.eva.evaseqcol.datasource;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public interface AssemblySequencesDataSource {
    Optional<AssemblySequenceEntity> getAssemblySequencesByAccession(String accession)
            throws IOException, NoSuchAlgorithmException;
}
