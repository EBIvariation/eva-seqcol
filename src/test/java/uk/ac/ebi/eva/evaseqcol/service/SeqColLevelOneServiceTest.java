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

import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.AssemblyDataGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColLevelOneServiceTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataService seqColExtendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    @Autowired
    private SeqColLevelTwoService levelTwoService;

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
    void constructSeqColL1Test() throws IOException {
        // Construct seqCol L1 out of a L2 seqCol object
        List<SeqColExtendedDataEntity> extendedDataEntities = seqColExtendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        );
        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(extendedDataEntities, SeqColEntity.NamingConvention.GENBANK);
        SeqColLevelTwoEntity levelTwoEntity = levelTwoService.constructSeqColL2(levelOneEntity.getDigest(), extendedDataEntities);
        SeqColLevelOneEntity constructedEntity = levelOneService.constructSeqColLevelOne(levelTwoEntity, SeqColEntity.NamingConvention.GENBANK);
        assertNotNull(constructedEntity);
        assertNotNull(constructedEntity.getSeqColLevel1Object().getSequences());
    }

    @Test
    void addSequenceCollectionL1() throws IOException {
        List<SeqColExtendedDataEntity> extendedDataEntities = seqColExtendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        ); // Contains the list of names, lengths and sequences exploded

        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(extendedDataEntities, SeqColEntity.NamingConvention.GENBANK);
        Optional<SeqColLevelOneEntity> savedEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertTrue(savedEntity.isPresent());
        System.out.println(savedEntity.get());
    }
}