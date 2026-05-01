package org.osi.converter;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for converting between data formats (YAML and JSON).
 *
 * <p>This is the external API for conversion. Implementations handle the conversion
 * of data from one format to another, with support for property mapping.</p>
 *
 */
public interface Converter {

    /**
     * Converts the input file and writes results to the specified output directory.
     *
     * <p>Each semantic model is written to a separate file named after its apiName.
     * Example: "Sales_Model.json", "Marketing_Model.json"
     *
     * @param inputPath the path to the input file
     * @param outputDir the directory where output files will be written
     */
    void convert(Path inputPath, Path outputDir);

    /**
     * Converts string content from the source format to the target format.
     *
     * <p>For OSI to Salesforce: returns one Salesforce model per OSI semantic_model entry.
     * <p>For Salesforce to OSI: returns one OSI document with one semantic_model entry.
     *
     * @param content the content to convert
     * @return list of converted content strings (one per semantic model)
     */
    List<String> convert(String content);
}
