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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SeqColControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private final String RETRIEVAL_PATH = "/collection";

    private final String SEQCOL_DIGEST = "AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX"; // seqCol test digest

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
        seqColWriter.create(); // Save some seqCol objects into the database
        baseUrl = baseUrl + ":" + port + contextPath;
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
        assertNotNull(levelTwoEntity.get("lengths"));
        assertNotNull(levelTwoEntity.get("sequences"));
    }

    @Test
    void getServiceInfoTest() {
        String finalRequest = baseUrl + SERVICE_INFO_PATH;
        Map<String, Object> serviceInfoMap = restTemplate.getForObject(finalRequest, Map.class);
        assertNotNull(serviceInfoMap);
        assertNotNull(serviceInfoMap.get("id"));
    }

    @Test
    void getSeqColListTest() {
        String finalRequest = baseUrl + "/list/collection";
        Map<String, Object> listResult = restTemplate.getForObject(finalRequest, Map.class);
        List<String> level0DigestList = (List<String>) listResult.get("results");
        Map<String, Integer> pagination = (Map<String, Integer>) listResult.get("pagination");
        assertEquals(Arrays.asList("AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX", "ySaGQd8xaXhhfyR5PsTBp4ggbXXVub7w"), level0DigestList);
        assertEquals(0, pagination.get("page"));
        assertEquals(10, pagination.get("pageSize"));
        assertEquals(2, pagination.get("total"));

        finalRequest = baseUrl + "/list/collection?page=1&page_size=1";
        listResult = restTemplate.getForObject(finalRequest, Map.class);
        level0DigestList = (List<String>) listResult.get("results");
        pagination = (Map<String, Integer>) listResult.get("pagination");
        assertEquals(Arrays.asList("ySaGQd8xaXhhfyR5PsTBp4ggbXXVub7w"), level0DigestList);
        assertEquals(1, pagination.get("page"));
        assertEquals(1, pagination.get("pageSize"));
        assertEquals(2, pagination.get("total"));

        finalRequest = baseUrl + "/list/collection?names=rIeQU2I79mbAFQwiV_kf6E8OUIEWq5h9";
        listResult = restTemplate.getForObject(finalRequest, Map.class);
        level0DigestList = (List<String>) listResult.get("results");
        pagination = (Map<String, Integer>) listResult.get("pagination");
        assertEquals(Arrays.asList("AOhJezyy4yRW-GQqnAnD0HQhjcpOb4UX"), level0DigestList);
        assertEquals(0, pagination.get("page"));
        assertEquals(10, pagination.get("pageSize"));
        assertEquals(1, pagination.get("total"));
    }

    @Test
    void getSeqColAttributeTest() {
        String attributeValue = "names";
        String digest = "rIeQU2I79mbAFQwiV_kf6E8OUIEWq5h9";
        String finalRequest = baseUrl + "/attribute/collection/" + attributeValue + "/" + digest;
        List<String> attributeResult = restTemplate.getForObject(finalRequest, List.class);
        assertEquals(Arrays.asList("chrI", "chrII", "chrIII", "chrIV", "chrV", "chrVI", "chrVII", "chrVIII", "chrIX",
                "chrX", "chrXI", "chrXII", "chrXIII", "chrXIV", "chrXV", "chrXVI"), attributeResult);

        attributeValue = "sequences";
        digest = "dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6";
        finalRequest = baseUrl + "/attribute/collection/" + attributeValue + "/" + digest;
        attributeResult = restTemplate.getForObject(finalRequest, List.class);
        assertEquals(Arrays.asList("SQ.lZyxiD_ByprhOUzrR1o1bq0ezO_1gkrn", "SQ.vw8jTiV5SAPDH4TEIZhNGylzNsQM4NC9",
                        "SQ.A_i2Id0FjBI-tQyU4ZaCEdxRzQheDevn", "SQ.QXSUMoZW_SSsCCN9_wc-xmubKQSOn3Qb",
                        "SQ.UN_b-wij0EtsgFqQ2xNsbXs_GYQQIbeQ", "SQ.z-qJgWoacRBV77zcMgZN9E_utrdzmQsH", "SQ.9wkqGXgK6bvM0gcjBiTDk9tAaqOZojlR",
                        "SQ.K8ln7Ygob_lcVjNh-C8kUydzZjRt3UDf", "SQ.hb1scjdCWL89PtAkR0AVH9-dNH5R0FsN", "SQ.DKiPmNQT_aUFndwpRiUbgkRj4DPHgGjd",
                        "SQ.RwKcMXVadHZub1qL0Y5c1gmNU1_vHFme", "SQ.1sw7ZtgO9JRb1kUEuhVz1wBix5_8Opci", "SQ.V7DQqMKG7bcyxiMZK9wNjkK-udR7hrad",
                        "SQ.R8nT1N2qQFMc_uVMQUVMw-D2GcVmb5v6", "SQ.DPa_ORXLkGyyCbW9SWeqePfortM-Vdlm", "SQ.koyLEKoDOQtGHjb4r0m3o2SXxI09Z_sI"),
                attributeResult);

        attributeValue = "lengths";
        digest = "Ms_ixPgQMJaM54dVntLWeovXSO7ljvZh";
        finalRequest = baseUrl + "/attribute/collection/" + attributeValue + "/" + digest;
        attributeResult = restTemplate.getForObject(finalRequest, List.class);
        assertEquals(Arrays.asList(230218, 813184, 316620, 1531933, 576874, 270161, 1090940, 562643, 439888, 745751, 666816, 1078177, 924431, 784333, 1091291, 948066), attributeResult);

        attributeValue = "md5_sequences";
        digest = "_6iaYtcWw4TZaowlL7_64Wu9mbHpDUw4";
        finalRequest = baseUrl + "/attribute/collection/" + attributeValue + "/" + digest;
        attributeResult = restTemplate.getForObject(finalRequest, List.class);
        assertEquals(Arrays.asList("6681ac2f62509cfc220d78751b8dc524", "97a317c689cbdd7e92a5c159acd290d2",
                "54f4a74aa6392d9e19b82c38aa8ab345", "74180788027e20df3de53dcb2367d9e3", "d2787193198c8d260f58f2097f9e1e39",
                "b7ebc601f9a7df2e1ec5863deeae88a3", "a308c7ebf0b67c4926bc190dc4ba8ed8", "f66a4f8eef89fc3c3a393fe0210169f1",
                "4eae53ae7b2029b7e1075461c3eb9aac", "6757b8c7d9cca2c56401e2484cf5e2fb", "e72df2471be793f8aa06850348a896fa",
                "77945d734ab92ad527d8920c9d78ac1c", "073f9ff1c599c1a6867de2c7e4355394", "188bca5110182a786cd42686ec6882c6",
                "7e02090a38f05459102d1a9a83703534", "232475e9a61a5e07f9cb2df4a2dad757"), attributeResult);

        attributeValue = "sorted_name_length_pairs";
        digest = "EPyYaJVvxgA5x8oMe2tIC8_eyUjeUAOB";
        finalRequest = baseUrl + "/attribute/collection/" + attributeValue + "/" + digest;
        attributeResult = restTemplate.getForObject(finalRequest, List.class);
        assertEquals(Arrays.asList("4KSEeZmiD9VkYC7ecIAsX7aXQggFwp9H", "B2VqaWiRary0j7btzmlGhNuoVPqlE21J",
                "d5VdLsb-kcdSepxl1dxSNk9KWEwL2bZ2", "FU4O09TlQ0Ls3f-kumtsnAag66MxsaNH", "H9xUzOT5xqMmL-Epq0qARIT3446kTZmR",
                "hio8YzqYfZVVMcINhQPDVd5xREYAMrzA", "kH3mpix7X2RAEENZYueDqxK0X5m06pan", "lKQL4D5qqnuToblXhGL1ZXMAuR6o6ng4",
                "NwP-e9miFDExu2aFejtku7KvmGGXla2R", "pFlAwWxDmSH9KkdK_yHBA0xtql0lSmTf", "QveClkmJYVlkucglvpJmON5uTL0Eu0No",
                "qWT4vrlChGgN5N_w3_-_NyQfkFxn98eN", "sdKiPkMb6nKtNfN_BP-X8284rdYQYDhM", "uKS-fRsOtAxqCTxktf6kUhwsBC9BMT9I",
                "w7OoScU_vDJJtw4UsGV_hd5u_je0fTei", "w8srPsF7XUe6GMvrLRI5gXX_DYAO3k_O"), attributeResult);
    }
}

