package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class NCBIAssemblyDataSourceTest {

    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    private static final String GCF_ACCESSION_NO_CHROMOSOMES = "GCF_006125015.1";

    @Autowired
    private NCBIAssemblyDataSource dataSource;

    @Test
    public void getAssemblyByAccessionGCAHavingChromosomes() throws IOException {
        Optional<AssemblyEntity> accession = dataSource.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES);
        assertTrue(accession.isPresent());
        List<ChromosomeEntity> chromosomes = accession.get().getChromosomes();
        assertNotNull(chromosomes);
        assertFalse(chromosomes.isEmpty());
    }

    @Test
    public void getAssemblyByAccessionGCFNoChromosomes() throws IOException {
        Optional<AssemblyEntity> accession = dataSource.getAssemblyByAccession(GCF_ACCESSION_NO_CHROMOSOMES);
        assertTrue(accession.isPresent());
        List<ChromosomeEntity> chromosomes = accession.get().getChromosomes().stream()
                                                      .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.CHROMOSOME))
                                                      .collect(Collectors.toList());
        assertEquals(0, chromosomes.size());
    }
}