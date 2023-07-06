package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AssemblySequenceEntity {

    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String insdcAccession;

    @ApiModelProperty(value = "List of all sequences of the assembly.")
    private List<SeqColSequenceEntity> sequences;

    public AssemblySequenceEntity setInsdcAccession(String insdcAccession) {
        this.insdcAccession = insdcAccession;
        return this;
    }

    public AssemblySequenceEntity setSequences(List<SeqColSequenceEntity> sequences) {
        this.sequences = sequences;
        return this;
    }
}
