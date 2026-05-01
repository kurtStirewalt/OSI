package org.osi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.osi.converter.ConverterConstants.Level;
import org.osi.converter.pipeline.PipelineStep;
import org.osi.exception.ConversionException;
import org.osi.util.MappingUtils;

import java.util.Map;
import java.util.Set;

import static org.osi.converter.ConverterConstants.AI_CONTEXT;
import static org.osi.converter.ConverterConstants.API_NAME;
import static org.osi.converter.ConverterConstants.BUSINESS_PREFERENCES;
import static org.osi.converter.ConverterConstants.LABEL;

/**
 * Bidirectional handler for mapping top-level semantic model properties.
 *
 * <p>Supports both conversion directions:
 * <ul>
 *   <li>OSI → Salesforce: name → apiName, apply SF defaults, restore custom_extensions</li>
 *   <li>Salesforce → OSI: apiName → name, strip SF defaults, store custom_extensions</li>
 * </ul>
 *
 * <p>Handles only top-level scalar properties (not arrays).
 *
 */
public class SemanticModelMappingHandler implements PipelineStep {

    private final ConversionDirection direction;
    private final CustomExtensionHandler customExtensionHandler;
    private final ObjectMapper jsonMapper;

    public SemanticModelMappingHandler(ConversionDirection direction, CustomExtensionHandler customExtensionHandler) {
        this.direction = direction;
        this.customExtensionHandler = customExtensionHandler;
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void execute(Map<String, Object> sourceData, Map<String, Object> outputData, Map<String, String> mappings) {
        if (direction == ConversionDirection.OSI_TO_SALESFORCE) {
            mapOsiToSalesforce(sourceData, outputData, mappings);
        } else {
            mapSalesforceToOsi(sourceData, outputData, mappings);
        }
    }

    /**
     * Maps OSI top-level properties to Salesforce format.
     * Steps: generic mappings → manual conversions → restore custom extensions
     */
    private void mapOsiToSalesforce(
            Map<String, Object> sourceData, Map<String, Object> outputData, Map<String, String> mappings) {

        var mappedData = GenericMappingEngine.applyMappings(sourceData, mappings);
        outputData.putAll(mappedData);

        convertAiContextToBusinessPreferences(sourceData, outputData);

        customExtensionHandler.restoreCustomExtensionsAtLevel(outputData, sourceData, Level.SEMANTIC_MODEL);

        applyDefaults(outputData);
    }

    /**
     * Maps Salesforce top-level properties to OSI format.
     * Steps: generic mappings → manual conversions → store custom extensions
     */
    private void mapSalesforceToOsi(
            Map<String, Object> sourceData, Map<String, Object> outputData, Map<String, String> mappings) {

        var mappedData = GenericMappingEngine.applyMappings(sourceData, mappings);
        outputData.putAll(mappedData);

        convertBusinessPreferencesToAiContext(sourceData, outputData);

        Set<String> allHandledProps = MappingUtils.extractTopLevelKeys(mappings);
        allHandledProps.add(BUSINESS_PREFERENCES);

        customExtensionHandler.storeUnmappedProperties(outputData, sourceData, allHandledProps, Level.SEMANTIC_MODEL);
    }

    /**
     * Converts Salesforce businessPreferences to OSI ai_context.
     *
     * @param sourceData Salesforce data containing businessPreferences
     * @param outputData OSI data to populate with ai_context
     */
    private void convertBusinessPreferencesToAiContext(Map<String, Object> sourceData, Map<String, Object> outputData) {
        Object businessPreferences = sourceData.get(BUSINESS_PREFERENCES);
        if (businessPreferences != null) {
            outputData.put(AI_CONTEXT, businessPreferences);
        }
    }

    /**
     * Converts OSI ai_context (string or object) to Salesforce businessPreferences (string).
     *
     * <p>ai_context can be:
     * <ul>
     *   <li>A simple string - copied as-is</li>
     *   <li>An object - serialized to JSON string</li>
     * </ul>
     *
     * @param sourceData OSI data containing ai_context
     * @param outputData Salesforce data to populate with businessPreferences
     */
    private void convertAiContextToBusinessPreferences(Map<String, Object> sourceData, Map<String, Object> outputData) {
        Object aiContextObj = sourceData.get(AI_CONTEXT);
        if (aiContextObj == null) {
            return;
        }

        String businessPreferences;
        if (aiContextObj instanceof String) {
            businessPreferences = aiContextObj.toString();
        } else {
            try {
                businessPreferences = jsonMapper.writeValueAsString(aiContextObj);
            } catch (JsonProcessingException e) {
                throw new ConversionException("Failed to serialize ai_context to JSON: " + e.getMessage(), e);
            }
        }
        outputData.put(BUSINESS_PREFERENCES, businessPreferences);
    }

    /**
     * Applies default values for required Salesforce semantic model properties.
     * Used when converting OSI → Salesforce.
     */
    private void applyDefaults(Map<String, Object> outputData) {
        if (!outputData.containsKey(LABEL)) {
            String apiName = (String) outputData.get(API_NAME);
            outputData.put(LABEL, apiName);
        }
    }
}
