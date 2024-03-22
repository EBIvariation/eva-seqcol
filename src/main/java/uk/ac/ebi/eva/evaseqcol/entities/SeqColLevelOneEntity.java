package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "seqcol_digest", referencedColumnName = "seqcol_digest"),
            @JoinColumn(name = "source_id", referencedColumnName = "source_identifier")
    })
    private SeqColMetadata metadata;
    public SeqColLevelOneEntity(String digest, JSONLevelOne jsonLevelOne, SeqColMetadata metadata){
        super(digest);
        this.seqColLevel1Object = jsonLevelOne;
        this.metadata = metadata;
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
