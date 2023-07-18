package uk.ac.ebi.eva.evaseqcol.exception;

public class duplicateSeqColException extends RuntimeException {

    public duplicateSeqColException(String digest) {
        super("A similar seqCol already exists with digest " + digest);
    }
}
