package uk.ac.ebi.eva.evaseqcol.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyAlreadyIngestedException;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.AttributeNotDefinedException;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.exception.IncorrectAccessionException;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;

/**
 * Maps the exceptions thrown by the controllers to HTTP responses. The body is the plain text exception message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({SeqColNotFoundException.class, AssemblyNotFoundException.class,
            AttributeNotDefinedException.class})
    public ResponseEntity<String> handleNotFound(RuntimeException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DuplicateSeqColException.class, AssemblyAlreadyIngestedException.class})
    public ResponseEntity<String> handleConflict(RuntimeException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({IncorrectAccessionException.class, IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Any other error is a server error. Spring MVC's own exceptions (eg: unreadable request body) are rethrown so
     * that they keep their default handling and status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleServerError(Exception e) throws Exception {
        if (e instanceof ErrorResponse) {
            throw e;
        }
        logger.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private ResponseEntity<String> respond(HttpStatus status, String message) {
        return new ResponseEntity<>(message, status);
    }
}
