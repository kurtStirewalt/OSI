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
7. [Ontology](#ontology)
8. [Complete Example](#complete-example)

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

The allowable multiplicities of relationships defined in the [Ontology](#ontology) section.

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

Logical datasets represent business entities (fact and dimension tables). They contain fields and define the structure of the data.

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

## Ontology

Enterprise data are often modeled at both the logical level, in terms of datasets and fields,
and the conceptual level in the form of an ontology that comprises concepts, relationships,
and business rules. This section describes how to declare an ontology and schema mappings
from logical-level fields to concepts and relationships in the ontology. Ontologies are
organized hierarchically in top-level collection of tree structures, each root of which
is a concept, under which relationships and schema mappings are defined.

### Concepts

Concepts represent the types of things that have meaning in a business setting, e.g., person, company,
or salary. Each concept is either an entity type or a value type. Ontologies implicitly include a
value-type for each basic data type, like `Integer`, `Decimal`, and `String`, and an entity type
called `Any`. Every other concept ontology extends (is a subtype of) one of these concepts.

Concepts conform to the following schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `concept` | string | Yes | Unique name of this concept |
| `description` | string | No | Human-readable description |
| `relationships` | list | No | Relationships where this concept plays the first role |
| `extends` | list | No | Names of this concept's supertypes |
| `derived_by` | list | No | Expressions that derive this concept's population |
| `identify_by` | list | No | Names of relationships that uniquely reference objects of this concept |
| `requires` | list | No | Expressions that constrain this concept's population |
| `entity_mappings` | list | No | Mappings from field values to concept objects |
| `relationship_mappings` | list | No | Mappings from field values to relatioship links |

### Extends

Every user-declared concept extends one or more concepts in the ontology. The new concept
is a sutype of each concept that it extends, and the extended concepts are its supertypes.

Any concept that directly or indirectly extends a value type like `Integer` or `String` is a value type.
Any concept that does not extend some value type is an entity type, and if a concept declares no extends
list, then it is assumed to extend the built-in entity type `Any`. If `SocialSecurityNr` extends `Integer`
and `Employee` extends `Person`, which declares no extends list, then `SocialSecurityNr` is a value type
and both `Person` and `Employee` are entity types.

### Relationships

Relationships relate objects of one or more concepts and declare how to verbalize links among
those objects. Relationships have set (as opposed to bag) semantics, and links do not contain
nulls.

Each relationship that is declared under a concept conforms to the following schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Part of the identifier for this relationship |
| `description` | string | No | Human-readable description |
| `multiplicity` | enum | No | Multiplicity constraint |
| `roles` | list | No | List of additional roles in this relationship |
| `derived_by` | list | No | Expressions that derive links of this relationship |
| `requires` | list | No | Expressions that constrain this relationship's population |
| `verbalizes` | list | Yes | Patterns describing how to verbalize links |

Each relationship is uniquely identified by a prepending its declared name with that of the containing
concept. For instance, in:

```yaml
ontology:
  - concept: Person
    relationships:
      - name: earns
        roles:
          - player: Salary
        multiplicity: ManyToOne
        verbalizes: [ "{Person} earns {Salary}" ]
      ...
```

the relationship is identified by the string `Person.earns`. This convention naturally supports
expressions that navigate over the links of relationships using the “dot-join” operator in a
manner that is familiar to object-oriented programming languages. This relationship links
`Person` and `Salary` objects and verbalizes each link as “Person earns Salary.” 

#### Roles

Objects play roles in the links of a relationship. If you think of a relationship as a narrow table,
then its links are like rows and its roles are like columns. Each role is played by a concept that
constrains the type of objects that can play that role in any link. In `Person.earns`, `Person` and
`Salary` play the first and second roles respectively.

By convention, the first role of any relationship is played by the concept under which the
relationship is declared. Any additional roles are enumerated in order in the roles list
using this schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `player` | string | Yes | Name of the concept that plays this role |
| `name` | string | No | Optional role name |

For instance, in:

```yaml
ontology:
  - concept: Person
    relationships:
      - name: files_married_joint
        verbalizes: [ "{Person} files married filing joint" ]
      - name: purchased_on
        roles:
          - player: Vehicle
          - player: Date
        multiplicity: ManyToOne
        verbalizes: [ "{Person} puchased {Vehicle} on {Date}" ]
```

the unary relationship `Person.files_married_joint` has an empty roles list, while the
ternary relationship `Person.purchased_on` declares two additional roles played by
`Vehicle` and `Date` respectively,

The role player often suffices to distinguish the role within its relationship, but when
the same concept plays more than one role, the user must declare a distinguising name for
any additional role whose player does not suffice to distinguish it from other roles in
the same relationship. For instance, in:

```yaml
ontology:
  - concept: Store
    relationships:
      - name: ships_to_in_days
        roles:
          - player: Store
            name: destination
          - player: NrDays
        multiplicity: ManyToOne
        verbalizes: [ "{Store} ships to {destination} in {NrDays}" ]
```

the role name `destination` distinguishes the second `Store`-playing role from the first in
this relationship.

Expressions that are used to define derived_by rules and requires constraints will refer to
roles by name -- the name defaulting to the concept that plays the role unless an explicit
role name is provided. In any expression that involving links of the `Store.ships_to_in_days`
relationship can then use the variables `Store` and `destination` to refer to objecs that
play these two `Store`-playing roles without ambiguity.

#### Multiplicities

If a relationship comprises more than one role, objects that play the last role could be functionally
dermined by a tuple of objects that play the other roles. This knowledge is declared using a `ManyToOne`
multiplicity constraint. In the examples above, the constraint declares that each person earns at most
one salary and that for each pair of stores, the former ships to the latter in at most one number of
days. For relationships of ternary and higher arity, the multiplicity applies to the n-th role, meaning
the object that plays the n-th role is functionally determined by the tuple of objects that play
the first n-1 roles.

In the special case of a binary relationship, one might declare a `OneToOne` multiplicity, which
indicates the relationship is many-to-one in both directions. For instance, the `Person.nr`
relationship is one-to-one because each person is assigned at most one social security number
and each social security number is assigned to at most one person.

In the absence of any multiplicity, we make no assumptions of functional dependencies among
any of the roles.

### Identifying relationships

Many conceptual models distinguish one or more relationships to use when referencing entity-type
objects in expressions and queries. The `Person.nr` relationship can be used to reference a
person by their social security number; while the pair of relationships `License.acct` and
`License.seat_nr` can be used to reference a license by its associated account and seat number.
These identifier relationships are always binary, and their first role is always played by the
concept the relationship is used to reference.

### Derivation expressions

Concepts and relationships may be derived using expressions. Think of a derived concept or 
relationship as a conceptual view whose objects or links are derived from those of other
concepts or relationships. For instance:

```yaml
ontology:
  - concept: Person
    relationships:
      - name: parent_of
        roles:
          - player: Person
            name: "child"
        verbalizes: [ "{Person} is a parent of {Person:child}", "{Person:child} is a child of {Person}" ]
      - name: ancestor_of
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

declares two derived relationships -- `ancestor_of` and `taxed_at`. Each link of `Person.ancestor_of`
relates a person to one of its descendants. The two expressions form the base and recursive cases for
this calculation. In the base case, a `Person` as an ancestor of some `descendant` if that `Person`
is the parent of that descendant. And in the recursive case, a `Person` is an ancestor of some
`descendant` if that `Person` is an ancestor of the parent of that `descendant`. Notice in this
example how role names are used to disambiguate the two `Person` roles in this relationship.

Each link of `Person.taxed_at` links a `Person` object to a `TaxRate` that is derived using
expressions that determine the rate based on the person's filing status and how much they earn.
If, for some person, none of the expressions can be evaluated, then the relationship will have
no link involving that person.

Expressions that derive a relationship are interpreted as rules for constructing the links of the
relationship in the same way that a SQL query is interpreted as a rule for constructing the rows
of a new table. Each expression must therefore reference each role of the relationship, either
explicitly or implicitly. If an expression evaluates to some object (like 10.0 in the two examples
here) then that object will implicitly play the last role, and the expression must reference each
of the other roles explicitly. If an expression does not evaluate to any object, then it must
explicitly reference each role.

A derived concept is one whose population is derived from those of its supertype concepts
using one or more expressions. For instance:

```yaml
ontology:
  - concept: Employee
    extends: [Person]
    derived_by: [ "EXISTS ( Person.earns )" ]
```

declares that the population of Employee is derived from the population of Person by
classifying each Person who earns some salary as a Employee.

### Requires

The requires list contains expressions that give additional semantics to a concept or relationship
by declaring conditions that must hold over their populations. When applied to a concept, each
expression must reference the concept, as in:

```yaml
ontology:
  - concept: SocialSecurityNr
    extends: [Integer]
    requires: [ "0 < SocialSecurityNr", "SocialSecurityNr <= 999999999" ]
```

When applied to a relationship, each expression must reference one or more roles of the
relationship. For instance, in:

```yaml
ontology:
  - concept: Item
    extends: [Integer]
    relationships:
      - name: offers_in
        roles:
          - player: Store
        verbalizations: [ "{Item} is offered for sale in {Store}", "{Store} offers sale of {Item}" ]  
      - name: total_sales_in
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

### Mappings

Logical to conceptual schema mappings declare how field values map to conceptual objects and
relationship links among conceptual objects. The key idea is to map a SQL expression that computes
a value from one or more fields to some role that is played by a value-typed concept.

#### Entity mappings

Entity mappings declare how and under what conditions fields in the logical level map to objects of
some entity type in the population of the model. Each mapping conforms to the following schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `entity_map` | list | Yes | Role bindings for each identifying relationship |

For instance, the entity mapping in:

```yaml
ontology:
  - concept: Person
    relationships:
      - name: nr
        roles:
          - player: SocialSecurityNr
        multiplicity: OneToOne
        verbalizes: [ "{Person} is identified by {SocialSecurityNr}" ]
    identify_by: [ nr ]
    entity_mappings:
      - entity_map:
        - role: Person.nr
          expr: PERSONS.SSN
    ...
```

uses one role binding that maps values from the `SSN` field of dataset `PERSONS` to objects
that play the `SocialSecurityNr` role in the `nr` relationship. Because each link in that
relationship associates a `SocialSecurityNr` object to some unique `Person` object, this
role binding suffices to associate each distinct `SSN` value in the dataset to a distinct
`Person` object in the ontology.

A more interesting example maps fields to the `OrderLineItem`  concept, whose identifier
involves two relationships:

```yaml
  - concept: OrderLineItem
    relationships:
      - name: nr
        roles: [ concept: LineNr ]
        multiplicity: ManyToOne
      - name: order
        roles: [ concept: CustOrder ]
        multiplicity: ManyToOne
    identify_by: [ "nr", "order" ]
    requires: [ "OrderLineItem.nr", "OrderLineItem.order" ]
    entity_mappings:
      - entity_map:
        - role: OrderLineItem.nr
          expr: LINEITEMS.L_LINENUMBER
        - role: OrderLineItem.order
          entity_map:
            - role: CustOrder.nr
              expr: LINEITEMS.L_ORDERKEY

```

This entity mapping uses two role bindings -- one that maps the `L_LINENUMBER` field to the `LineNr`
role of the `nr` relationship, and one that uses a more complex structure to map `CustOrder` objects
to the role that concept plays in the `order` relationship. Because `LineNr` is a value-typed concept,
we can directly map an expression involving fields to that role just as in the previous example. But
because `CustOrder` is an entity type, we cannot directly map field values to its objects but must
instead use its identifier, which here involves one relationship called `CustOrder.nr`.

#### Relationship mappings

Relationship mappings declare how and under what conditions fields refer to objects that
play roles in the links of relationships. Unlike entity mappings, which declare how to
construct objects from fields, relationship mappings declare how to look up objects using
fields and then form tuples that populate relationships in the model.

Relationship mappings are organized hierarchically into trees with common tuple prefixes
to allow mapping to links of relationships of many different arities while minimizing
redundancy. Each node in the tree has this schema: 

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `concept` | string | No | Concept that plays the role mapped to by this node in the tree |
| `expr` | string | No | Maps fields when the concept is a value type |
| `entity_map` | list | Yes | Maps fields when the concept is an entity type |
| `relationship` | string | No | Relationship whose links are mapped to by this level in the tree |
| `children` | list | No | List of relationship child mappings |

For instance, the relationship mapping in:

```yaml
ontology:
  - concept: Item
    relationships:
      - name: nr
        roles: [ concept: SkuNr ]
        multiplicity: OneToOne
        verbalises: "{Item} is identified by {SkuNr}"
      - name: active     # A unary relationship
        verbalizes: [ "{Item} is actively sold" ]
      - name: active_in
        roles: [ concept: Store ]
        verbalises: [ "{Item} is actively sold in {Store}" ]
      - name: returned_in
        roles: [ concept: Store, concept: Amount ]
        verbalizes: [ "{Item} returned in {Store} for {Amount}" ]
        multiplicitly: ManyToOne
      - name: sold_in
        roles: [ concept: Store, concept: Amount ]
        verbalizes: [ "{Item} sells in {Store} for {Amount}" ]
        multiplicitly: ManyToOne
    identify_by: [ nr ]
    entity_mappings:
      - entity_map:
          role: Item.nr
          expr: ITEMS.SKU
    relationship_mappings:
      - entity_map:
          role: Item.nr
          expr: METRICS.SKU
        relationship: Item.active
        relationship_mappings:
          - concept: Store
            entity_map:
              role: Store.nr
              expr: METRICS.STORE
            relationship: Item.active_in
            relationship_mappings:
              - concept: Amount
                expr: METRICS.SALES
                relationship: Item.sold_in
              - concept: Amount
                expr: METRICS.RETURNS
                relationship: Item.returned_in
```

maps fields of the `METRICS` dataset to links of four different relationships. The hierarchical
structure simplifies the mapping by not declaring how to map the `SKU` field four times and the
`STORE` field three times.

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
