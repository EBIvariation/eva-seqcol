package uk.ac.ebi.eva.evaseqcol.refget;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SHA512CalculatorTest {

    private final String FIRST_SEQ_REFSEQ = "NW_001589959.2";
    private final Integer FIRST_SEQ_SIZE = 2692213;
    private final String FIRST_SEQ_MD5 = "7255454b5d7eb75cb046f16fbe9a7c7d"; // The md5 hash of the first sequence in the fasta file
    private BufferedReader reader;
    private InputStreamReader streamReader;
    private InputStream stream;
    private ChecksumCalculator sha512Calculator;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        stream = new FileInputStream(
                new File("src/test/resources/GCF_000001765.3_genome_sequence.fna"));
        streamReader = new InputStreamReader(stream);
        reader = new BufferedReader(streamReader);
        sha512Calculator = new SHA512Calculator();

    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    /**
     * Parse the file and return the plain sequence that has the given seqRefseq*/
    Optional<String> getSequenceByRefseqFromFastaFile(String seqRefseq) throws IOException {
        if (reader == null){
            throw new NullPointerException("Not valid InputStreamReader.");
        }
        String line = reader.readLine();
        boolean found = false;
        while (line != null && !found){
            if (line.startsWith(">")){
                String refSeq = line.substring(1, line.indexOf(' '));
                if (refSeq.equals(seqRefseq)){
                    found = true;
                } else {
                    continue;
                }
                line = reader.readLine();
                StringBuilder sequenceValue = new StringBuilder();
                while (line != null && !line.startsWith(">")){
                    // Looking for the sequence lines for this refseq
                    sequenceValue.append(line);
                    line = reader.readLine();
                }
                return Optional.of(sequenceValue.toString());
            }
        }
        return Optional.empty();
    }

    @Test
    @Disabled
    void calculateChecksum() throws IOException {
        Optional<String> testSequence = getSequenceByRefseqFromFastaFile(FIRST_SEQ_REFSEQ);
        assertTrue(testSequence.isPresent());
        assertEquals(sha512Calculator.calculateChecksum(testSequence.get()), FIRST_SEQ_MD5);
    }
}