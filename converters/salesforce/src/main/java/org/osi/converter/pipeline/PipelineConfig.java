package org.osi.converter.pipeline;

import java.util.List;
import java.util.Map;

/**
 * Root configuration model matching pipeline-config.yaml structure.
 *
 */
public class PipelineConfig {
    private Map<String, List<String>> pipelines;           // Direction -> handler names
    private Map<String, DirectionConfig> directionConfigs; // Direction -> config

    public Map<String, List<String>> getPipelines() {
        return pipelines;
    }

    public void setPipelines(Map<String, List<String>> pipelines) {
        this.pipelines = pipelines;
    }

    public Map<String, DirectionConfig> getDirectionConfigs() {
        return directionConfigs;
    }

    public void setDirectionConfigs(Map<String, DirectionConfig> directionConfigs) {
        this.directionConfigs = directionConfigs;
    }
}
