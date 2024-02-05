package uk.ac.ebi.eva.evaseqcol.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${controller.auth.admin.username}")
    private String USERNAME_ADMIN;

    @Value("${controller.auth.admin.password}")
    private String PASSWORD_ADMIN;

    private final String ADMIN_PATH = "/admin/seqcols";

    private final String ASM_ACCESSION = "GCA_000146045.2";

    private final String insertedSeqColDigest = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq"; // The digest of the seqCol that will be inserted by the PUT method

    private static RestTemplate restTemplate;

    @Autowired
    private SeqColService seqColService;

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
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor(USERNAME_ADMIN, PASSWORD_ADMIN)
        );
        baseUrl = baseUrl + ":" + port + contextPath + ADMIN_PATH;
    }

    @AfterEach
    void tearDown() {
        seqColService.removeAllSeqCol(); // TODO Fix: This operation is rolled back for some reason @see 'https://www.baeldung.com/hibernate-initialize-proxy-exception' (might help)
    }

    @Test
    @Order(2)
    @Transactional
    /**
     * Ingest all possible seqCol objects given the assembly accession*/
    void ingestSeqColsTest() {
        String finalRequest = baseUrl + "/{asmAccession}";
        restTemplate.put(finalRequest, null, ASM_ACCESSION);
        Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>)
                seqColService.getSeqColByDigestAndLevel(insertedSeqColDigest, 1);
        Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>)
                seqColService.getSeqColByDigestAndLevel(insertedSeqColDigest, 2);
        assertTrue(levelOneEntity.isPresent());
        assertTrue(levelTwoEntity.isPresent());
        assertEquals(insertedSeqColDigest,levelOneEntity.get().getDigest());
        assertNotNull(levelTwoEntity.get().getLengths());
        assertEquals(ASM_ACCESSION, levelOneEntity.get().getAsmAccession());
    }

    @Test
    @Order(1)
    /**
     * Testing the response for the ingestion endpoint for
     * different kind of ingestion cases:
     * 1. Not existed
     * 2. Already existed
     * 3. Invalid assembly accession
     * 4. Not found assembly accession
     * Note: the order of execution is important */
    void ingestionResponseTest() {
        // 1. Testing Valid Non-Existing Accession
        String finalRequest1 = baseUrl + "/" + ASM_ACCESSION;
        ResponseEntity<String> putResponse1 = restTemplate.exchange(finalRequest1, HttpMethod.PUT, null, String.class);
        assertEquals(HttpStatus.CREATED, putResponse1.getStatusCode());

        // 2. Testing Valid Existing Accession
        final String finalRequest2 = baseUrl + "/" + ASM_ACCESSION; // Same as above
        assertThrows(HttpClientErrorException.Conflict.class,
                     () -> restTemplate.exchange(finalRequest2, HttpMethod.PUT, null, String.class));

        // 3. Testing Invalid Assembly Accession
        final String finalRequest3 = baseUrl + "/" + ASM_ACCESSION.substring(0, ASM_ACCESSION.length()-4); // Less than 15 characters
        assertThrows(HttpClientErrorException.BadRequest.class,
                     () -> restTemplate.exchange(finalRequest3, HttpMethod.PUT, null, String.class));

        // 4. Testing Assembly Not Found
        final String finalRequest4 = baseUrl + "/" + ASM_ACCESSION + "55"; // Accession doesn't correspond to any assembly
        assertThrows(HttpClientErrorException.NotFound.class,
                     () -> restTemplate.exchange(finalRequest4, HttpMethod.PUT, null, String.class));
    }

}
