package org.osi.converter.pipeline;

import org.osi.converter.*;
import org.osi.exception.ConversionException;

/**
 * Factory for creating handler instances using a hardcoded registry.
 * pipeline configuration specifies which handlers to run and in what order.
 *
 */
public class HandlerFactory {
    private final CustomExtensionHandler customExtensionHandler;

    public HandlerFactory(CustomExtensionHandler customExtensionHandler) {
        this.customExtensionHandler = customExtensionHandler;
    }

    /**
     * Creates a handler instance from the registered handler names.
     *
     * @param handlerName The handler name from pipeline config
     * @param direction The conversion direction
     * @return A PipelineStep instance
     * @throws ConversionException if handler name is unknown
     */
    public PipelineStep createHandler(String handlerName, ConversionDirection direction) {
        return switch(handlerName) {
            case "DatasetMappingHandler" ->
                new DatasetMappingHandler(direction, customExtensionHandler);
            case "FieldMappingHandler" ->
                new FieldMappingHandler(direction, customExtensionHandler);
            case "RelationshipMappingHandler" ->
                new RelationshipMappingHandler(direction, customExtensionHandler);
            case "MetricMappingHandler" ->
                new MetricMappingHandler(direction, customExtensionHandler);
            case "SemanticModelMappingHandler" ->
                new SemanticModelMappingHandler(direction, customExtensionHandler);
            default ->
                throw new ConversionException("Unknown handler: " + handlerName);
        };
    }
}
