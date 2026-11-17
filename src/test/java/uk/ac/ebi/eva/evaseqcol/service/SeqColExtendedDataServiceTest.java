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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import uk.ac.ebi.eva.evaseqcol.entities.NamingConvention;

@SpringBootTest
@ActiveProfiles("integration")
class SeqColExtendedDataServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @Autowired
    private SeqColExtendedDataFactory extendedDataFactory;

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

    /**
     * Adding multiple seqCol extended data objects*/
    @Test
    void addSeqColExtendedData() throws IOException {
        assertNotNull(assemblyEntity);
        assertEquals(assemblySequenceEntity.getSequences().size(), assemblyEntity.getChromosomes().size());
        SeqColExtendedDataEntity<List<Integer>> seqColLengthsObject = extendedDataFactory.lengths(assemblyEntity);
        SeqColExtendedDataEntity<List<String>> seqColNamesObject = extendedDataFactory.namesByNamingConvention(assemblyEntity, NamingConvention.GENBANK);
        SeqColExtendedDataEntity<List<String>> seqColSequencesObject = extendedDataFactory.sequences(assemblySequenceEntity);
        extendedDataService.addSeqColExtendedData(seqColLengthsObject);
        extendedDataService.addSeqColExtendedData(seqColNamesObject);
        extendedDataService.addSeqColExtendedData(seqColSequencesObject);
        assertTrue(extendedDataService.findByDigest(seqColNamesObject.getDigest()).isPresent());
        assertTrue(extendedDataService.findByDigest(seqColLengthsObject.getDigest()).isPresent());
        assertTrue(extendedDataService.findByDigest(seqColSequencesObject.getDigest()).isPresent());
    }
}