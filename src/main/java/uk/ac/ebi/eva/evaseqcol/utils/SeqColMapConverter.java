package uk.ac.ebi.eva.evaseqcol.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class SeqColMapConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private SeqColMapConverter() {
    }

    /**
     * Return a map representation of the given seqColLevelTwoEntity with its attributes ("lengths", "names", etc.)
     * NOTE!: The level 0 digest as well as the naming convention values will be lost
     */
    public static Map<String, List<?>> getSeqColLevelTwoMap(SeqColLevelTwoEntity levelTwoEntity) {
        return OBJECT_MAPPER.convertValue(levelTwoEntity, new TypeReference<>() {});
    }

    /**
     * Read the json file for the given filePath and return Map representation of
     * its content. Supports both file paths and classpath: URLs.
     */
    public static Map<String, Object> jsonToMap(String filePath) throws IOException {
        TypeReference<Map<String, Object>> mapType = new TypeReference<>() {};
        if (filePath.startsWith("classpath:")) {
            ClassPathResource resource = new ClassPathResource(filePath.substring("classpath:".length()));
            try (InputStream inputStream = resource.getInputStream()) {
                return OBJECT_MAPPER.readValue(inputStream, mapType);
            }
        }
        return OBJECT_MAPPER.readValue(new File(filePath), mapType);
    }
}
