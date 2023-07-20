package uk.ac.ebi.eva.evaseqcol.exception;

public class DuplicateSeqColException extends RuntimeException {

    public DuplicateSeqColException(String digest) {
        super("A similar seqCol already exists with digest " + digest);
    }
}
