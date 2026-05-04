# Project Documentation Index

## Project Overview

- **Type:** Monolith backend
- **Primary Language:** Java 17
- **Framework:** Spring Boot 3.4.2
- **Build Tool:** Maven
- **Architecture:** Layered with domain-driven package structure

## Quick Reference

- **Tech Stack:** Spring Boot 3.4.2 | Java 17 | PostgreSQL 14.6 | Redis 7.2 | Log4j2 | MapStruct | Lombok
- **Entry Point:** `mvn spring-boot:run` (port 8080)
- **Architecture Pattern:** Layered (Controller -> Service -> Repository) with domain-driven packages
- **API Docs:** http://localhost:8080/swagger-ui/index.html
- **Source Files:** 107 Java files across 7 business domains

## Generated Documentation

- [Project Overview](./project-overview.md)
- [Architecture](./architecture.md)
- [Source Tree Analysis](./source-tree-analysis.md)
- [Component Inventory](./component-inventory.md)
- [API Contracts](./api-contracts.md)
- [Data Models](./data-models.md)
- [Development Guide](./development-guide.md)
- [Deployment Guide](./deployment-guide.md)

## Existing Documentation

- [CLAUDE.md](../CLAUDE.md) - AI agent context (authoritative project instructions)
- [README.md](../README.md) - Project readme
- [CHANGELOG.md](../CHANGELOG.md) - Project changelog
- [ELK Stack Guide](./elk-stack-guide.md) - ELK stack setup instructions
- [Product Feature Spec](../features/product-feature.md) - Product domain feature specification
- [BMad Architecture](../_bmad-output/planning-artifacts/Architecture.md) - Previous BMad architecture artifact

## Getting Started

1. Start infrastructure: `docker compose up -d postgres redis`
2. Configure environment variables (see `.env.dev`)
3. Run: `./mvnw spring-boot:run`
4. Open Swagger UI: http://localhost:8080/swagger-ui/index.html
5. Run tests: `./mvnw test`

## Domain Map

| Domain | Maturity | Endpoints | Entities |
|--------|----------|-----------|----------|
| Product | High | `/api/inventory/products` | Product, ProductHistory |
| Clinical | High | `/api/v1/clinical/catalog/*` | Molecule, Brand, Store, StoreProduct |
| Location | Medium | `/api/v1/location/*` | Address, Warehouse |
| Order | Low | `/api/v1/order/*` | Order |
| Transaction | Low | `/api/v1/transaction/*` | Transaction, UserBalance |
| Users | Low | `/api/v1/users/*` | User |
