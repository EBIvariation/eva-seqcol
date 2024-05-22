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
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.AssemblyDataGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
@Testcontainers
class SeqColLevelOneServiceTest {

    @Autowired
    private AssemblyDataGenerator assemblyDataGenerator;

    private final String GCA_ACCESSION = "GCA_000146045.2";

    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;

    @Autowired
    private SeqColExtendedDataService seqColExtendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14.0");

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
        Map<String, Object> extendedDataMapGenbank = seqColExtendedDataService.constructExtendedSeqColDataMap(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        );
        List<SeqColExtendedDataEntity<List<String>>> stringListExtDataList =
                (List<SeqColExtendedDataEntity<List<String>>>) extendedDataMapGenbank.get("stringListExtDataList");
        List<SeqColExtendedDataEntity<List<Integer>>> integerListExtDataList =
                (List<SeqColExtendedDataEntity<List<Integer>>>) extendedDataMapGenbank.get("integerListExtDataList");
        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(
                stringListExtDataList, integerListExtDataList, SeqColEntity.NamingConvention.GENBANK, GCA_ACCESSION);
        SeqColLevelTwoEntity levelTwoEntity = levelTwoService.
                constructSeqColL2(levelOneEntity.getDigest(), stringListExtDataList, integerListExtDataList);
        SeqColLevelOneEntity constructedEntity = levelOneService.constructSeqColLevelOne(levelTwoEntity, SeqColEntity.NamingConvention.GENBANK, GCA_ACCESSION);
        assertNotNull(constructedEntity);
        assertNotNull(constructedEntity.getSeqColLevel1Object().getSequences());
    }

    @Test
    void addSequenceCollectionL1() throws IOException {
        Map<String, Object> extendedDataMapGenbank = seqColExtendedDataService.constructExtendedSeqColDataMap(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        ); // Contains the list of names, lengths and sequences exploded
        List<SeqColExtendedDataEntity<List<String>>> stringListExtDataList =
                (List<SeqColExtendedDataEntity<List<String>>>) extendedDataMapGenbank.get("stringListExtDataList");
        List<SeqColExtendedDataEntity<List<Integer>>> integerListExtDataList =
                (List<SeqColExtendedDataEntity<List<Integer>>>) extendedDataMapGenbank.get("integerListExtDataList");
        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(
                stringListExtDataList, integerListExtDataList, SeqColEntity.NamingConvention.GENBANK, GCA_ACCESSION);
        Optional<SeqColLevelOneEntity> savedEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertTrue(savedEntity.isPresent());
        System.out.println(savedEntity.get());
    }
}