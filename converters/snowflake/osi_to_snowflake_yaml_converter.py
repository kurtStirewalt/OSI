"""
Converts an OSI (Open Semantic Interchange) YAML semantic model to a Snowflake
Cortex Analyst semantic model YAML. Pure offline conversion — no Snowflake
connection required.

Usage:
    python3 osi_to_snowflake_yaml_converter.py -i input.yaml -o output.yaml
"""

import argparse
import sys
import warnings

import yaml


DEFAULT_SUPPORTED_VERSIONS = {"0.1.0", "0.1.1"}


class OsiConversionError(Exception):
    """Raised when an OSI YAML cannot be converted to Snowflake format."""


def _unwrap_semantic_model(root):
    """Handles both wrapped and flattened OSI YAML formats.

    Wrapped format has ``version`` at root and model properties inside
    ``semantic_model: [...]``. Flattened format has everything at root.
    Returns a single dict with version + all model properties merged.
    """
    semantic_model = root.get("semantic_model")
    if semantic_model is None:
        # Flattened format — everything at root
        return root

    # Wrapped format — extract first model from the list
    if not isinstance(semantic_model, list) or len(semantic_model) == 0:
        raise OsiConversionError(
            "Invalid OSI YAML: 'semantic_model' must be a non-empty list"
        )

    model = semantic_model[0]
    if not isinstance(model, dict):
        raise OsiConversionError(
            "Invalid OSI YAML: 'semantic_model' entries must be mappings"
        )

    # Merge root-level version into the model dict (version lives outside
    # the semantic_model wrapper in the OSI spec)
    version = root.get("version")
    if version is not None and "version" not in model:
        model["version"] = version

    return model


def convert_osi_to_snowflake(osi_yaml_str, supported_versions=None):
    """Top-level entry point. Parses OSI YAML, validates, converts, returns
    Snowflake YAML string.

    Accepts both:
    - Wrapped format: ``version`` at root, model inside ``semantic_model: [...]``
    - Flattened format: ``version``, ``name``, ``datasets``, etc. all at root

    Args:
        osi_yaml_str: OSI YAML as a string.
        supported_versions: Set of supported OSI version strings.
            Defaults to {"0.1.0", "0.1.1"}.

    Returns:
        Snowflake Cortex Analyst semantic model YAML string.

    Raises:
        OsiConversionError: If the input cannot be converted.
    """
    if supported_versions is None:
        supported_versions = DEFAULT_SUPPORTED_VERSIONS

    root = yaml.safe_load(osi_yaml_str)
    if not isinstance(root, dict):
        raise OsiConversionError("Invalid OSI YAML: expected a mapping at the root")

    # Handle wrapped format: version at root, model inside semantic_model list
    osi = _unwrap_semantic_model(root)

    version = osi.get("version")
    if version is None:
        warnings.warn(
            "OSI YAML is missing a 'version' field; version validation skipped",
            stacklevel=2,
        )
    else:
        version_str = str(version)
        if version_str not in supported_versions:
            raise OsiConversionError(
                f"Unsupported OSI specification version '{version_str}'. "
                f"Supported: {sorted(supported_versions)}"
            )

    snowflake_model = _convert_model(osi)

    return yaml.dump(
        snowflake_model,
        default_flow_style=False,
        sort_keys=False,
        allow_unicode=True,
    )


def _convert_model(osi):
    """Converts the root OSI model dict to a Snowflake semantic model dict."""
    name = osi.get("name")
    if not name:
        raise OsiConversionError("Missing required 'name' field in semantic model")

    result = {}
    result["name"] = name

    description = osi.get("description")
    if description:
        result["description"] = description

    # datasets -> tables
    datasets = osi.get("datasets")
    if datasets:
        tables = [_convert_dataset(ds) for ds in datasets]
        if tables:
            result["tables"] = tables

    # relationships
    relationships = osi.get("relationships")
    if relationships:
        converted_rels = [_convert_relationship(rel) for rel in relationships]
        if converted_rels:
            result["relationships"] = converted_rels

    # metrics
    metrics = osi.get("metrics")
    if metrics:
        converted_metrics = [m for m in (_convert_metric(m) for m in metrics) if m is not None]
        if converted_metrics:
            result["metrics"] = converted_metrics

    # Warn about dropped OSI-only fields (model-level synonyms have no Snowflake
    # counterpart at the root, so treat the entire ai_context as dropped)
    _warn_dropped_fields(osi, "model", synonyms_extracted=False)

    return result


def _convert_dataset(dataset):
    """Converts an OSI dataset dict to a Snowflake table dict."""
    result = {}
    name = dataset.get("name")
    if not name:
        raise OsiConversionError("Missing required 'name' field in dataset")
    result["name"] = name

    # source -> base_table
    source = dataset.get("source")
    base_table = _parse_source(source)
    if base_table is not None:
        result["base_table"] = base_table

    # primary_key: [col,...] -> primary_key: {columns: [col,...]}
    pk = dataset.get("primary_key")
    if pk:
        result["primary_key"] = {"columns": pk}

    # unique_keys: [[...], ...] -> unique_keys: [{columns: [...]}, ...]
    uks = dataset.get("unique_keys")
    if uks:
        result["unique_keys"] = [{"columns": uk} for uk in uks]

    description = dataset.get("description")
    if description:
        result["description"] = description

    # Extract synonyms from ai_context
    synonyms = _extract_synonyms(dataset.get("ai_context"))
    if synonyms:
        result["synonyms"] = synonyms

    # Classify fields into dimensions, time_dimensions, facts
    fields = dataset.get("fields")
    if fields:
        dimensions = []
        time_dimensions = []
        facts = []

        for field in fields:
            classification = _classify_field(field)
            converted = _convert_field(field)
            if converted is None:
                continue
            if classification == "time_dimension":
                time_dimensions.append(converted)
            elif classification == "dimension":
                dimensions.append(converted)
            else:
                facts.append(converted)

        if dimensions:
            result["dimensions"] = dimensions
        if time_dimensions:
            result["time_dimensions"] = time_dimensions
        if facts:
            result["facts"] = facts

    # Warn about dropped OSI-only fields
    _warn_dropped_fields(dataset, f"dataset '{name}'")

    return result


def _classify_field(field):
    """Returns 'dimension', 'time_dimension', or 'fact' based on field structure."""
    dimension = field.get("dimension")
    if dimension is None:
        return "fact"
    if isinstance(dimension, dict) and dimension.get("is_time") is True:
        return "time_dimension"
    return "dimension"


def _convert_field(field):
    """Converts an OSI field dict to a Snowflake dimension/time_dimension/fact entry."""
    field_name = field.get("name")
    if not field_name:
        raise OsiConversionError("Missing required 'name' field in field definition")

    expr_str = _extract_expression(field.get("expression"), field_name)
    if expr_str is None:
        return None

    result = {}
    result["name"] = field_name
    result["expr"] = expr_str

    description = field.get("description")
    if description:
        result["description"] = description

    # Extract synonyms from ai_context (synonyms map to a native Snowflake field)
    synonyms = _extract_synonyms(field.get("ai_context"))
    if synonyms:
        result["synonyms"] = synonyms

    # Warn about dropped OSI-only fields
    _warn_dropped_fields(field, f"field '{field_name}'")

    return result


def _convert_metric(metric):
    """Converts an OSI metric dict to a Snowflake model-level metric dict."""
    metric_name = metric.get("name")
    if not metric_name:
        raise OsiConversionError("Missing required 'name' field in metric")

    expr_str = _extract_expression(metric.get("expression"), metric_name)
    if expr_str is None:
        return None

    result = {}
    result["name"] = metric_name
    result["expr"] = expr_str

    description = metric.get("description")
    if description:
        result["description"] = description

    # Extract synonyms from ai_context (synonyms map to a native Snowflake field)
    synonyms = _extract_synonyms(metric.get("ai_context"))
    if synonyms:
        result["synonyms"] = synonyms

    # Warn about dropped OSI-only fields
    _warn_dropped_fields(metric, f"metric '{metric_name}'")

    return result


def _convert_relationship(rel):
    """Converts an OSI relationship dict to a Snowflake relationship dict."""
    result = {}
    rel_name = rel.get("name")
    if not rel_name:
        raise OsiConversionError("Missing required 'name' field in relationship")
    result["name"] = rel_name

    left_table = rel.get("from")
    if not left_table:
        raise OsiConversionError(
            f"Relationship '{rel_name}': missing required 'from' field"
        )
    right_table = rel.get("to")
    if not right_table:
        raise OsiConversionError(
            f"Relationship '{rel_name}': missing required 'to' field"
        )
    result["left_table"] = left_table
    result["right_table"] = right_table

    from_cols = rel.get("from_columns", [])
    to_cols = rel.get("to_columns", [])

    if len(from_cols) != len(to_cols):
        raise OsiConversionError(
            f"Relationship '{rel_name}': from_columns and to_columns must have the "
            f"same length (got {len(from_cols)} and {len(to_cols)})"
        )

    relationship_columns = []
    for i in range(len(from_cols)):
        relationship_columns.append(
            {"left_column": from_cols[i], "right_column": to_cols[i]}
        )
    if relationship_columns:
        result["relationship_columns"] = relationship_columns

    # Warn about dropped OSI-only fields (relationships have no native synonyms)
    _warn_dropped_fields(rel, f"relationship '{rel_name}'", synonyms_extracted=False)

    return result


def _extract_expression(expression, field_name):
    """Selects the best dialect expression for Snowflake.

    Returns the expression string, or None if the field should be skipped
    (unsupported dialect only). Raises OsiConversionError if expression
    is missing entirely.
    """
    if expression is None:
        raise OsiConversionError(
            f"Missing expression for field/metric '{field_name}'"
        )

    dialects = expression.get("dialects")
    if not dialects:
        raise OsiConversionError(
            f"Missing expression for field/metric '{field_name}'"
        )

    snowflake_expr = None
    ansi_expr = None

    for d in dialects:
        dialect_name = (d.get("dialect") or "").upper()
        if dialect_name == "SNOWFLAKE":
            snowflake_expr = d.get("expression")
        elif dialect_name == "ANSI_SQL":
            ansi_expr = d.get("expression")

    if snowflake_expr:
        return snowflake_expr
    if ansi_expr:
        return ansi_expr

    raise OsiConversionError(
        f"Field/metric '{field_name}' has no Snowflake-compatible expression "
        f"(requires SNOWFLAKE or ANSI_SQL dialect)"
    )


def _parse_source(source):
    """Parses an OSI dataset source string into a Snowflake base_table dict.

    Returns None if source is empty/None. Returns {"definition": source} for
    subqueries. Otherwise splits into 3-part db.schema.table.
    """
    if not source:
        return None

    source_stripped = str(source).strip()
    if not source_stripped:
        return None

    # Detect subqueries — require whitespace after the keyword to avoid false
    # positives on table names like WITH_TABLE or SELECT_RESULTS.
    upper = source_stripped.upper()
    if upper.startswith(("SELECT ", "SELECT\n", "SELECT\t",
                          "WITH ", "WITH\n", "WITH\t")):
        return {"definition": source_stripped}

    # TODO: Quoted identifiers (e.g., "my.db"."my schema"."my table") are not
    # handled. Basic dot-splitting only.
    parts = source_stripped.split(".")
    if len(parts) == 3:
        return {
            "database": parts[0].upper(),
            "schema": parts[1].upper(),
            "table": parts[2].upper(),
        }

    raise OsiConversionError(
        f"Source '{source}' must be a fully qualified db.schema.table or a subquery"
    )


def _extract_synonyms(ai_context):
    """Extracts synonyms list from a structured ai_context object.

    If ai_context is a dict with a 'synonyms' key, returns the synonyms list.
    If ai_context is a plain string or None, returns None.
    """
    if isinstance(ai_context, dict):
        synonyms = ai_context.get("synonyms")
        if isinstance(synonyms, list) and synonyms:
            return list(synonyms)
    return None


def _warn_dropped_fields(source, context, synonyms_extracted=True):
    """Warns about OSI fields that have no Snowflake counterpart and are dropped.

    Args:
        source: The OSI dict being converted.
        context: Human-readable description (e.g., "field 'col1'").
        synonyms_extracted: If True, ai_context.synonyms are mapped to
            Snowflake's native synonyms field, so only non-synonym ai_context
            keys are reported as dropped. If False (e.g., for relationships),
            the entire ai_context is dropped.
    """
    dropped = []

    ai_context = source.get("ai_context")
    if ai_context:
        if synonyms_extracted and isinstance(ai_context, dict):
            non_synonym_keys = [k for k in ai_context if k != "synonyms"]
            if non_synonym_keys:
                dropped.append(f"ai_context ({', '.join(non_synonym_keys)})")
        else:
            dropped.append("ai_context")

    if source.get("custom_extensions"):
        dropped.append("custom_extensions")

    if source.get("version"):
        dropped.append("version")

    if source.get("label"):
        dropped.append("label")

    if dropped:
        warnings.warn(
            f"Dropped from {context} (no Snowflake counterpart): "
            + ", ".join(dropped),
            stacklevel=3,
        )


def main():
    parser = argparse.ArgumentParser(
        description="Convert OSI YAML semantic model to Snowflake Cortex Analyst YAML"
    )
    parser.add_argument(
        "-i", "--input", required=True, help="Path to the OSI YAML input file"
    )
    parser.add_argument(
        "-o", "--output", required=True, help="Path to write the Snowflake YAML output"
    )
    args = parser.parse_args()

    with open(args.input, "r") as f:
        osi_yaml_str = f.read()

    try:
        snowflake_yaml_str = convert_osi_to_snowflake(osi_yaml_str)
    except OsiConversionError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

    with open(args.output, "w") as f:
        f.write(snowflake_yaml_str)

    print(f"Converted {args.input} -> {args.output}")


if __name__ == "__main__":
    main()
