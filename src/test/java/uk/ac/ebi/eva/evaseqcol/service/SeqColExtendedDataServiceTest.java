package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.io.AssemblyDataGenerator;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColExtendedDataServiceTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

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
        SeqColExtendedDataEntity seqColLengthsObject = SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity);
        SeqColExtendedDataEntity seqColNamesObject = SeqColExtendedDataEntity.constructSeqColNamesObjectByNamingConvention(assemblyEntity, SeqColEntity.NamingConvention.GENBANK);
        SeqColExtendedDataEntity seqColSequencesObject = SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity);
        Optional<SeqColExtendedDataEntity> fetchNamesEntity = extendedDataService.addSeqColExtendedData(seqColLengthsObject);
        Optional<SeqColExtendedDataEntity> fetchLengthsEntity = extendedDataService.addSeqColExtendedData(seqColNamesObject);
        Optional<SeqColExtendedDataEntity> fetchSequencesEntity = extendedDataService.addSeqColExtendedData(seqColSequencesObject);
        assertTrue(fetchNamesEntity.isPresent());
        assertTrue(fetchLengthsEntity.isPresent());
        assertTrue(fetchSequencesEntity.isPresent());
    }
}