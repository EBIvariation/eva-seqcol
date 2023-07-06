package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public abstract class SeqColEntity {

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    public enum NamingConvention {
        ENA, GENBANK, UCSC
    }
}
