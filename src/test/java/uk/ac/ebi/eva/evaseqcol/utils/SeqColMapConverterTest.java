package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("h2")
class SeqColMapConverterTest {

    @Autowired
    private SeqColGenerator seqColGenerator;

    @Value("${service.info.file.path}")
    private String serviceInfoFilePath;

    @Test
    void seqColLevelTwoMapConverterTest() {
        SeqColLevelTwoEntity levelTwoEntity = seqColGenerator.generateLevelTwoEntity();
        Map<String, List<?>> levelTwoMap = SeqColMapConverter.getSeqColLevelTwoMap(levelTwoEntity);
        assertFalse(levelTwoMap.keySet().isEmpty()); // At least we should have the "sequences", "lengths" and "names"
        assertTrue(levelTwoMap.containsKey("sequences"));
        assertTrue(levelTwoMap.containsKey("lengths"));
        assertTrue(levelTwoMap.containsKey("names"));
        assertNotNull(levelTwoMap.get("sequences"));
        assertNotNull(levelTwoMap.get("lengths"));
        assertNotNull(levelTwoMap.get("names"));
    }

    @Test
    void jsonToMapTest() throws IOException {
        Map<String, Object> serviceInfoMap = SeqColMapConverter.jsonToMap(serviceInfoFilePath);
        assertNotNull(serviceInfoMap);
    }
}