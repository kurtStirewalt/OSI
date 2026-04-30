package org.osi.exception;

/**
 * Exception thrown when a conversion operation fails.
 * This can happen during YAML to JSON or JSON to YAML conversion.
 *
 */
public class ConversionException extends RuntimeException {

    /**
     * Constructs a new ConversionException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConversionException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConversionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
