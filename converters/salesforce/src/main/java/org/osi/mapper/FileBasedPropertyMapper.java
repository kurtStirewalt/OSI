package org.osi.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.osi.exception.InvalidInputException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property mapper that loads mappings from a YAML configuration file.
 *
 * <p>This mapper reads the bundled mappings.yaml which defines OSI-Salesforce
 * property mappings. The transformation is performed by the converter classes.</p>
 *
 */
public class FileBasedPropertyMapper implements PropertyMapper {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private final Map<String, String> mappings;
    private final Map<String, String> reverseMappings;

    /**
     * Constructs a FileBasedPropertyMapper with the specified mappings.
     *
     * @param mappings the OSI-Salesforce mappings
     */
    public FileBasedPropertyMapper(Map<String, String> mappings) {
        this.mappings = mappings != null ? new LinkedHashMap<>(mappings) : new LinkedHashMap<>();
        this.reverseMappings = generateReverseMappings();
    }

    /**
     * Generates reverse mappings (Salesforce to OSI) from the OSI to Salesforce mappings.
     *
     * @return the Salesforce-to-OSI mappings
     */
    private Map<String, String> generateReverseMappings() {
        return mappings.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Creates a FileBasedPropertyMapper from a classpath resource.
     *
     * @param resourcePath the path to the YAML resource (e.g., "/mappings.yaml")
     * @return a new FileBasedPropertyMapper
     * @throws InvalidInputException if the resource cannot be read or parsed
     */
    public static FileBasedPropertyMapper fromResource(String resourcePath) {
        try (InputStream is = FileBasedPropertyMapper.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new InvalidInputException("Mapping configuration resource not found: " + resourcePath);
            }

            String content = new String(is.readAllBytes());
            return parseYaml(content);
        } catch (IOException e) {
            throw new InvalidInputException("Failed to read mapping configuration resource: " + resourcePath, e);
        }
    }

    private static FileBasedPropertyMapper parseYaml(String content) {
        try {
            Map<String, String> mappings = YAML_MAPPER.readValue(
                content,
                new TypeReference<LinkedHashMap<String, String>>() {}
            );
            return new FileBasedPropertyMapper(mappings);
        } catch (Exception e) {
            throw new InvalidInputException("Failed to parse YAML mapping configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getOsiToSalesforceMappings() {
        return Collections.unmodifiableMap(mappings);
    }

    @Override
    public Map<String, String> getSalesforceToOsiMappings() {
        return Collections.unmodifiableMap(reverseMappings);
    }

    @Override
    public String toString() {
        return "FileBasedPropertyMapper{" + "mappingCount=" + mappings.size() + '}';
    }
}
