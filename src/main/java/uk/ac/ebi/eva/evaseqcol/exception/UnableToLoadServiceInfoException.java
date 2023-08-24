package uk.ac.ebi.eva.evaseqcol.exception;

public class UnableToLoadServiceInfoException extends RuntimeException {

    public UnableToLoadServiceInfoException(String serviceInfoFilePath) {
        super("Unable to load service-info file: " + serviceInfoFilePath);
    }
}
