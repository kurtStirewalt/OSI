# OSI Roadmap (Community-Informed)

This roadmap synthesizes community discussions and voting signals from the [OSI GitHub Discussions](https://github.com/open-semantic-interchange/OSI/discussions) board. It groups work into three categories:

- **Current Efforts / Working Groups** — strategic initiatives with active working groups driving spec evolution now
- **Future Efforts** — strategic initiatives planned for future working groups
- **Enhancements & Additions** — incremental improvements that extend the current model

---

## Current Efforts / Working Groups

These are the strategic initiatives where working groups are actively driving spec evolution.

---

### Metric Semantics & Computation Model

**Goal:** Enable expressive, composable, and well-defined semantic models.

**Motivation:**
The current model lacks sufficient support for metrics at different grains, filters, aggregation semantics, and relationships between metrics.

**Key Discussions:**

- [Top-level "metrics" vs. dataset-level "measures"](https://github.com/open-semantic-interchange/OSI/discussions/29)
- [Cumulative and other "expansions" to metrics](https://github.com/open-semantic-interchange/OSI/discussions/39)
- [Structured aggregation_method for Metrics](https://github.com/open-semantic-interchange/OSI/discussions/19)
- [Add "entity / grain" as a first-class concept](https://github.com/open-semantic-interchange/OSI/discussions/12)
- [Add explicit datasets reference to Metrics](https://github.com/open-semantic-interchange/OSI/discussions/18)

**Roadmap Deliverables:**

- Standard metrics specification language
- First-class aggregation, relationship, and grain semantics, including a specification that documents the expected behavior that the community has aligned on
- Support for derived and cumulative metrics

---

### Core Semantic Model (Entities, Relationships, Grain)

**Goal:** Strengthen OSI as a true semantic modeling layer.

**Motivation:**
Ambiguity in how entities, joins, and grain are represented limits interoperability.

**Key Discussions:**

- [Relationship Semantics](https://github.com/open-semantic-interchange/OSI/discussions/24)
- [Complex Relationship Definitions](https://github.com/open-semantic-interchange/OSI/discussions/4)
- [Make Relationship Cardinality Explicit](https://github.com/open-semantic-interchange/OSI/discussions/50)
- [Inner join in relationships](https://github.com/open-semantic-interchange/OSI/discussions/11)
- [Support for cross-dataset dimensions & single-dataset measures](https://github.com/open-semantic-interchange/OSI/discussions/27)

**Roadmap Deliverables:**

- Explicit entity modeling
- Enahnced relationship definitions & capabilities
- Cross-domain modeling support

---

### Catalog Integration & Semantic Services

**Goal:** Integrate OSI with data catalogs and enable centralized semantic services.

**Motivation:**
Semantic models need to be discoverable, governable, and shareable across systems.

**Roadmap Deliverables:**

- Integration patterns with catalogs (e.g., Polaris)
- Standalone semantic service / registry
- Discovery, versioning, and access control for OSI models

---

## Future Efforts

These strategic initiatives are planned for future working groups as the spec matures.

---

### Dataset Abstraction & Logical Modeling

**Goal:** Decouple semantic definitions from physical storage.

**Motivation:**
Users want reusable semantic models independent of underlying tables or views.

**Key Discussions:**

- [Add support for "Logical Datasets" (query-defined entities / view definitions)](https://github.com/open-semantic-interchange/OSI/discussions/49)
- [Support one-to-many binding between a logical dataset and the physical table, view, or query](https://github.com/open-semantic-interchange/OSI/discussions/61)
- [Structured Dataset Sources](https://github.com/open-semantic-interchange/OSI/discussions/23)

**Roadmap Deliverables:**

- Mapping layer between logical and physical datasets
- Reusable semantic definitions across environments

---

### Inbound Semantic Query Language (SQL++)

**Goal:** Define a standard query interface for interacting with OSI models.

**Motivation:**
Consumers (BI tools, AI systems, APIs) need a consistent way to query semantic models independent of underlying SQL dialects.

**Roadmap Deliverables:**

- Standard semantic query language (OSI-native or SQL-extended)
- Mapping from semantic queries → execution plans
- Support for metrics, dimensions, filters, and relationships

---

### SQL Dialect, Expressions, and Execution Boundaries

**Goal:** Clarify the role of SQL and execution within OSI.

**Motivation:**
There is tension between portability and practical execution requirements.

**Key Discussions:**

- [Add Default Dialect at Dataset Level](https://github.com/open-semantic-interchange/OSI/discussions/16)
- [Expectations around SQL expression dialects and conversion](https://github.com/open-semantic-interchange/OSI/discussions/28)
- [Use templating engine instead of plain yaml](https://github.com/open-semantic-interchange/OSI/discussions/62)
- [Jinja Templates](https://github.com/open-semantic-interchange/OSI/discussions/6)

**Roadmap Deliverables:**

- Explicit dialect handling strategy
- Clear boundaries between semantic definition and execution
- Optional templating support

---

### Dimensions, Hierarchies, and Time Semantics

**Goal:** Standardize how dimensions and time are modeled.

**Motivation:**
Inconsistent handling of hierarchies and time impacts usability and interoperability.

**Key Discussions:**

- [Dimension Hierarchies](https://github.com/open-semantic-interchange/OSI/discussions/21)
- [Dimension Groups](https://github.com/open-semantic-interchange/OSI/discussions/20)
- [Replace is_time with dimension_type Enum](https://github.com/open-semantic-interchange/OSI/discussions/17)
- [Universal calendar support](https://github.com/open-semantic-interchange/OSI/discussions/44)
- [Date Spine models](https://github.com/open-semantic-interchange/OSI/discussions/47)

**Roadmap Deliverables:**

- Hierarchical dimension modeling
- Standardized time semantics
- Calendar abstractions

---

### AI-Native Semantic Layer

**Goal:** Enable OSI as a reliable foundation for AI-driven analytics.

**Motivation:**
There is growing demand for structured semantic context and grounded query generation.

**Key Discussions:**

- [Do not prescribe "AI Context" as a key name](https://github.com/open-semantic-interchange/OSI/discussions/32)
- [Keyword for skipping context for AI](https://github.com/open-semantic-interchange/OSI/discussions/14)
- [Usage guidelines with samples especially for ai_context field](https://github.com/open-semantic-interchange/OSI/discussions/9)
- [Add verified_queries as a core element of the spec](https://github.com/open-semantic-interchange/OSI/discussions/82)

**Roadmap Deliverables:**

- Standardized AI context metadata
- Verified or curated query definitions
- Mechanisms for controlling AI exposure to semantic elements

---

### Governance, Identity, and Validation

**Goal:** Ensure trust, stability, and long-term interoperability.

**Motivation:**
Enterprise adoption requires consistent identifiers, validation, and governance hooks.

**Key Discussions:**

- [Make stable identifiers explicit rather than reusing name](https://github.com/open-semantic-interchange/OSI/discussions/31)
- [Metrics schema - Certified and Certifying Authority](https://github.com/open-semantic-interchange/OSI/discussions/53)
- [Governance metadata hooks](https://github.com/open-semantic-interchange/OSI/discussions/13)
- [Add more rigor to the spec using LinkML](https://github.com/open-semantic-interchange/OSI/discussions/67)
- [OSI-level validations?](https://github.com/open-semantic-interchange/OSI/discussions/35)

**Roadmap Deliverables:**

- Stable identifiers across environments
- Validation and conformance standards
- Governance and certification frameworks

---

### Industry / Domain-Specific Semantic Models

**Goal:** Accelerate adoption through reusable, standardized domain models.

**Motivation:**
Organizations repeatedly recreate similar semantic models (e.g., SaaS, finance, retail). Standardized models can drive faster adoption and consistency.

**Roadmap Deliverables:**

- Curated domain-specific semantic model templates
- Best-practice metric and dimension definitions by industry
- Interoperable model packages aligned with OSI

---

### Reference Engine Implementation (Compilation / SQL Generation)

**Goal:** Provide a canonical implementation for interpreting and executing OSI models.

**Motivation:**
A reference engine ensures consistent interpretation of the spec and accelerates ecosystem adoption.

**Roadmap Deliverables:**

- Reference compiler from OSI → SQL
- Canonical handling of joins, aggregations, and filters
- Test suite to validate conformance across implementations

---

## Enhancements & Additions (Incremental Improvements)

These items improve usability, clarity, and completeness without fundamentally changing the spec.

---

### Naming, Terminology, and UX Improvements

**Goal:** Align OSI vocabulary with how practitioners think about semantic models, and improve the authoring experience.

**Motivation:**
Several naming conventions in the current spec create confusion or clash with established industry terminology. Clearer naming reduces onboarding friction and improves readability of OSI definitions.

**Roadmap Deliverables:**

- Revised terminology that reflects community consensus (e.g., "Dimension" over "Field")
- Consistent naming conventions for source references, descriptions, and display labels

**Key Discussions:**

- [Rename Field to Dimension](https://github.com/open-semantic-interchange/OSI/discussions/33)
- [Rename Dataset.source to avoid conflation with where an entity was first defined](https://github.com/open-semantic-interchange/OSI/discussions/34)
- [Generalise description field](https://github.com/open-semantic-interchange/OSI/discussions/36)
- [Introduce a concept for "Display name"](https://github.com/open-semantic-interchange/OSI/discussions/37)

---

### Data Types and Field Semantics

**Goal:** Provide native support for rich data typing so downstream tools can interpret fields without guesswork.

**Motivation:**
Consuming systems (BI tools, AI agents, dashboards) frequently need to know whether a field represents a currency, a physical unit, or sensitive data — but this context is lost in the current spec and must be re-inferred or hard-coded per tool.

**Roadmap Deliverables:**

- First-class unit and currency annotations on measures and dimensions
- Standardized semantic field type taxonomy (dimension type, data type, PII classification)

**Key Discussions:**

- [Native support for units](https://github.com/open-semantic-interchange/OSI/discussions/42)
- [Native support for currencies](https://github.com/open-semantic-interchange/OSI/discussions/43)
- [Semantic Field Types: dimension_type, data_type, and pii_classification](https://github.com/open-semantic-interchange/OSI/discussions/55)

---

### Extended Metadata for OSI

**Goal:** Introduce a lightweight, optional metadata layer that improves how data is interpreted, presented, and consumed — without affecting execution semantics.

**Motivation:**
OSI standardizes structural and logical semantics well, but there is limited support for conveying interpretability context such as display conventions, default aggregation behavior, KPI polarity, sorting preferences, and alignment to external semantic concepts. These details are often redefined or inferred inconsistently across developers, BI tools, and AI systems.

**Roadmap Deliverables:**

- [Extended Metadata Proposal for OSI](https://github.com/open-semantic-interchange/OSI/issues/100) — optional, backward-compatible metadata fields (e.g., `measurement`, `display_format`, `semantic_type`, `default_aggregation`, `desired_direction`, `default_sort`, `semantic_mappings`)
- Richer application-specific extension points beyond `custom_extensions`
- Sample value annotations for documentation and AI grounding

**Key Discussions:**

- [Expand custom_extensions to be more suitable for application-specific metadata](https://github.com/open-semantic-interchange/OSI/discussions/30)
- [Sample values](https://github.com/open-semantic-interchange/OSI/discussions/7)
- [Governance metadata hooks](https://github.com/open-semantic-interchange/OSI/discussions/13) *(also informs strategic governance work)*

---

### Developer Experience & Documentation

**Goal:** Lower the barrier to adopting and correctly using OSI through better guidance, examples, and tooling-friendly formatting.

**Motivation:**
New adopters and tool authors need clearer documentation, real-world samples, and support for rich-text descriptions to effectively author and consume OSI models.

**Roadmap Deliverables:**

- Comprehensive usage guides with annotated examples, especially for AI context fields
- Data modeling best-practice documentation
- Markdown support in description fields for richer inline documentation

**Key Discussions:**

- [Usage guidelines with samples especially for ai_context field](https://github.com/open-semantic-interchange/OSI/discussions/9)
- [Information about data modelling](https://github.com/open-semantic-interchange/OSI/discussions/8)
- [Markdown support](https://github.com/open-semantic-interchange/OSI/discussions/38)

---

### Additional Modeling Constructs

**Goal:** Expand OSI's expressiveness to cover common modeling patterns that are currently unsupported.

**Motivation:**
Practitioners frequently need to model dimension groupings, audience segments, and reusable filter definitions. Without native support, these patterns are implemented inconsistently across tools.

**Roadmap Deliverables:**

- Dimension grouping and organization primitives
- Audience / segment definitions as first-class constructs
- Reusable semantic filter definitions

**Key Discussions:**

- [Dimension Groups](https://github.com/open-semantic-interchange/OSI/discussions/20)
- [Add Support for Audiences](https://github.com/open-semantic-interchange/OSI/discussions/51)
- [Semantic Filters](https://github.com/open-semantic-interchange/OSI/discussions/5)

---

### Specialized Capabilities

**Goal:** Extend OSI to support domain-specific data types and patterns that go beyond traditional tabular analytics.

**Motivation:**
Geospatial analytics and time-series modeling have unique requirements (spatial types, geographic hierarchies, date spines) that benefit from first-class spec support rather than ad-hoc workarounds.

**Roadmap Deliverables:**

- Spatial field types, spatial relationships, and geographic hierarchies
- Date spine model support for time-series alignment and gap-filling

**Key Discussions:**

- [Geospatial data support: spatial field types, spatial relationships, and geographic hierarchies](https://github.com/open-semantic-interchange/OSI/discussions/69)
- [Date Spine models](https://github.com/open-semantic-interchange/OSI/discussions/47)

---

### Tooling & Ecosystem Support

**Goal:** Provide reference tooling that makes it easy to validate, convert, and adopt OSI models.

**Motivation:**
Broad ecosystem adoption depends on practical tools that let teams validate their models against the spec and convert between OSI and existing vendor formats without manual effort.

**Roadmap Deliverables:**

- Validator code (schema validation, linting, conformance checks)
- Participant ↔ OSI converter code (read/write interoperability with existing tools)

