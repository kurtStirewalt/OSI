package org.osi.converter;

/**
 * Factory for creating converters based on conversion direction.
 *
 */
public class ConverterFactory {

    /**
     * Creates a converter for the specified direction.
     *
     * @param direction The conversion direction
     * @return A Converter instance configured for the specified direction
     */
    public static Converter getConverter(ConversionDirection direction) {
        return new ConverterImpl(direction);
    }
}
