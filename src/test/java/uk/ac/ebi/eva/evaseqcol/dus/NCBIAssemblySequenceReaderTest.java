package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class NCBIAssemblySequenceReaderTest {

    private final String ASSEMBLY_ACCESSION = "GCF_000001765.3";

    private final Integer NUMBER_OF_SEQUENCES = 9; // Some sequences have been removed for size reasons

    private final String FRIST_REFSEQ = "NW_001589959.2"; // The refseq of the first sequence in the fasta file

    private final String FIRST_SEQ_MD5 = "87faa0a4adb9b68d291900d666800c40"; // The md5 hash of the first sequence in the fasta file

    private AssemblySequenceEntity sequencesEntity;

    private InputStreamReader streamReader;

    private InputStream stream;

    @Autowired
    private NCBIAssemblySequenceReaderFactory readerFactory;

    private NCBIAssemblySequenceReader reader;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        stream = new FileInputStream(
                new File("src/test/resources/GCF_000001765.3_genome_sequence.fna"));
        streamReader = new InputStreamReader(stream);
        reader = readerFactory.build(streamReader, ASSEMBLY_ACCESSION);
        SeqColSequenceEntity sequence = new SeqColSequenceEntity()
                .setRefseq("LDPM01000001.1"); // The first sequence of the test fasta file
        sequencesEntity = new AssemblySequenceEntity()
                .setInsdcAccession(ASSEMBLY_ACCESSION)
                .setSequences(Arrays.asList(sequence));
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblySequencesReader() throws IOException {
        assertTrue(reader.ready());
    }

    AssemblySequenceEntity getAssemblySequencesEntity() throws IOException, NoSuchAlgorithmException {
        return reader.getAssemblySequencesEntity();
    }

    @Test
    void verifyAssemblySequencesMetadata() throws IOException, NoSuchAlgorithmException {
        AssemblySequenceEntity sequencesEntity1 = getAssemblySequencesEntity();
        assertEquals(ASSEMBLY_ACCESSION, sequencesEntity1.getInsdcAccession());
        assertEquals(FRIST_REFSEQ, sequencesEntity1.getSequences().get(0).getRefseq());
        assertEquals(FIRST_SEQ_MD5, sequencesEntity1.getSequences().get(0).getSequenceMD5());
        assertEquals(NUMBER_OF_SEQUENCES, sequencesEntity1.getSequences().size());
    }


}