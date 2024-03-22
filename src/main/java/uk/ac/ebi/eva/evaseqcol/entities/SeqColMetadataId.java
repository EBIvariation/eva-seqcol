package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SeqColMetadataId implements Serializable {

    @NotNull
    private String seqColDigest;
    @NotNull
    private SeqColMetadata.SourceIdentifier sourceIdentifier; // Eg: INSDC, UCSC, GENBANK, etc..

}
