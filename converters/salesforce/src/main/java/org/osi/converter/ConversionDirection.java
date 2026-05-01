package org.osi.converter;

/**
 * Enum representing the direction of conversion.
 *
 */
public enum ConversionDirection {
    /**
     * Converting from OSI YAML format to Salesforce JSON format.
     */
    OSI_TO_SALESFORCE,

    /**
     * Converting from Salesforce JSON format to OSI YAML format.
     */
    SALESFORCE_TO_OSI;

    /**
     * Converts enum name to pipeline configuration key.
     * Maps OSI_TO_SALESFORCE -> "osiToSalesforce" and SALESFORCE_TO_OSI -> "salesforceToOsi"
     *
     * @return The pipeline key used in YAML configuration
     */
    public String toPipelineKey() {
        return switch (this) {
            case OSI_TO_SALESFORCE -> "osiToSalesforce";
            case SALESFORCE_TO_OSI -> "salesforceToOsi";
        };
    }
}
