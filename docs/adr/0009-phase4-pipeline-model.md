# ADR 0009: Phase 4 Pipeline Control-Plane Model

## Status

Accepted

## Context

DataCraft needs a stable control-plane representation of visual data workflows before implementing a visual editor or execution planner. Pipeline structure must remain independent from third-party execution engines, and malformed graphs must not reach later planning or execution phases.

## Decision

- Add a standalone `datacraft-pipeline` module that depends on platform API/common infrastructure only; it has no DataX, Camel, SeaTunnel, or Flink dependency.
- Persist Pipeline, Node, and Edge data in `dc_pipeline`, `dc_pipeline_node`, and `dc_pipeline_edge`. Store a JSON graph snapshot on the Pipeline row for audit/debug support while keeping nodes and edges queryable.
- Use `PipelineStatus` (`DRAFT`, `ACTIVE`, `ARCHIVED`) and `ExecutionStrategy` (`AUTO`, `NATIVE`, `DATAX`, `CAMEL`, `SEATUNNEL`) as control-plane fields. Selecting a strategy does not instantiate an engine in this phase.
- Use a metadata-only `NodeRegistry` with three initial types: `DATABASE_SOURCE`, `FILTER`, and `DATABASE_SINK`. Execution implementations and SPI adapters remain future work.
- Validate names, unique node keys, known node types, edge endpoints, duplicate/self-loop edges, and DAG acyclicity at the application boundary. Empty graphs are valid drafts.
- Expose authenticated CRUD and node metadata REST APIs. Return API DTOs rather than persistence entities and use stable error codes for validation/not-found cases.
- Defer Vue Flow editing, planner, execution, quality, scheduling, and engine adapters to later phases.

## Consequences

The next frontend phase can build against a complete, authenticated graph contract without coupling the editor to execution engines. The graph snapshot is convenient for audit/debug, while normalized rows support detail queries and future indexing. Version increments provide a basic optimistic-change history signal; stronger concurrent-edit conflict handling can be added when collaborative editing becomes in scope.
