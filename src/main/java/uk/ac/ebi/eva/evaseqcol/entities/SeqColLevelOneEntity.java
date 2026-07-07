package uk.ac.ebi.eva.evaseqcol.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Data
@Table(name = "sequence_collections_L1")
public class SeqColLevelOneEntity extends SeqColEntity {

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONLevelOne seqColLevel1Object;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "seqcol_md", joinColumns = @JoinColumn(name = "digest", nullable = false, insertable = false, updatable = false))
    private Set<SeqColMetadataEntity> metadata;

    public SeqColLevelOneEntity(String digest, JSONLevelOne jsonLevelOne) {
        super(digest);
        this.seqColLevel1Object = jsonLevelOne;
    }

    public void addMetadata(SeqColMetadataEntity seqColMetadataEntity) {
        if (metadata == null) metadata = new HashSet<>();
        metadata.add(seqColMetadataEntity);
    }

    @Override
    public String toString() {
        return "{\n" + "    \"sequences\": \"" + seqColLevel1Object.getSequences() + "\",\n" + "    \"names\": \"" + seqColLevel1Object.getNames() + "\"\n" + "}";
    }
}
