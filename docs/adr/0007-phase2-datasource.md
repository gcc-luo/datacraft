# ADR 0007: Phase 2 JDBC Datasource Management

## Status

Accepted

## Context

Phase 2 requires users to manage database connections before metadata collection and Pipeline editing. DataCraft must keep datasource credentials out of API responses and logs while preserving the Control Plane / Execution Plane boundary.

## Decision

- Add a standalone `datacraft-datasource` module; controllers call an application service rather than MyBatis mappers.
- Support PostgreSQL and MySQL first, with structured host/port/database fields and server-generated JDBC URLs.
- Store only AES-256-GCM ciphertext in `dc_datasource.password_ciphertext`.
- Supply the encryption key through `DATACRAFT_DATASOURCE_ENCRYPTION_KEY`; the application fails fast unless it decodes to exactly 32 bytes.
- Decrypt only in the short-lived connection-test call. Return safe status messages instead of raw JDBC exceptions.
- Keep the Phase 2 UI in the existing console layout and defer metadata, Pipeline, and engine integration to later phases.

## Consequences

The platform can safely persist and test its first database connectors, but adding another connector requires extending the datasource type and its JDBC driver. Rotating the encryption key requires a planned migration/re-encryption operation, which is outside Phase 2.
