package uk.ac.ebi.eva.evaseqcol.exception;

/**
 * This exception should be thrown when an undefined seqcol attribute has
 * been given (normally by the user in the POST comparison request's body)*/
public class AttributeNotDefinedException extends RuntimeException{

    public AttributeNotDefinedException(String msg) {
        super(msg);
    }
}
