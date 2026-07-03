package uk.ac.ebi.eva.evaseqcol.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColGenerator;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;
import uk.ac.ebi.eva.evaseqcol.utils.AbstractIntegrationTest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class SeqColComparisonControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final String COMPARISON_PATH = "/comparison";

    private final String SEQCOL_A_DIGEST = "AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX"; // seqCol test digest

    private final String SEQCOL_B_DIGEST = "AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX";

    @Autowired
    private SeqColWriter seqColWriter;

    @Autowired
    private SeqColGenerator seqColGenerator;

    private static RestTemplate restTemplate;

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
        assertNotNull(comparisonResult.get("attributes"));
        assertNotNull(comparisonResult.get("array_elements"));
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
        assertNotNull(comparisonResult.get("attributes"));
        assertNotNull(comparisonResult.get("array_elements"));
    }
}
