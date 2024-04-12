package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import org.hibernate.annotations.Type;

import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @OneToMany(mappedBy = "seqColLevelOne", cascade = CascadeType.ALL)
    private List<SeqColMetadataEntity> metadata;

    public SeqColLevelOneEntity() {
        this.metadata = new ArrayList<>();
    }

    public SeqColLevelOneEntity(String digest, JSONLevelOne jsonLevelOne, List<SeqColMetadataEntity> metadata){
        super(digest);
        this.seqColLevel1Object = jsonLevelOne;
        this.metadata = metadata;
    }

    public void addMetadata(SeqColMetadataEntity metadata){
        this.metadata.add(metadata);
    }

    public void addAllMetadata(List<SeqColMetadataEntity> metadata){
        this.metadata.addAll(metadata);
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
