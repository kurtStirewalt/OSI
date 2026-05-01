package org.osi.converter.pipeline;

import static org.osi.converter.ConverterConstants.*;

/**
 * Configuration for a specific conversion direction.
 *
 */
public class DirectionConfig {
    private String inputFormat;
    private String outputFormat;
    private String schemaPath;
    private String extractModelNameFrom;

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getExtractModelNameFrom() {
        return extractModelNameFrom;
    }

    public void setExtractModelNameFrom(String extractModelNameFrom) {
        this.extractModelNameFrom = extractModelNameFrom;
    }

    /**
     * Get file extension based on output format.
     * @return JSON_EXTENSION for json format, YAML_EXTENSION for yaml format
     */
    public String getFileExtension() {
        return JSON.equals(outputFormat)
            ? JSON_EXTENSION
            : YAML_EXTENSION;
    }
}
