package uk.ac.ebi.eva.evaseqcol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeqColMetadataDTO {

    private String sourceId;
    private String sourceUrl;
    private SeqColEntity.NamingConvention namingConvention;
    private Date timestamp;

    public static SeqColMetadataDTO toMetadataDTO(SeqColMetadataEntity metadataEntity) {
        return new SeqColMetadataDTO(
                metadataEntity.getSourceIdentifier(),
                metadataEntity.getSourceUrl(),
                metadataEntity.getNamingConvention(),
                metadataEntity.getTimestamp()
        );
    }
}
