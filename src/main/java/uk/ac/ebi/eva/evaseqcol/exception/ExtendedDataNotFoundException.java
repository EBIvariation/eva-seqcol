package uk.ac.ebi.eva.evaseqcol.exception;

public class ExtendedDataNotFoundException extends RuntimeException{
    
    public ExtendedDataNotFoundException(String digest) {
        super("No seqcol extended data with digest " + digest + " could be found in the db");
    }
}
