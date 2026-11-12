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
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.AssemblyDataGenerator;
import uk.ac.ebi.eva.evaseqcol.utils.AbstractIntegrationTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import uk.ac.ebi.eva.evaseqcol.entities.NamingConvention;

@SpringBootTest
@ActiveProfiles("integration")
class SeqColLevelOneServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private final String GCA_ACCESSION = "GCA_000146045.2";

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataFactory extendedDataFactory;

    @Autowired
    private SeqColLevelOneFactory levelOneFactory;

    @Autowired
    private SeqColLevelOneService levelOneService;

    @Autowired
    private SeqColLevelTwoService levelTwoService;

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
    void constructSeqColL1Test() throws IOException {
        // Construct seqCol L1 out of a L2 seqCol object
        SeqColExtendedDataSet extendedData = extendedDataFactory.extendedDataSet(
                assemblyEntity, assemblySequenceEntity, NamingConvention.GENBANK);
        SeqColLevelOneEntity levelOneEntity = levelOneFactory.create(extendedData, NamingConvention.GENBANK, GCA_ACCESSION);
        SeqColLevelTwoEntity levelTwoEntity = levelTwoService.constructSeqColL2(levelOneEntity.getDigest(), extendedData);
        SeqColLevelOneEntity constructedEntity = levelOneFactory.create(levelTwoEntity, NamingConvention.GENBANK, GCA_ACCESSION);
        assertNotNull(constructedEntity);
        assertNotNull(constructedEntity.getSeqColLevel1Object().getSequences());
        // The round trip L1 -> L2 -> L1 gives the same object
        assertEquals(levelOneEntity.getDigest(), constructedEntity.getDigest());
        assertEquals(levelOneEntity.getSeqColLevel1Object(), constructedEntity.getSeqColLevel1Object());
    }

    @Test
    void addSequenceCollectionL1() throws IOException {
        SeqColExtendedDataSet extendedData = extendedDataFactory.extendedDataSet(
                assemblyEntity, assemblySequenceEntity, NamingConvention.GENBANK);
        SeqColLevelOneEntity levelOneEntity = levelOneFactory.create(extendedData, NamingConvention.GENBANK, GCA_ACCESSION);
        SeqColLevelOneEntity savedEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertEquals(levelOneEntity.getDigest(), savedEntity.getDigest());
        assertTrue(levelOneService.existsByDigest(levelOneEntity.getDigest()));
    }
}
