package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ENAAssemblyReportReaderTest {

    private static final String CHROMOSOME_ENA_SEQUENCE_NAME = "1";

    private static final String CHROMOSOME_GENBANK_ACCESSION = "GK000001.2";

    private static final String SCAFFOLD_SEQUENCE_NAME = "ChrU_1";

    private static final String SCAFFOLD_GENBANK_ACCESSION = "GJ057137.1";

    private InputStreamReader streamReader;

    private InputStream stream;

    @Autowired
    private ENAAssemblyReportReaderFactory readerFactory;

    private ENAAssemblyReportReader reader;

    @BeforeEach
    void setup() throws FileNotFoundException {
        stream = new FileInputStream("src/test/resources/GCA_000003055.3_sequence_report.txt");
        streamReader = new InputStreamReader(stream);
        reader = readerFactory.build(streamReader);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblyReportReader() throws IOException {
        assertTrue(reader.ready());
    }

    AssemblyEntity getAssemblyEntity() throws IOException {
        return reader.getAssemblyEntity();
    }

    @Test
    void verifyAssemblyHasChromosomes() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        assertNotNull(chromosomes);
        assertEquals(3316, chromosomes.size());
    }

    @Test
    void verifyChromosomeMetadata() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        ChromosomeEntity chromosome = chromosomes.get(0);
        assertEquals(CHROMOSOME_ENA_SEQUENCE_NAME, chromosome.getEnaSequenceName());
        assertEquals(CHROMOSOME_GENBANK_ACCESSION, chromosome.getInsdcAccession());
        assertNull(chromosome.getUcscName());
    }

    @Test
    void verifyAssemblyHasScaffolds() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> scaffolds = assembly.getChromosomes().stream()
                                                   .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertEquals(3286, scaffolds.size());
    }

    @Test
    void assertParsedScaffoldValid() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> scaffolds = assembly.getChromosomes().stream()
                                                   .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertTrue(scaffolds.size() > 0);
        ChromosomeEntity scaffold = scaffolds.get(0);
        assertNotNull(scaffold);
        assertEquals(SCAFFOLD_SEQUENCE_NAME, scaffold.getEnaSequenceName());
        assertEquals(SCAFFOLD_GENBANK_ACCESSION, scaffold.getInsdcAccession());
    }
}