package uk.ac.ebi.eva.evaseqcol.exception;

public class DuplicateSeqColWithDifferentMetadata extends RuntimeException{
    
    public DuplicateSeqColWithDifferentMetadata(String digest) {
        super("A similar seqCol already exists with digest " + digest + " but with different metadata");
    }
}
