package org.osi.converter.pipeline;

import java.util.Map;

/**
 * Base interface for pipeline steps.
 * Both mapping handlers and special wrapper steps implement this interface.
 *
 */
public interface PipelineStep {
    /**
     * Execute this pipeline step.
     *
     * @param sourceData The source data (may be modified by handler)
     * @param outputData The output data being built
     * @param mappings Property mappings
     */
    void execute(Map<String, Object> sourceData, Map<String, Object> outputData, Map<String, String> mappings);
}
