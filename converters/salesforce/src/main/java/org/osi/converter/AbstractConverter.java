package org.osi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.osi.exception.ConversionException;
import org.osi.exception.InvalidInputException;
import org.osi.mapper.FileBasedPropertyMapper;
import org.osi.mapper.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for converters that provides common functionality
 * for YAML and JSON conversion with property mapping support.
 *
 */
public abstract class AbstractConverter implements Converter {

    private static final String MAPPING_RESOURCE = "/mappings.yaml";
    private static final Logger logger = LoggerFactory.getLogger(AbstractConverter.class);


    protected final PropertyMapper mapper;
    protected final ObjectMapper jsonMapper;
    protected final ObjectMapper yamlMapper;
    protected final CustomExtensionHandler customExtensionHandler;

    /**
     * Constructs an AbstractConverter with the bundled mapping configuration.
     */
    protected AbstractConverter() {
        this(loadBundledMapper());
    }

    /**
     * Loads the bundled mapping configuration from classpath.
     */
    private static PropertyMapper loadBundledMapper() {
        return FileBasedPropertyMapper.fromResource(MAPPING_RESOURCE);
    }

    /**
     * Constructs an AbstractConverter with the specified property mapper.
     *
     * @param mapper the property mapper to use (required, cannot be null)
     */
    protected AbstractConverter(PropertyMapper mapper) {
        this.mapper = mapper;

        this.jsonMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

        YAMLFactory yamlFactory = new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);

        this.yamlMapper = new ObjectMapper(yamlFactory)
            .enable(SerializationFeature.INDENT_OUTPUT);

        this.customExtensionHandler = new CustomExtensionHandler(this.jsonMapper);
    }

    @Override
    public void convert(Path inputPath, Path outputDir) {
        validateInputPath(inputPath);

        if (!Files.isDirectory(outputDir)) {
            throw new InvalidInputException("Output path must be a directory: " + outputDir);
        }

        try {
            String inputContent = Files.readString(inputPath);
            List<String> results = convert(inputContent);
            writeOutputFiles(outputDir, results);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert file: " + inputPath, e);
        }
    }

    /**
     * Writes conversion results to files using model names.
     *
     * <p>Each file is named after the model's apiName field.
     * Example: models with apiName "Sales_Model" and "Marketing_Model"
     * generate "Sales_Model.json" and "Marketing_Model.json"
     *
     * @param outputDir the directory where output files will be written
     * @param results the list of conversion results to write
     * @throws IOException if writing fails
     * @throws ConversionException if model name cannot be extracted
     */
    private void writeOutputFiles(Path outputDir, List<String> results) throws IOException {
        String extension = getFileExtension();

        for (String result : results) {
            String modelName = extractModelName(result);
            Path outputFile = outputDir.resolve(modelName + extension);
            Files.writeString(outputFile, result);
            logger.info("Generated: {}", outputFile.toAbsolutePath());
        }
    }

    /**
     * Determines the file extension to use for output files.
     * Must be overridden by subclasses to specify their output format.
     */
    protected abstract String getFileExtension();

    /**
     * Extracts model name from converted result for use as filename.
     * Must be overridden by subclasses based on their output format.
     *
     * @param result the converted result string
     * @return the model name to use for the filename
     * @throws ConversionException if model name cannot be extracted
     */
    protected abstract String extractModelName(String result);

    /**
     * Validates that the input path exists.
     *
     * @param inputPath the path to validate
     */
    protected void validateInputPath(Path inputPath) {
        if (!Files.exists(inputPath)) {
            throw new InvalidInputException("Input file does not exist: " + inputPath);
        }
    }

    /**
     * Parses JSON content to a Map.
     *
     * @param content the JSON content
     * @return the parsed Map
     */
    protected Map<String, Object> parseJson(String content) {
        try {
            return jsonMapper.readValue(content, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new InvalidInputException("Invalid JSON content: " + e.getMessage(), e);
        }
    }

    /**
     * Parses YAML content to a Map.
     *
     * @param content the YAML content
     * @return the parsed Map
     */
    protected Map<String, Object> parseYaml(String content) {
        try {
            return yamlMapper.readValue(content, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new InvalidInputException("Invalid YAML content: " + e.getMessage(), e);
        }
    }

    /**
     * Serializes a Map to JSON string.
     *
     * @param data the data to serialize
     * @return the JSON string
     */
    protected String toJson(Map<String, Object> data) {
        try {
            return jsonMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to serialize to JSON", e);
        }
    }

    /**
     * Serializes a Map to YAML string.
     *
     * @param data the data to serialize
     * @return the YAML string
     */
    protected String toYaml(Map<String, Object> data) {
        try {
            return yamlMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to serialize to YAML", e);
        }
    }
}
