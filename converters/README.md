# OSI Converters

Standalone converters that transform [OSI](../README.md) (Open Semantic Interchange) semantic models into vendor-specific formats. Each converter is a pure offline tool — no external connections required.

## Structure

Each subdirectory contains a self-contained converter for a specific target platform, with its own setup instructions, dependencies, and usage documentation. See the README in each converter's directory for details.

## Contributing a Converter

A converter directory should include:

- A **README** with setup and usage instructions
- Any **dependency/build files** needed to run the converter (e.g., `requirements.txt`, `pom.xml`)
- The converter source code itself
