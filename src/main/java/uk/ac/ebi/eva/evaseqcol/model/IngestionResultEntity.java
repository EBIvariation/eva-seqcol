package uk.ac.ebi.eva.evaseqcol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This Entity will hold the information that should be returned
 * upon ingestion of seqcol objects (given the assembly accession)*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionResultEntity {

    @JsonProperty("assembly_accession")
    private String assemblyAccession;
    @JsonProperty("numberOfInsertedSeqcols")
    private Integer numberOfInsertedSeqcols = 0;
    @JsonProperty("inserted_seqcols")
    private List<InsertedSeqColEntity> insertedSeqcols = new ArrayList<>();
    @JsonProperty("error_message")
    private String errorMessage = null;


    public void addInsertedSeqCol(InsertedSeqColEntity insertedSeqCol) {
        this.insertedSeqcols.add(insertedSeqCol);
    }

    /**
     * Increment the numberOfInsertedSeqcols by one*/
    public void incrementNumberOfInsertedSeqCols() {
        this.numberOfInsertedSeqcols += 1;
    }
}
