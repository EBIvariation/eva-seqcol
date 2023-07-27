package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SeqColSequenceEntity {
    @ApiModelProperty(value = "Sequence's Refseq accession.")
    private String refseq;
    @ApiModelProperty(value = "Sequence's MD5 checksum value.")
    private String sequenceMD5;
    @ApiModelProperty(value = "Sequence's defalut (ga4gh) checksum value")
    private String sequence;

    public SeqColSequenceEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public SeqColSequenceEntity setSequenceMD5(String sequenceMD5) {
        this.sequenceMD5 = sequenceMD5;
        return this;
    }

    public SeqColSequenceEntity setSequence(String sequence) {
        this.sequence = sequence;
        return this;
    }
}
