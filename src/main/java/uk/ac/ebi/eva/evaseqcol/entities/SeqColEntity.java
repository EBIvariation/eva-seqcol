package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public abstract class SeqColEntity {

    protected String digest; // The level 0 digest

    protected NamingConvention namingConvention;


    public enum NamingConvention {
        ENA, GENBANK, UCSC
    }
}
