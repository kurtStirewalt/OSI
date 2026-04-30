package org.osi.converter.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.osi.exception.ConversionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads pipeline configuration from YAML.
 * Uses Jackson to deserialize osi-salesforce-converter-config.yaml into PipelineConfig model.
 *
 */
public class PipelineConfigLoader {
    private static final String CONFIG_RESOURCE = "/osi-salesforce-converter-config.yaml";
    private final ObjectMapper yamlMapper;

    public PipelineConfigLoader() {
        YAMLFactory yamlFactory = new YAMLFactory();
        this.yamlMapper = new ObjectMapper(yamlFactory);
    }

    public static PipelineConfig loadFromResource() {
        PipelineConfigLoader loader = new PipelineConfigLoader();
        try (InputStream stream = PipelineConfigLoader.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (stream == null) {
                throw new ConversionException("Pipeline config not found: " + CONFIG_RESOURCE);
            }

            // Read full YAML into map
            Map<String, Object> rawConfig = loader.yamlMapper.readValue(stream, new TypeReference<>() {});

            // Parse into structured config
            PipelineConfig config = new PipelineConfig();
            config.setPipelines((Map<String, List<String>>) rawConfig.get("pipelines"));

            // Parse direction configs (osiToSalesforce, salesforceToOsi sections)
            Map<String, DirectionConfig> directionConfigs = new HashMap<>();
            for (String direction : config.getPipelines().keySet()) {
                if (rawConfig.containsKey(direction)) {
                    DirectionConfig dirConfig = loader.yamlMapper.convertValue(
                        rawConfig.get(direction),
                        DirectionConfig.class
                    );
                    directionConfigs.put(direction, dirConfig);
                }
            }
            config.setDirectionConfigs(directionConfigs);

            return config;
        } catch (IOException e) {
            throw new ConversionException("Failed to load pipeline config", e);
        }
    }
}
