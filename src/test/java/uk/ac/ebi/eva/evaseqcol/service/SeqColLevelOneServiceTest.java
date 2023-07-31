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

    private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";
    private static final String GCA_ACCESSION = "GCA_000146045.2";
    //private final String REPORT_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_Dpse_3.0_assembly_report.txt";
    //private final String SEQUENCES_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_genome_sequence.fna";

    //private static final String GCF_ACCESSION = "GCF_000001765.3";
    private static InputStreamReader sequencesStreamReader;
    private static InputStream sequencesStream;

    private static InputStreamReader reportStreamReader;
    private static InputStream reportStream;

    @Autowired
    private NCBIAssemblyReportReaderFactory reportReaderFactory;
    private NCBIAssemblyReportReader reportReader;

    @Autowired
    private NCBIAssemblySequenceReaderFactory sequenceReaderFactory;
    private NCBIAssemblySequenceReader sequenceReader;

    @Autowired
    private SeqColExtendedDataService seqColExtendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    private final DigestCalculator digestCalculator = new DigestCalculator();

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
    void setUp() throws FileNotFoundException {
        sequencesStream = new FileInputStream(
                new File(SEQUENCES_FILE_PATH_1));
        sequencesStreamReader = new InputStreamReader(sequencesStream);
        sequenceReader = sequenceReaderFactory.build(sequencesStreamReader, GCA_ACCESSION);

        reportStream = new FileInputStream(
                new File(REPORT_FILE_PATH_1));
        reportStreamReader = new InputStreamReader(reportStream);
        reportReader = reportReaderFactory.build(reportStreamReader);
    }

    @AfterEach
    void tearDown() throws IOException {
        reportStream.close();
        reportStreamReader.close();
        sequencesStream.close();
        sequencesStreamReader.close();
    }

    AssemblyEntity getAssemblyEntity() throws IOException {
        return reportReader.getAssemblyEntity();
    }

    AssemblySequenceEntity getAssemblySequenceEntity() throws IOException {
        return sequenceReader.getAssemblySequencesEntity();
    }

    @Test
    void constructSeqColL1Test() throws IOException {
        // Construct seqCol L1 out of a L2 seqCol object
        AssemblyEntity assemblyEntity = getAssemblyEntity();
        AssemblySequenceEntity assemblySequenceEntity = getAssemblySequenceEntity();
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
        AssemblyEntity assemblyEntity = getAssemblyEntity();
        AssemblySequenceEntity assemblySequenceEntity = getAssemblySequenceEntity();
        List<SeqColExtendedDataEntity> extendedDataEntities = seqColExtendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        ); // Contains the list of names, lengths and sequences exploded

        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(extendedDataEntities, SeqColEntity.NamingConvention.GENBANK);
        Optional<SeqColLevelOneEntity> savedEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertTrue(savedEntity.isPresent());
        System.out.println(savedEntity.get());
    }
}