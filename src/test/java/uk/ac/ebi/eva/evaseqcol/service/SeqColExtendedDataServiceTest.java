package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.io.AssemblyDataGenerator;
import uk.ac.ebi.eva.evaseqcol.utils.AbstractIntegrationTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColExtendedDataServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @BeforeEach
    void setUp() throws IOException {
        assemblyEntity = assemblyDataGenerator.generateAssemblyEntity();
        assemblySequenceEntity = assemblyDataGenerator.generateAssemblySequenceEntity();
    }

    @AfterEach
    void tearDown() {
        assemblyEntity = null; // May speed up the object deletion by the garbage collector
        assemblySequenceEntity = null; // May speed up the object deletion by the garbage collector
    }

    @Test
    /**
     * Adding multiple seqCol extended data objects*/
    void addSeqColExtendedData() throws IOException {
        assertNotNull(assemblyEntity);
        assertEquals(assemblySequenceEntity.getSequences().size(), assemblyEntity.getChromosomes().size());
        SeqColExtendedDataEntity<List<Integer>> seqColLengthsObject = SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity);
        SeqColExtendedDataEntity<List<String>> seqColNamesObject = SeqColExtendedDataEntity.constructSeqColNamesObjectByNamingConvention(assemblyEntity, SeqColEntity.NamingConvention.GENBANK);
        SeqColExtendedDataEntity<List<String>> seqColSequencesObject = SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity);
        Optional<SeqColExtendedDataEntity<List<Integer>>> fetchLengthsEntity = extendedDataService.addSeqColExtendedData(seqColLengthsObject);
        Optional<SeqColExtendedDataEntity<List<String>>> fetchNamesEntity = extendedDataService.addSeqColExtendedData(seqColNamesObject);
        Optional<SeqColExtendedDataEntity<List<String>>> fetchSequencesEntity = extendedDataService.addSeqColExtendedData(seqColSequencesObject);
        assertTrue(fetchNamesEntity.isPresent());
        assertTrue(fetchLengthsEntity.isPresent());
        assertTrue(fetchSequencesEntity.isPresent());
    }
}