# OSI Converters

Standalone converters that transform OSI (Open Semantic Interchange) YAML semantic models into vendor-specific formats. Pure offline tools — no connections required.

## Setup

```bash
pip3 install -r requirements.txt
```

## Converters

### Snowflake (Cortex Analyst)

> **Note:** This converter is under active development. It handles common cases well but has not been thoroughly tested against all edge cases — use with caution in production.

Converts OSI YAML to Snowflake Cortex Analyst semantic model YAML.

```bash
python3 osi_to_snowflake_yaml_converter.py -i input.yaml -o output.yaml
```

Some OSI concepts (e.g., `ai_context` on relationships) do not have a native counterpart in the Snowflake semantic model. These are dropped during conversion and the converter will emit warnings so you know what was left behind.
