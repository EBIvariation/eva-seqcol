package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Data
@Table(name = "sequence_collections_L1")
@IdClass(SeqColId.class)
public class SeqColLevelOneEntity extends SeqColEntity{

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.EAGER)
    private JSONLevelOne seqColLevel1Object;

    @Id
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected NamingConvention namingConvention;

    public SeqColLevelOneEntity(String digest, NamingConvention namingConvention, JSONLevelOne jsonLevelOne){
        super(digest, namingConvention);
        this.seqColLevel1Object = jsonLevelOne;
        this.namingConvention = namingConvention;
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
