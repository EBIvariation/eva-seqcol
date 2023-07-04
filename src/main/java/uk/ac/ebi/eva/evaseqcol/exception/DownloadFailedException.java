package uk.ac.ebi.eva.evaseqcol.exception;

public class DownloadFailedException extends RuntimeException{
    public DownloadFailedException(String msg) {
        super(msg);
    }
}
