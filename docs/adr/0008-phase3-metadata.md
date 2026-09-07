# ADR 0008: Phase 3 Database Metadata Collection

## Status

Accepted

## Context

DataCraft needs a control-plane view of database assets before users can design Pipelines. The first supported connectors are PostgreSQL and MySQL, and metadata collection must not expose datasource credentials or pull business rows into the application JVM.

## Decision

- Add a standalone `datacraft-metadata` module; the module depends on datasource abstractions but does not add any execution-engine dependency.
- Store the current datasource snapshot in `dc_dataset` and `dc_dataset_field`. Re-sync deletes the datasource snapshot and inserts a fresh one in a transaction; field rows are removed by foreign-key cascade.
- Use JDBC `DatabaseMetaData` for schemas, tables, columns, primary keys, and remarks.
- Use read-only PostgreSQL/MySQL dialect queries only for estimated row counts. A row-count query failure leaves the estimate null rather than failing structural collection.
- Expose only API response DTOs. Dataset responses contain structural metadata and never contain datasource passwords or ciphertext.
- Keep the first scope to PostgreSQL/MySQL Schema, Table, and Field metadata. Views, indexes, constraints, lineage, sampling, and profiling remain out of scope.
- Provide an authenticated asset catalog with datasource selection, schema/table navigation, breadcrumb detail, field search, and manual sync.

## Consequences

The platform can present a stable asset catalog and the next Pipeline phase can reference dataset IDs. Snapshot replacement keeps the read model simple, but each sync currently recollects the full datasource and may be more expensive than incremental refresh for very large catalogs. Estimated row counts are intentionally advisory and may be null or approximate depending on the source database statistics.
