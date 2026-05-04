# Project Overview

## Stock Management System

A Spring Boot backend application for managing pharmaceutical inventory across multiple business domains including products, clinical catalogs, locations, orders, transactions, and users.

## Quick Reference

| Property | Value |
|----------|-------|
| **Type** | Monolith backend |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.4.2 |
| **Build** | Maven |
| **Database** | PostgreSQL 14.6 |
| **Cache** | Redis 7.2 |
| **Test DB** | H2 (PostgreSQL mode) |
| **Architecture** | Layered with domain-driven packages |
| **API Docs** | Swagger UI (springdoc-openapi) |
| **Observability** | ELK Stack + OpenTelemetry |
| **Containerization** | Docker (multi-stage build) |

## Repository Structure

**Type:** Monolith (single cohesive codebase)

## Source Statistics

- **Java source files**: 107
- **Business domains**: 7 (product, clinical, location, order, transaction, users, shared)
- **REST controllers**: 10
- **JPA repositories**: 13
- **Service classes**: 19
- **Test classes**: 7

## Documentation

- [Architecture](./architecture.md) - System architecture, patterns, and design decisions
- [Source Tree Analysis](./source-tree-analysis.md) - Annotated directory structure
- [API Contracts](./api-contracts.md) - REST API endpoint catalog
- [Data Models](./data-models.md) - Entity schemas and database design
- [Development Guide](./development-guide.md) - Setup, testing, and coding conventions
- [Deployment Guide](./deployment-guide.md) - Docker, infrastructure, and environment config

## Existing Documentation

- [CLAUDE.md](../CLAUDE.md) - AI agent context (authoritative)
- [README.md](../README.md) - Project readme
- [CHANGELOG.md](../CHANGELOG.md) - Project changelog
- [ELK Stack Guide](./elk-stack-guide.md) - ELK stack setup instructions
- [Product Feature Spec](../features/product-feature.md) - Product domain feature specification
- [BMad Architecture](../_bmad-output/planning-artifacts/Architecture.md) - Previous BMad architecture artifact
