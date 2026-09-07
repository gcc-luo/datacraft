# ADR 0010: Phase 5 Pipeline Editor

## Status

Accepted

## Context

Phase 4 provides authenticated Pipeline and NodeMetadata APIs, but users still need a control-plane UI to inspect and edit the graph. The editor must remain independent from execution engines and must preserve the backend's node-key and DAG validation contract.

## Decision

- Build the editor as a three-column Vue Flow view: metadata-driven node palette, graph canvas, and property inspector.
- Use typed API DTOs and explicit graph mapping functions. Vue Flow node IDs are the stable backend `nodeKey`; save converts canvas elements back to `PipelineRequest`.
- Load node types from `GET /v1/node-types`; the palette and custom node render identity, category, and preferred engine from metadata rather than hard-coded engine behavior.
- Support Pipeline CRUD, node drag/drop, edge creation, node deletion, coordinate editing, engine preference, and JSON configuration validation. Keep save/delete errors visible and retain dirty state on save failure.
- Keep execution, publish, schedule, quality, planner, and third-party engine adapters outside this phase. The editor only changes the DataCraft control-plane model.
- Import Vue Flow base/theme styles globally and provide a responsive, reduced-motion-friendly visual treatment for the dark canvas and inspector.

## Consequences

Users can create and maintain valid Pipeline graph drafts through the UI using the Phase 4 contract. The editor has no execution-engine coupling, so Phase 6 can add planning and native execution without changing the graph authoring boundary. Server-side validation remains authoritative for DAG correctness and node configuration rules.
