package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;

@Data
public class SequenceEntity{

    @ApiModelProperty(value = "GenBank's name of the sequence.")
    private String genbankSequenceName;

    @ApiModelProperty(value = "ENA's name of the sequence")
    private String enaSequenceName;

    @ApiModelProperty(value = "Sequence's INSDC accession.")
    private String insdcAccession;

    @ApiModelProperty(value = "Sequence's RefSeq accession.")
    private String refseq;

    @ApiModelProperty(value = "Sequence's length")
    private Long seqLength;

    @ApiModelProperty(value = "Sequence's UCSC style name")
    private String ucscName;

    @ApiModelProperty(value = "Sequence's MD5 checksum value.")
    private String md5checksum;

    @ApiModelProperty(value = "Sequence's TRUNC512 checksum value.")
    private String trunc512checksum;

    public enum ContigType {
        SCAFFOLD,
        CHROMOSOME
    }

    @ApiModelProperty(value = "Type of contig: chromosome (or) scaffold")
    private ContigType contigType;

    @ApiModelProperty(value = "Assembly that this sequence belongs to.")
    private AssemblyEntity assembly;

    public SequenceEntity setGenbankSequenceName(String name) {
        this.genbankSequenceName = name;
        return this;
    }

    public SequenceEntity setEnaSequenceName(String enaSequenceName) {
        this.enaSequenceName = enaSequenceName;
        return this;
    }

    public SequenceEntity setInsdcAccession(String insdcAccession) {
        this.insdcAccession = insdcAccession;
        return this;
    }

    public SequenceEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public SequenceEntity setSeqLength(Long seqLength) {
        this.seqLength = seqLength;
        return this;
    }

    public SequenceEntity setUcscName(String ucscName) {
        this.ucscName = ucscName;
        return this;
    }

    public SequenceEntity setMd5checksum(String md5checksum) {
        this.md5checksum = md5checksum;
        return this;
    }

    public SequenceEntity setTrunc512checksum(String trunc512checksum) {
        this.trunc512checksum = trunc512checksum;
        return this;
    }

    public void setContigType(ContigType contigType) {
        this.contigType = contigType;
    }

    public SequenceEntity setAssembly(AssemblyEntity assembly) {
        this.assembly = assembly;
        return this;
    }
}
