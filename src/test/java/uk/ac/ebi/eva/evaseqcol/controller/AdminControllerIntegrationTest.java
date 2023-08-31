package uk.ac.ebi.eva.evaseqcol.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
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

    @Test
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
    }

}
