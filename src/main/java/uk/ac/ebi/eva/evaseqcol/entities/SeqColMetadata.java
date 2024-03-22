package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@IdClass(SeqColMetadataId.class)
@Table(name = "seqcol_md")
@Data
public class SeqColMetadata {

    @Id
    @Column(name = "seqcol_digest")
    private String seqColDigest;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "source_identifier")
    private SourceIdentifier sourceIdentifier;

    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    private SeqColEntity.NamingConvention namingConvention;

    @Column(insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @OneToOne(mappedBy = "metadata")
    private SeqColLevelOneEntity seqColLevelOne;

    public enum SourceIdentifier {
        Insdc
    }

    public SeqColMetadata setSeqColDigest(String digest) {
        this.seqColDigest = digest;
        return this;
    }

    public SeqColMetadata setSourceIdentifier(SourceIdentifier sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
        return this;
    }

    public SeqColMetadata setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public SeqColMetadata setNamingConvention(SeqColEntity.NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        return this;
    }
}
