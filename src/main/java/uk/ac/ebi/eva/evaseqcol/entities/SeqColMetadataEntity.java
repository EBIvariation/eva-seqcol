package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@Embeddable
@NoArgsConstructor
public class SeqColMetadataEntity {

    @Column(name = "source_id")
    private String sourceIdentifier;  // Eg: INSDC Acession

    @Column(name = "source_url")
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "naming_convention")
    private SeqColEntity.NamingConvention namingConvention;

    @Column(name = "created_on", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdOn = new Date();

    public SeqColMetadataEntity(String sourceIdentifier, String sourceUrl, SeqColEntity.NamingConvention namingConvention,
                                Date createdOn) {
        this.sourceIdentifier = sourceIdentifier;
        this.sourceUrl = sourceUrl;
        this.namingConvention = namingConvention;
        this.createdOn = createdOn;
    }

    public SeqColMetadataEntity setNamingConvention(SeqColEntity.NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        return this;
    }

    public SeqColMetadataEntity setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
        return this;
    }

    public SeqColMetadataEntity setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }
}
