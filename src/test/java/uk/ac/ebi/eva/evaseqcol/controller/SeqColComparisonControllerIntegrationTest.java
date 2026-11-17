package uk.ac.ebi.eva.evaseqcol.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColGenerator;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;
import uk.ac.ebi.eva.evaseqcol.utils.AbstractIntegrationTest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        seqColWriter.clearData();
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

    @Test
    void compareWithItsOwnLevelTwoGivesTheSameDigest() {
        // POSTing the level 2 representation of a stored seqCol must give back the stored level 0 digest
        String levelTwoUrl = baseUrl.replace(COMPARISON_PATH, "") + "/collection/" + SEQCOL_A_DIGEST + "?level=2";
        Map<String, Object> levelTwo = restTemplate.getForObject(levelTwoUrl, Map.class);
        Map<String, Object> comparisonResult = restTemplate.postForObject(baseUrl + "/" + SEQCOL_A_DIGEST, levelTwo,
                                                                          Map.class);
        Map<String, String> digests = (Map<String, String>) comparisonResult.get("digests");
        assertEquals(SEQCOL_A_DIGEST, digests.get("a"));
        assertEquals(SEQCOL_A_DIGEST, digests.get("b"));
    }

    @Test
    void compareWithUnknownSeqColReturnsNotFound() {
        SeqColLevelTwoEntity seqColLevelTwoPostBody = seqColGenerator.generateLevelTwoEntity();
        HttpClientErrorException postException = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.postForObject(baseUrl + "/unknown_digest", seqColLevelTwoPostBody, String.class));
        assertEquals(HttpStatus.NOT_FOUND, postException.getStatusCode());
        assertEquals("No seqCol corresponding to digest unknown_digest could be found in DB",
                     postException.getResponseBodyAsString());

        HttpClientErrorException getException = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.getForObject(baseUrl + "/" + SEQCOL_A_DIGEST + "/unknown_digest", String.class));
        assertEquals(HttpStatus.NOT_FOUND, getException.getStatusCode());
    }
}
