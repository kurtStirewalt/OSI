# OSI to Snowflake Converter

Converts OSI YAML semantic models to [Snowflake Cortex Analyst](https://docs.snowflake.com/en/user-guide/snowflake-cortex/cortex-analyst) semantic model YAML. Pure offline conversion — no Snowflake connection required.

> **Note:** This converter is under active development. It handles common cases well but has not been thoroughly tested against all edge cases — use with caution in production.

## Setup

```bash
pip3 install -r requirements.txt
```

## Usage

```bash
python3 osi_to_snowflake_yaml_converter.py -i input.yaml -o output.yaml
```

## Limitations

Some OSI concepts (e.g., `ai_context` on relationships) do not have a native counterpart in the Snowflake semantic model. These are dropped during conversion and the converter will emit warnings so you know what was left behind.
