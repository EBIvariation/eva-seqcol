package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "seqcol_md")
@Data
public class SeqColMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "md_id")
    private Long mdId;

    @Column(name = "source_id")
    private String sourceIdentifier;  // Eg: INSDC Acession

    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    private SeqColEntity.NamingConvention namingConvention;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date timestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seqcol_digest")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SeqColLevelOneEntity seqColLevelOne;

    public SeqColMetadataEntity setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
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

    public SeqColMetadataEntity setNamingConvention(SeqColEntity.NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeqColMetadataEntity)) return false;
        SeqColMetadataEntity that = (SeqColMetadataEntity) o;
        return Objects.equals(sourceIdentifier, that.sourceIdentifier) && Objects.equals(sourceUrl, that.sourceUrl)
                && namingConvention == that.namingConvention && Objects.equals(seqColLevelOne, that.seqColLevelOne);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIdentifier, sourceUrl, namingConvention, seqColLevelOne);
    }
}
