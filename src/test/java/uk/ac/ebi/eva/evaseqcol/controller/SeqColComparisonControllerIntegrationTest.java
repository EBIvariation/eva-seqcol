package uk.ac.ebi.eva.evaseqcol.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColGenerator;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SeqColComparisonControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final String COMPARISON_PATH = "/comparison";

    private final String SEQCOL_A_DIGEST = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq"; // seqCol test digest

    private final String SEQCOL_B_DIGEST = "rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE";

    @Autowired
    private SeqColWriter seqColWriter;

    @Autowired
    private SeqColGenerator seqColGenerator;

    private static RestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14.0");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeAll
    static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    void setUp() throws IOException {
        seqColWriter.create(); // Save some seqCol objects into the database
        baseUrl = baseUrl + ":" + port + contextPath + COMPARISON_PATH;
    }

    @AfterEach
    void tearDown() {
        seqColWriter.clearData();
    }

    @Test
    void compareLocalSeqColsTest() {
        String finalRequest = baseUrl + "/" + SEQCOL_A_DIGEST + "/" + SEQCOL_B_DIGEST;
        Map<String, Object> comparisonResult = restTemplate.getForObject(finalRequest, Map.class);
        assertNotNull(comparisonResult);
        assertNotNull(comparisonResult.get("digests"));
        assertNotNull(comparisonResult.get("arrays"));
        assertNotNull(comparisonResult.get("elements"));
    }

    @Test
    /**
     * Compare a local seqCol object (identified by digest) with a user provided one,
     * provided in the body of the POST request*/
    void compareALocalSeqColWithProvidedOneTest() {
        String finlRequest = baseUrl + "/" + SEQCOL_A_DIGEST;
        SeqColLevelTwoEntity seqColLevelTwoPostBody = seqColGenerator.generateLevelTwoEntity();
        Map<String, Object> comparisonResult = restTemplate.postForObject(finlRequest, seqColLevelTwoPostBody, Map.class);
        assertNotNull(comparisonResult);
        assertNotNull(comparisonResult.get("digests"));
        assertNotNull(comparisonResult.get("arrays"));
        assertNotNull(comparisonResult.get("elements"));
    }
}
