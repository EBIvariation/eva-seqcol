package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AssemblySequencesEntity {

    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String insdcAccession;

    @ApiModelProperty(value = "List of all sequences of the assembly.")
    private List<Sequence> sequences;

    public AssemblySequencesEntity setInsdcAccession(String insdcAccession) {
        this.insdcAccession = insdcAccession;
        return this;
    }

    public AssemblySequencesEntity setSequences(List<Sequence> sequences) {
        this.sequences = sequences;
        return this;
    }
}
