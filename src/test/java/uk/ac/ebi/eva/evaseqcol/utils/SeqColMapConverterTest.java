package uk.ac.ebi.eva.evaseqcol.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeqColMapConverterTest {

    private SeqColMapConverter seqColMapConverter = new SeqColMapConverter();

    @Autowired
    private SeqColGenerator seqColGenerator;

    @Value("${service.info.file.path}")
    private String serviceInfoFilePath;

    @Test
    void setSeqColLevelOneMapConverterTest() {
        SeqColLevelOneEntity levelOneEntity = seqColGenerator.generateLevelOneEntity();
        Map<String, String> levelOneMap = seqColMapConverter.getSeqColLevelOneMap(levelOneEntity);
        assertFalse(levelOneMap.keySet().isEmpty()); // At least we should have the "sequences", "lengths" and "names"
        assertTrue(levelOneMap.containsKey("sequences"));
        assertTrue(levelOneMap.containsKey("lengths"));
        assertTrue(levelOneMap.containsKey("names"));
        assertNotNull(levelOneMap.get("sequences"));
        assertNotNull(levelOneMap.get("lengths"));
        assertNotNull(levelOneMap.get("names"));
    }

    @Test
    void seqColLevelTwoMapConverterTest() {
        SeqColLevelTwoEntity levelTwoEntity = seqColGenerator.generateLevelTwoEntity();
        Map<String, List<String>> levelTwoMap = seqColMapConverter.getSeqColLevelTwoMap(levelTwoEntity);
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