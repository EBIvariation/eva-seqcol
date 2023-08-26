package uk.ac.ebi.eva.evaseqcol.entities;

import com.sun.istack.NotNull;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@EqualsAndHashCode
public class SeqColId implements Serializable {
    @NotNull
    private String digest;

    @Enumerated(EnumType.STRING)
    private SeqColEntity.NamingConvention namingConvention;
}
