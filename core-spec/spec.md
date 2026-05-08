# OSI - Core Metadata Specification

**Version:** 0.1.1

## Goals

- **Standardization**: Establish uniform language and structure for semantic model definitions, ensuring consistency and ease of interpretation across various tools and systems.
- **Extensibility**: Support domain-specific extensions while maintaining core compatibility.
- **Interoperability**: Enable exchange and reuse across different AI and BI applications.

## Table of Contents

1. [Enumerations](#enumerations)
2. [Semantic Model](#semantic-model)
3. [Datasets](#datasets)
4. [Join paths](#join-paths)
5. [Fields](#fields)
6. [Metrics](#metrics)
7. [Concepts](#concepts)
8. [Mappings](#mappings)
9. [Complete Example](#complete-example)

---

## Enumerations

Standard enumeration values used throughout the specification.

### Dialects

Supported SQL and expression language dialects for metrics and field definitions.

| Dialect | Description |
|---------|-------------|
| `ANSI_SQL` | Standard SQL dialect |
| `SNOWFLAKE` | Snowflake SQL |
| `MDX` | Multi-Dimensional Expressions |
| `TABLEAU` | Tableau calculations |
| `DATABRICKS` | Databricks SQL |
| `MAQL` | GoodData MAQL (Metric Analysis and Query Language) |

### Multiplicities

The allowable multiplicities of relationships defined in the [Concepts](#concepts) section.

| Multiplicity | Description |
|---------|-------------|
| `ManyToOne` | The last role of a relationship is uniquely determined by the other roles |
| `OneToOne` | The relationship is ManyToMany in both directions (only for binary relationships) |

### Vendors

Supported vendors for custom extensions and integrations.

| Vendor | Description |
|--------|-------------|
| `COMMON` | Common/standard extensions |
| `SNOWFLAKE` | Snowflake-specific attributes |
| `SALESFORCE` | Salesforce/Tableau-specific attributes |
| `DBT` | dbt-specific attributes |
| `DATABRICKS` | Databricks-specific attributes |
| `GOODDATA` | GoodData-specific attributes |

## Semantic Model

The top-level container that represents a complete semantic model, including datasets, join paths, and  metrics.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the semantic model |
| `description` | string | No | Human-readable description |
| `ai_context` | string/object | No | Additional context for AI tools (e.g., custom instructions) |
| `datasets` | array | Yes | Collection of logical datasets (fact and dimension tables) |
| `join_paths` | array | No | Defines how logical datasets are connected |
| `metrics` | array | No | Quantifiable measures defined as aggregate expessions on fields from logical datsets |
| `custom_extensions` | array | No | Vendor-specific attributes for extensibility |

### Example

```yaml
semantic_model:
  - name: sales_analytics
    description: Sales and customer analytics model
    ai_context:
      instructions: "Use this model for sales analysis and customer insights"
    datasets: []
    join_paths: []
    metrics: []
    custom_extensions:
      - vendor_name: DBT
        data: '{"project_name": "tpcds_analytics", "models_path": "models/semantic"}'
```

---

## Datasets

Logical datasets represent business entities or concepts (fact and dimension tables). They contain fields and define the structure of the data.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the dataset |
| `source` | string | Yes | Reference to underlying physical table/view (e.g., `database.schema.table`) or query |
| `primary_key` | array | No | Primary key columns that uniquely identify rows (single or composite) |
| `unique_keys` | array of arrays | No | Array of unique key definitions (each can be single or composite) |
| `description` | string | No | Human-readable description |
| `ai_context` | string/object | No | Additional context for AI tools (e.g., synonyms, common terms) |
| `fields` | array | No | Row-level attributes for grouping, filtering, and metric expressions |
| `custom_extensions` | array | No | Vendor-specific attributes |

### Primary Key Examples

```yaml
# Simple primary key
primary_key: [customer_id]

# Composite primary key
primary_key: [order_id, line_number]
```

### Unique Keys Examples

```yaml
# Multiple unique keys (each can be simple or composite)
unique_keys:
  - [email]                    # Simple unique key
  - [first_name, last_name]    # Composite unique key
```

### Example

```yaml
datasets:
  - name: orders
    source: sales.public.orders
    primary_key: [order_id]
    unique_keys:
      - [order_id]
      - [order_number]
    description: Order transactions
    ai_context:
      synonyms:
        - "purchases"
        - "sales"
    fields: []
    custom_extensions:
      - vendor_name: DBT
        data: '{"materialized": "table"}'
```

---

## Join paths

Join paths define how logical datasets are connected through foreign key constraints. They support both simple and composite keys.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the join path |
| `from` | string | Yes | The logical dataset on the many side of the join path |
| `to` | string | Yes | The logical dataset on the one side of the join path |
| `from_columns` | array | Yes | Array of column names in the "from" dataset (foreign key columns) |
| `to_columns` | array | Yes | Array of column names in the "to" dataset (primary or unique key columns) |
| `ai_context` | string/object | No | Additional context for AI tools |
| `custom_extensions` | array | No | Vendor-specific attributes |

### Important Notes

- The order of columns in `from_columns` must correspond to the order in `to_columns`
- Both arrays must have the same number of columns
- For simple join paths, use a single column: `[column1]`
- For composite join paths, use multiple columns: `[column1, column2]`

### Examples

**Simple Join Path:**

```yaml
- name: orders_to_customers
  from: orders
  to: customers
  from_columns: [customer_id]
  to_columns: [id]
```

**Composite Join Path:**

```yaml
# order_lines.product_id = products.id AND order_lines.variant_id = products.variant_id
- name: order_lines_to_products
  from: order_lines
  to: products
  from_columns: [product_id, variant_id]
  to_columns: [id, variant_id]
```

---

## Fields

Fields represent row-level attributes that can be used for grouping, filtering, and in metric expressions. They can be simple column references or computed expressions.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the field within the dataset |
| `expression` | object | Yes | Expression definition with dialect support |
| `dimension` | object | No | Dimension metadata (e.g., `is_time` flag) |
| `label` | string | No | Label for categorization |
| `description` | string | No | Human-readable description |
| `ai_context` | string/object | No | Additional context for AI tools (e.g., synonyms) |
| `custom_extensions` | array | No | Vendor-specific attributes |

### Expression Object

The expression object supports multiple SQL dialects for cross-platform compatibility. Each field can define expressions in different dialects.

**Structure:**

```yaml
expression:
  dialects:
    - dialect: ANSI_SQL  # Must be one of the dialects enum values
      expression: "customer_id"  # Scalar SQL expression
```

**Key Points:**

- Use scalar SQL expressions (no aggregations)
- Can be simple column references (e.g., `customer_id`) or computed expressions (e.g., `first_name || ' ' || last_name`)
- Multiple dialect versions can be provided for the same field

### Dimension Object

| Field | Type | Description |
|-------|------|-------------|
| `is_time` | boolean | Indicates if this is a time-based dimension for temporal filtering |

### Examples

**Simple Column Reference for a Dimension:**

```yaml
- name: customer_id
  expression:
    dialects:
      - dialect: ANSI_SQL
        expression: customer_id
  description: Customer identifier
  dimension: 
    is_time: false
```

**Computed Field:**

```yaml
- name: full_name
  expression:
    dialects:
      - dialect: ANSI_SQL
        expression: first_name || ' ' || last_name
  description: Customer full name
  ai_context:
    synonyms:
      - "name"
      - "customer name"
```

**Time Dimension:**

```yaml
- name: order_date
  expression:
    dialects:
      - dialect: ANSI_SQL
        expression: order_date
  dimension:
    is_time: true
  description: Date when order was placed
  ai_context:
    synonyms:
      - "purchase date"
      - "transaction date"
```

**Multi-Dialect Field:**

```yaml
- name: email_normalized
  expression:
    dialects:
      - dialect: ANSI_SQL
        expression: LOWER(email)
      - dialect: SNOWFLAKE
        expression: LOWER(email)::VARCHAR
  description: Normalized email address
```

---

## Metrics

Quantitative measures defined on business data, representing key calculations like sums, averages, ratios, etc. Metrics are defined at the semantic model level and can  span multiple datasets.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the metric |
| `expression` | object | Yes | Expression definition with dialect support |
| `description` | string | No | Human-readable description of what the metric measures |
| `ai_context` | string/object | No | Additional context for AI tools (e.g., synonyms) |
| `custom_extensions` | array | No | Vendor-specific attributes |

### Expression Object

The expression object supports multiple dialects

```yaml
expression:
  dialects:
  - dialect: ANSI_SQL  # Default
    expression: "SUM(order.sales) / COUNT(DISTINCT order.customer_id)"
```

### Examples

**Simple Aggregation:**

```yaml
- name: total_revenue
  expression:
    - dialect: ANSI_SQL
      expression: SUM(orders.amount)
  description: Total revenue across all orders
  ai_context:
    synonyms:
      - "total sales"
      - "revenue"
```

**Cross-Dataset Metric:**

```yaml
- name: avg_orders
  expression:
    - dialect: ANSI_SQL
      expression: SUM(orders.amount) / COUNT(DISTINCT customers.id)
  description: Average orders
  ai_context:
    synonyms:
      - "Order Average by customer"
```

---

## Custom Extensions

Custom extensions allow vendors to add platform-specific metadata without breaking core compatibility. Each extension includes a vendor name and arbitrary JSON data.

### Schema

```yaml
custom_extensions:
  - vendor_name: string  # Must be from vendors enum
    data: string         # JSON string containing vendor-specific data
```

### Examples

**Snowflake Extension:**

```yaml
- vendor_name: SNOWFLAKE
  data: '{
    "warehouse": "ANALYTICS_WH",
    "database": "PROD",
    "schema": "PUBLIC"
  }'
```

**Salesforce Extension:**

```yaml
- vendor_name: SALESFORCE
  data: '{
    "tableau_workbook_id": "sales_dashboard",
    "einstein_enabled": true,
    "crm_sync": {
      "enabled": true,
      "sync_frequency": "daily"
    }
  }'
```

**DBT Extension:**

```yaml
- vendor_name: DBT
  data: '{
    "project_name": "analytics",
    "materialized": "table",
    "tags": ["daily", "core"]
  }'
```

**Databricks Extension:**

```yaml
- vendor_name: Databricks
  data: '{
    "default_catalog": "finance",
    "default_schema": "gold"
  }'
```

---

## Concepts

Concepts represent the types of things that have meaning in a business setting, e.g., person, company, or
salary. Each concept is either an entity type or a value type. Every ontology starts with a value-type
concept for each basic data type, like `Integer`, `Decimal`, and `String`, and an entity-type concept
called `Any`. Every other concept in the ontology extends one of these starter concepts.

### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for this concept |
| `description` | string | No | Human-readable description |
| `relationships` | object | No | Relationships where this concept plays the first role |
| `extends` | list | No | Names of this concept's supertypes |
| `derived_by` | list | No | Expressions for deriving this concept's population |
| `identify_by` | list | No | Names of relationships that collectively identify this concept |
| `requires` | list | No | Expressions that constrain this concept's population |

### Extends

Every concept that a user declares extends one or more concepts in the ontology. The new concept
is a sutype of each concept that it extends, and the extended concepts are its supertypes. For instance,`SocialSecurityNr` extends `Integer`, `Person` extends `Any`, and `Employee` extends `Person`. Any
concept that directly or indirectly extends a value type like `Integer` or `String` is a value type.
A concept is an entity type if it is not a value type. Any concept with an empty extends list defaults
to being a subtype of `Any` and so is an entity type.

### Relationships

Relationships relate objects of one or more concepts and declare how to verbalize links among those objects.
For instance, a relationship named `earns` links persons to salaries. Each link pairs some `Person` with
some `Salary` and verbalizes as, “Person earns Salary annually.” Relationships have set (as opposed to bag)
semantics, and links do not contain nulls.

#### Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Part of the identifier for this relationship |
| `description` | string | No | Human-readable description |
| `multiplicity` | enum | No | Multiplicity constraint |
| `roles` | list | No | List of additional roles in this relationship |
| `derived_by` | list | No | Expressions that derive links of this relationship |
| `requires` | list | No | Expressions that constrain this relationship's population |
| `verbalization` | list | Yes | Patterns describing how to verbalize links |

Each relationship is uniquely identified by a prepending its declared name with that of the containing
concept. For instance, a relationship named `earns`, declared within the `Person` concept is identified
by the string `Person.earns`. This convention naturally supports expressions that navigate over the links
of relationships using the “dot-join” operator in a manner that is familiar to object-oriented programming
languages.

#### Multiplicities

In a relationship that comprises more than one role, the last role might be functionally dermined
by the other roles. This is declared by providing a ManyToOne multiplicity on that relationship.
For relationships of ternary and higher arity, the multiplicity applies to the n-th role, meaning
the object that plays the n-th role is functionally determined by the tuple of objects that play
the first n-1 roles. A relationship like `Item.total_sales_in`, which records the total sales amount
of an item at a given store, will have a ManyToOne multiplicity to indicate that for any pair
of `Item` and `Store` at most one `Amount` will be recorded as the total sales amount for that
`Item` and that `Store`.

In the special case of a binary relationship, one might declare a OneToOne multiplicity, which
indicates the relationship is ManyToOne in both directions. For instance, the `Person.ssn`
relationship will have a OneToOne multiplicity because each person is assigned at most one social
security number and each social security number is assigned to at most one person.

In the absence of any multiplicity, we make no assumptions of functional dependencies among
any of the roles.

#### Roles

Objects play roles in the links of a relationship. If you think of a relationship as a narrow table,
then links are its rows and roles are its columns. Each role is played by some concept, which
indicates the type of objects that can play that role in the relationship's links. In the
`Person.earns` relationship, `Person` and `Salary` play the first and second roles respectively.

By convention, the first role of any relationship will be played by the concept under which the
relationship is declared. Any additional roles are declared in the roles field as follows:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `player` | string | Yes | Name of the concept that plays this role |
| `name` | string | No | Optional role name |

A unary relationship like `Person.files_married_joint` declares no additional roles, 
while a ternary relationship like `Person.purchased_on` declares two additional roles
played by `Vehicle` and `Date` respectively.

The role player often suffices to distinguish the role within a given relationship.
However, the same concept can play multiple roles, such as in the ternary relationship
`Store.ships_to_in_days` that connects pairs of `Store` objects to the number of days
required to ship inventory from one store to another. When this happens, the user must
declare a distinguising name for any additional role whose player does not suffice to
distinguish it from other roles in the same relationship.

### Identifying relationships

Many conceptual models distinguish one or more relationships that should be used when
referecing entity-type objects in expressions and queries. For instance, the relationship
`Person.ssn` can be used to reference a person by their social security number; while
the pair of relationships `License.acct` and `License.seat_nr` can be used to reference
a license by the account and seat number. These relationships are always binary, and
their first roles are always played by the concept the relationship is used to reference.
When a set of identifying relationships is known for a concept, this knowledge can be
expressed using the identify_by list.

### Derivation expressions

Concepts and relationships may be derived using expressions. A derived relationship is one
whose links are derived from those of other relationships. For instance:

```yaml
- name: Person
  relationships:
    parent_of:
      roles:
        - player: Person
          name: "child"
      verbalizes: [ "{Person} is a parent of {Person:child}", "{Person:child} is a child of {Person}" ]
    ancestor_of:
      roles:
        - player: Person
          name: "descendant"
      derived_by:
        - "Person.parent_of(descendant)"
          "Person.ancestor_of.parent_of(descendant)"  
    taxed_at:
      roles:
        - player: TaxRate
      derived_by:
        - "10.0 WHERE ( Person.files_single AND Person.earns <= 11925 )"
        - "10.0 WHERE ( Person.files_married_joint AND Person.earns <= 23850 )"
        - ...
```

derives a link of `Person.taxed_at` for each object of the first role player (Person)
using expressions that return a TaxRate based on the person's filing status and how
much they earn. If, for some person, none of the expressions can be evaluated, then 
the relationship will have no link involving that person.

Expressions here are interpreted as rules for assembling the links of the relationship
in the same way that a SQL query is interpreted as a rule for assembling the rows of
a new table. Each expression must therefore reference each role of the relationship,
either explicitly or implicitly. When an expression is relational, then it must explicitly
reference each role. On the other handm if an expression returns a value (like 10.0 in the
two examples here) then that value will implicitly play the last role, and the expression
must reference each of the other roles explicitly. 

A derived concept is one whose population is derived from those of its supertype concepts
using one or more expressions. For instance:

```yaml
- name: Employee
  extends: [Person]
  derived_by: [ "EXISTS ( Person.earns )" ]
```

declares that the population of Employee is derived from the population of Person by
classifying each Person who earns some salary as a Employee.

### Requires

The requires list contains expressions that give additional semantics to a concept or relationship
by declaring conditions that must hold over their populations. When applied to a concept, each
expression must reference the concept itself or one of its supertypes, as in:

```yaml
- name: SocialSecurityNr
  extends: [Integer]
  requires: [ "0 < SocialSecurityNr", "SocialSecurityNr <= 999999999" ]
```

When applied to a relationship, each expression must reference one or more roles of the
relationship. For instance, in:

```yaml
- name: Item
  extends: [Integer]
  relationships:
    offers_in:
      roles:
        - player: Store
      verbalizations: [ "{Item} is offered for sale in {Store}", "{Store} offers sale of {Item}" ]  
    total_sales_in:
      roles:
        - player: Store
        - player: Amount
      verbalizations: [ "{Item} sold for cumulative {Amount} in {Store}" ] 
      requires:
        - "Amount > 0.0"
        - "Item.offers_in(Store)"
```

the first expression requires any value that plays the `Amount` role to be positive while the second
requires any item that has sales in some store to be offered in that store.

---

## Complete Example

Here's a complete semantic model example showing all components working together:

```yaml
semantic_model:
  - name: ecommerce_analytics
    description: E-commerce sales and customer analytics
    ai_context:
      instructions: "Use this model for analyzing sales trends, customer behavior, and product performance"

    datasets:
      - name: orders
        source: sales.public.orders
        primary_key: [order_id]
        description: Customer orders
        fields:
          - name: order_id
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: order_id
            description: Order identifier
          
          - name: customer_id
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: customer_id
            description: Customer identifier
          
          - name: order_date
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: order_date
            dimension:
              is_time: true
            description: Order date
          
          - name: amount
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: amount
            description: Order amount

      - name: customers
        source: sales.public.customers
        primary_key: [id]
        description: Customer information
        fields:
          - name: id
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: id
            description: Customer identifier
          
          - name: email
            expression:
              dialects:
                - dialect: ANSI_SQL
                  expression: email
            description: Customer email

    join_paths:
      - name: orders_to_customers
        from: orders
        to: customers
        from_columns: [customer_id]
        to_columns: [id]

    metrics:
      - name: total_revenue
        expression:
          dialects:
            - dialect: ANSI_SQL
              expression: SUM(orders.amount)
        description: Total revenue from all orders
        ai_context:
          synonyms:
            - "total sales"
            - "revenue"

      - name: customer_count
        expression:
          dialects:
            - dialect: ANSI_SQL
              expression: COUNT(DISTINCT customers.id)
        description: Total number of customers
        ai_context:
          synonyms:
            - "total customers"
            - "customer base"

    custom_extensions:
      - vendor_name: SNOWFLAKE
        data: '{"warehouse": "ANALYTICS_WH"}'
```

---

## AI Context Structure

The `ai_context` field can be either a simple string or a structured object with specific keys:

**Simple String:**

```yaml
ai_context: "orders, purchases, sales"
```

**Structured Object:**

```yaml
ai_context:
  instructions: "Use this for sales analysis"
  synonyms:
    - "orders"
    - "purchases"
    - "sales"
  examples:
    - "Show total sales last month"
    - "What's the revenue by region?"
```

### Recommended AI Context Fields

| Field | Type | Description |
|-------|------|-------------|
| `instructions` | string | Instructions for AI on how to use this entity |
| `synonyms` | array | Alternative names and terms |
| `examples` | array | Sample questions or use cases |

---

## Version History

- **0.1.2** (2026-05-08): Support for both logical and conceptual modeling layers
  - Core conceptual model structure
  - Support for concepts, relationships, and logical -> conceptual schema mappings
  - Renamed relationships in the logical (semantic) layer to join paths to avoid
    conflict with relationships in the conceptual layer

- **0.1.1** (2025-12-11): Initial release
  - Core semantic model structure
  - Support for datasets, relationships, fields, and metrics
  - Multi-dialect metric expressions
  - Vendor extensibility framework
  - Context for agents

---

## License

See LICENSE file for details.
