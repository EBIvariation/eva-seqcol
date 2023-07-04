package uk.ac.ebi.eva.evaseqcol.exception;

public class AssemblyNotFoundException extends RuntimeException {

    public AssemblyNotFoundException(String accession) {
        super("No assembly corresponding to accession " + accession + " could be found");
    }
}
