package uk.ac.ebi.eva.evaseqcol.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AssemblyEntity {

    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String insdcAccession;

    @ApiModelProperty(value = "The name of the assembly.")
    private String name;

    @ApiModelProperty(value = "The organism of the assembly.")
    private String organism;

    @ApiModelProperty(value = "Assembly's taxonomic ID.")
    private Long taxid;

    @ApiModelProperty(value = "Assembly's Refseq accession.")
    private String refseq;

    @ApiModelProperty(value = "Are assembly's INSDC and Refseq accessions identical")
    private boolean isGenbankRefseqIdentical;

    @ApiModelProperty(value = "Assembly's MD5 checksum value.")
    private String md5checksum;

    @ApiModelProperty(value = "Assembly's TRUNC512 checksum value.")
    private String trunc512checksum;

    @ApiModelProperty(value = "List of all chromosomes of the assembly present in the database.")
    private List<ChromosomeEntity> chromosomes;


    public AssemblyEntity setName(String name) {
        this.name = name;
        return this;
    }

    public AssemblyEntity setOrganism(String organism) {
        this.organism = organism;
        return this;
    }

    public AssemblyEntity setTaxid(Long taxid) {
        this.taxid = taxid;
        return this;
    }

    public AssemblyEntity setInsdcAccession(String insdcAccession) {
        this.insdcAccession = insdcAccession;
        return this;
    }

    public AssemblyEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public boolean isGenbankRefseqIdentical() {
        return isGenbankRefseqIdentical;
    }

    public AssemblyEntity setGenbankRefseqIdentical(boolean genbankRefseqIdentical) {
        isGenbankRefseqIdentical = genbankRefseqIdentical;
        return this;
    }

    public AssemblyEntity setMd5checksum(String md5checksum) {
        this.md5checksum = md5checksum;
        return this;
    }

    public AssemblyEntity setTrunc512checksum(String trunc512checksum) {
        this.trunc512checksum = trunc512checksum;
        return this;
    }

    public AssemblyEntity setChromosomes(List<ChromosomeEntity> chromosomes) {
        this.chromosomes = chromosomes;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name :\t")
               .append(this.name)
               .append("\n")
               .append("Organism :\t")
               .append(this.organism)
               .append("\n")
               .append("Tax ID :\t")
               .append(this.taxid)
               .append("\n")
               .append("INSDC :\t")
               .append(this.insdcAccession)
               .append("\n")
               .append("Refseq :\t")
               .append(this.refseq)
               .append("\n")
               .append("INSDC & Refseq identical :\t")
               .append(isGenbankRefseqIdentical)
               .append("\n")
               .append("md5checksum :\t")
               .append(this.md5checksum)
               .append("\n")
               .append("trunc512checksum :\t")
               .append(this.trunc512checksum)
               .append("\n");
        if (this.chromosomes != null) {
            builder.append("Number of chromosomes :\t")
                   .append(this.chromosomes.size())
                   .append("\n");
        }
        return builder.toString();
    }
}
