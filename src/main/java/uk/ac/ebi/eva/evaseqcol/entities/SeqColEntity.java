package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class SeqColEntity {

    protected String digest; // The level 0 digest


    public enum NamingConvention {
        ENA, GENBANK, UCSC, TEST
    }
}
