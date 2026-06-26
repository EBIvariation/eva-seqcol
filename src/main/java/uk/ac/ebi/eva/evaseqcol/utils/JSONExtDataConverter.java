package uk.ac.ebi.eva.evaseqcol.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JSONExtDataConverter implements AttributeConverter<JSONExtData<?>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JSONExtData<?> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute.getObject());
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSONExtData to JSON string", e);
        }
    }

    @Override
    public JSONExtData<?> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Object value = objectMapper.readValue(dbData, Object.class);
            return new JSONExtData<>(value);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON string to JSONExtData", e);
        }
    }
}