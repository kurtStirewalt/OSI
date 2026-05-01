package org.osi.exception;

/**
 * Exception thrown when the input is invalid.
 * This includes cases where the file does not exist, is not readable,
 * or if the file/String contains invalid YAML/JSON content.
 *
 */
public class InvalidInputException extends RuntimeException {

    /**
     * Constructs a new InvalidInputException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidInputException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
