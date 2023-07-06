package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
class NCBIAssemblySequenceDataSourceTest {

    private final String GCA_ACCESSION = "GCF_000001765.3"; // Extreme size assembly

    //private final String GCA_ACCESSION = "GCA_000146045.2"; // Contains a sequence that we need 'BK006935.2'

    @Autowired
    private NCBIAssemblySequenceDataSource dataSource;

    @Test
    void getAssemblySequencesByAccession() throws IOException, NoSuchAlgorithmException {
        Optional<AssemblySequenceEntity> sequencesEntity = dataSource.getAssemblySequencesByAccession(GCA_ACCESSION);
        assertTrue(sequencesEntity.isPresent());
        List<SeqColSequenceEntity> sequenceList = sequencesEntity.get().getSequences();
        assertNotNull(sequenceList);
        assertFalse(sequenceList.isEmpty());
    }
}