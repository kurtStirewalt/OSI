package org.osi.app;

import org.osi.converter.Converter;
import org.osi.converter.ConverterFactory;
import org.osi.converter.ConversionDirection;
import org.osi.exception.ConversionException;
import org.osi.exception.InvalidInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application class for the OSI-Salesforce Converter.
 *
 * <p>Converts between Salesforce Semantic Model and OSI formats.
 * Output file is placed in the same directory as input with the appropriate extension.</p>
 *
 */
public class OsiSalesforceConverter {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.exit(1);
        }

        try {
            OsiSalesforceConverter app = new OsiSalesforceConverter();
            String directionArg = args[0];
            Path inputPath = Paths.get(args[1]);

            ConversionDirection direction = parseDirection(directionArg);
            app.convert(direction, inputPath);
        } catch (InvalidInputException e) {
            System.exit(2);
        } catch (ConversionException e) {
            System.exit(3);
        }
    }

    private static ConversionDirection parseDirection(String direction) {
        return switch (direction.toLowerCase()) {
            case "tosf" -> ConversionDirection.OSI_TO_SALESFORCE;
            case "toosi" -> ConversionDirection.SALESFORCE_TO_OSI;
            default -> throw new InvalidInputException(
                "Invalid direction: " + direction + ". Expected: toSF or toOSI"
            );
        };
    }

    /**
     * Converts a file. Output files are written to the same directory as the input file,
     * with filenames based on model apiNames.
     *
     * @param direction The conversion direction
     * @param inputPath path to the input file
     */
    public void convert(ConversionDirection direction, Path inputPath) {
        if (!Files.exists(inputPath)) {
            throw new InvalidInputException("Input file not found: " + inputPath);
        }

        Path outputDir = inputPath.getParent();
        if (outputDir == null) {
            outputDir = Path.of(".");
        }

        Converter converter = ConverterFactory.getConverter(direction);
        converter.convert(inputPath, outputDir);
    }

}
