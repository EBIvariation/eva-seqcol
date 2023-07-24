package uk.ac.ebi.eva.evaseqcol.exception;

public class AssemblySequenceNotFoundException extends RuntimeException{

    public AssemblySequenceNotFoundException(String accession) {
        super("No assembly corresponding to accession " + accession + " could be found");
    }
}
