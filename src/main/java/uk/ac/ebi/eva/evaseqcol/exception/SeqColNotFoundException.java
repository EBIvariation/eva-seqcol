package uk.ac.ebi.eva.evaseqcol.exception;

public class SeqColNotFoundException extends RuntimeException {

    public SeqColNotFoundException(String accession) {
        super("No seqCol data corresponding to accession " + accession + " could be found");
    }
}
