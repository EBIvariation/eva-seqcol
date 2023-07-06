package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Sequence {
    @ApiModelProperty(value = "Sequence's Refseq accession.")
    private String refseq;
    @ApiModelProperty(value = "Sequence's MD5 checksum value.")
    private String sequenceMD5;

    public Sequence setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public Sequence setSequenceMD5(String sequenceMD5) {
        this.sequenceMD5 = sequenceMD5;
        return this;
    }
}
