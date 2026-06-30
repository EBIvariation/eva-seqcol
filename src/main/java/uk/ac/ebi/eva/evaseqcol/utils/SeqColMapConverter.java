package uk.ac.ebi.eva.evaseqcol.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

public class SeqColMapConverter {

    /**
     * Return a map representation of the given seqColLevelOneEntity with the minimal
     * required attributes ("lengths", "names", etc.)
     * NOTE!: Not all the attributes will be returned, only the ones concerned by the comparison
     * NOTE!: The level 0 digest as well as the naming convention values will be lost
     */
    public static Map<String, String> getSeqColLevelOneMap(SeqColLevelOneEntity seqColLevelOneEntity) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> seqColMap = objectMapper.convertValue(seqColLevelOneEntity.getSeqColLevel1Object(), Map.class);
        return seqColMap;
    }

    /**
     * Return a map representation of the given seqColLevelTwoEntity with the minimal
     * required attributes ("lengths", "names", etc.)
     * NOTE!: Not all the attributes will be returned, only the ones concerned by the comparison
     * NOTE!: The level 0 digest as well as the naming convention values will be lost
     */
    public static Map<String, List<?>> getSeqColLevelTwoMap(SeqColLevelTwoEntity levelTwoEntity) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<?>> seqColMap = objectMapper.convertValue(levelTwoEntity, Map.class);
        return seqColMap;
    }

    /**
     * Read the json file for the given filePath and return Map representation of
     * its content. Supports both file paths and classpath: URLs.*/
    public static Map<String, Object> jsonToMap(String filePath) throws IOException {
        Map<String, Object> jsonMap;
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if (filePath.startsWith("classpath:")) {
            String resourcePath = filePath.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                jsonMap = mapper.readValue(inputStream, Map.class);
            }
        } else {
            File file = new File(filePath);
            jsonMap = mapper.readValue(file, Map.class);
        }

        return jsonMap;
    }
}
