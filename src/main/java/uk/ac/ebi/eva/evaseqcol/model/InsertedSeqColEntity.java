package uk.ac.ebi.eva.evaseqcol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This entity will hold minimal seqcol information that will be returned
 * to the user upon ingestion*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertedSeqColEntity {

    private String digest; // Level 0 digest
    @JsonProperty("naming_convention")
    private String namingConvention;
}
