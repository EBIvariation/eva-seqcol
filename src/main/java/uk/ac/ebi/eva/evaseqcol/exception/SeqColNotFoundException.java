package uk.ac.ebi.eva.evaseqcol.exception;

public class SeqColNotFoundException extends RuntimeException {

    public SeqColNotFoundException(String digest) {
        super("No seqCol corresponding to digest " + digest + " could be found in DB");
    }
}
