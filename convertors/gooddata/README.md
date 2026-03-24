# GoodData OSI Convertor

Bidirectional convertor between GoodData's declarative Logical Data Model (LDM)
and the [OSI (Open Semantic Interchange)](https://github.com/open-semantic-interchange/OSI)
semantic model specification.

## Features

- **GoodData → OSI**: Convert a GoodData declarative LDM JSON to OSI semantic model YAML
- **OSI → GoodData**: Convert an OSI semantic model YAML to GoodData declarative LDM JSON
- Preserves GoodData-specific metadata (labels, date granularities, geo types) via OSI custom_extensions
- Generates dual-dialect expressions (ANSI_SQL + MAQL) for fields

## Usage

```python
import json
import yaml
from gooddata_osi import gooddata_to_osi, osi_to_gooddata
from gooddata_osi.models import gd_model_from_dict, gd_model_to_dict

# GoodData → OSI
with open("gooddata_ldm.json") as f:
    gd_model = gd_model_from_dict(json.load(f))
osi_model = gooddata_to_osi(gd_model, model_name="my_model")
with open("osi_model.yaml", "w") as f:
    yaml.dump(osi_model, f, default_flow_style=False)

# OSI → GoodData
with open("osi_model.yaml") as f:
    osi_data = yaml.safe_load(f)
gd_model = osi_to_gooddata(osi_data, data_source_id="my_datasource")
with open("gooddata_ldm.json", "w") as f:
    json.dump(gd_model_to_dict(gd_model), f, indent=2)
```

## Development

```bash
uv sync --group dev
uv run pytest
```

## Concept Mapping

| GoodData LDM | OSI Semantic Model |
|---|---|
| Dataset | Dataset |
| Attribute (+ Labels) | Field with `dimension` metadata |
| Fact | Field without `dimension` metadata |
| Reference (FK) | Relationship |
| Date Instance | Dataset with `GOODDATA` custom_extension (`date_dimension: true`) |
| MAQL expression | Dialect entry (`dialect: MAQL`) |

## Limitations

- **Metrics are not converted.** GoodData metrics use MAQL, a context-aware metric language
  where dimensionality and filters are applied at report time. The current OSI metric model
  is SQL-expression-based and cannot represent this paradigm.
- AggregatedFacts are not yet supported.
- Workspace data filters are not mapped.
