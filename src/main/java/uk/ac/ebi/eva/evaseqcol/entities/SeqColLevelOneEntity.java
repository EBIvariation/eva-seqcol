package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Data
@Table(name = "sequence_collections_L1")
public class SeqColLevelOneEntity extends SeqColEntity{

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONLevelOne seqColLevel1Object;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "seqcol_md", joinColumns =
    @JoinColumn(name = "digest", nullable = false, updatable = false))
    private Set<SeqColMetadataEntity> metadata;

    public SeqColLevelOneEntity(String digest, JSONLevelOne jsonLevelOne){
        super(digest);
        this.seqColLevel1Object = jsonLevelOne;
    }

    public void addMetadata(SeqColMetadataEntity seqColMetadataEntity){
        if(metadata == null) metadata = new HashSet<>();
        metadata.add(seqColMetadataEntity);
    }

    @Override
    public String toString() {
        return "{\n" +
                "    \"sequences\": \""+ seqColLevel1Object.getSequences() +"\",\n" +
                "    \"lengths\": \""+ seqColLevel1Object.getLengths() +"\",\n" +
                "    \"names\": \""+ seqColLevel1Object.getNames() +"\"\n" +
                "}";
    }
}
