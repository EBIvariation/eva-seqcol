package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelTwo;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "sequence_collections_L2")
public class SeqColLevelTwoEntity extends SeqColEntity{

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONLevelTwo object;

    public SeqColLevelTwoEntity setObject(JSONLevelTwo object) {
        this.object = object;
        return this;
    }

    public SeqColLevelTwoEntity(String digest, JSONLevelTwo jsonLevelTwo){
        super(digest);
        this.object = jsonLevelTwo;
    }
}
