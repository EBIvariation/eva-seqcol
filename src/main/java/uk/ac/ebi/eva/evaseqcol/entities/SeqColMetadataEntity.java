package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
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
public class SeqColMetadataEntity {

    @Column(name = "source_id")
    private String sourceIdentifier;  // Eg: INSDC Acession

    @Column(name = "source_url")
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "naming_convention")
    private SeqColEntity.NamingConvention namingConvention;

    @Column(name = "created_on", updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdOn;

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
