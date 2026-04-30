package org.osi.mapper;

import java.util.Map;

/**
 * Interface for mapping properties between OSI and Salesforce formats during conversion.
 *
 * <p>Implementations of this interface define bidirectional mappings between OSI YAML format
 * and Salesforce JSON format. This supports nested paths using dot notation
 * (e.g., "datasets.name" ↔ "semanticDataObjects.apiName").</p>
 *
 *
 */
public interface PropertyMapper {

    /**
     * Returns the mapping from OSI property paths to Salesforce property paths.
     *
     * @return a map where keys are OSI property paths and values are Salesforce property paths
     */
    Map<String, String> getOsiToSalesforceMappings();

    /**
     * Returns the mapping from Salesforce property paths to OSI property paths.
     * <p>This is the reverse mapping of {@link #getOsiToSalesforceMappings()}.
     *
     * @return a map where keys are Salesforce property paths and values are OSI property paths
     */
    Map<String, String> getSalesforceToOsiMappings();
}
