package uk.ac.ebi.eva.evaseqcol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NameLengthPairEntity {
    private String name;
    private String length;

    public NameLengthPairEntity(String name, String length) {
        this.name = name;
        this.length = length;
    }

    @Override
    public String toString() {
        return "{" +
                "    \"name\": \""+ name +"\"," +
                "    \"length\": \""+ length +"\"," +
                "}";
    }
}
