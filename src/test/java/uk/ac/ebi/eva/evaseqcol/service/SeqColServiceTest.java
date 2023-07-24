package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
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
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("seqcol")
//@Testcontainers
class SeqColServiceTest {

    private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";
    private static final String GCA_ACCESSION = "GCA_000146045.2";

    private final String TEST_DIGEST = "7eldYm-sjycc1MDEVSI5jmuNac4BO-eN";
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
    private SeqColLevelOneService levelOneService;

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @Autowired
    private SeqColService seqColService;


    /*@Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }*/

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
    //@Order(1)
<<<<<<< Updated upstream
=======
    @Disabled
>>>>>>> Stashed changes
    void addSequenceCollectionTest() throws IOException {
        AssemblyEntity assemblyEntity = getAssemblyEntity();
        AssemblySequenceEntity assemblySequenceEntity = getAssemblySequenceEntity();
        List<SeqColExtendedDataEntity> extendedDataEntities = extendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.UCSC
        );
        SeqColLevelOneEntity levelOneEntity = levelOneService.constructSeqColLevelOne(
                extendedDataEntities, SeqColEntity.NamingConvention.UCSC);
        Optional<String> resultDigest = seqColService.addFullSequenceCollection(levelOneEntity, extendedDataEntities);
        assertTrue(resultDigest.isPresent());
    }

    @Test
    //@Order(2)
    void getSeqColByDigestAndLevelTest() {
       /* Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestAndLevel(RESULT_DIGEST, 1);
        assertTrue(levelOneEntity.isPresent());*/
        Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestAndLevel(TEST_DIGEST, 2);
        assertTrue(levelTwoEntity.isPresent());
    }
}