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

import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SeqColControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private final String RETRIEVAL_PATH = "/collection";

    private final String SEQCOL_DIGEST = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq"; // seqCol test digest

    private final String SERVICE_INFO_PATH = "/service-info";

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static RestTemplate restTemplate;


    @Autowired
    private SeqColWriter seqColWriter;

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
        seqColWriter.write(); // Save some seqCol objects into the database
        baseUrl = baseUrl + ":" + port + contextPath ;
     }

    @AfterEach
    void tearDown() {
        seqColWriter.clearData();
    }

    @Test
    void getSeqColByDigestTest() {
        String level_1_path = "?level=1"; // can be left to default
        String level_2_path = "?level=2";
        String finalRequest = baseUrl + RETRIEVAL_PATH + "/" + SEQCOL_DIGEST;
        Map<String, Object> levelOneEntity = restTemplate.getForObject(finalRequest + level_1_path, Map.class);
        Map<String, List<String>> levelTwoEntity = restTemplate.getForObject(finalRequest + level_2_path, Map.class);
        assertNotNull(levelOneEntity);
        assertNotNull(levelTwoEntity);
        assertNotNull(levelTwoEntity.get("names"));
        assertNotNull(levelOneEntity.get("lengths"));
        assertNotNull(levelTwoEntity.get("sequences"));
    }

    @Test
    void getServiceInfoTest() {
        String finalRequest = baseUrl + SERVICE_INFO_PATH;
        Map<String, Object> serviceInfoMap = restTemplate.getForObject(finalRequest, Map.class);
        assertNotNull(serviceInfoMap);
        assertNotNull(serviceInfoMap.get("id"));
    }
}

