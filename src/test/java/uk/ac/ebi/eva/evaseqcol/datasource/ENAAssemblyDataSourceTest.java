package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
class ENAAssemblyDataSourceTest {

    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    @Autowired
    private ENAAssemblyDataSource enaDataSource;

    @Autowired
    private NCBIAssemblyDataSource ncbiDataSource;

    @Test
    public void getAssemblyByAccessionGCAHavingChromosomes() throws IOException {
        Optional<AssemblyEntity> accession = enaDataSource.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES);
        assertTrue(accession.isPresent());
        List<ChromosomeEntity> chromosomes = accession.get().getChromosomes();
        assertNotNull(chromosomes);
        assertFalse(chromosomes.isEmpty());
    }

    @Test
    public void getENASequenceNamesForAssembly() throws IOException {
        Optional<AssemblyEntity> assembly = ncbiDataSource.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES);
        enaDataSource.addENASequenceNamesToAssembly(assembly);
        assertTrue(assembly.isPresent());
        assertTrue(enaDataSource.hasAllEnaSequenceNames(assembly.get()));
    }
}