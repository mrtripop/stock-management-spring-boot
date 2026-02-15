# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run
- `mvn spring-boot:run` - Start the Spring Boot application
- `mvn clean compile` - Clean and compile the project
- `mvn clean package` - Build JAR package
- `mvn test` - Run all unit tests

### Database and Infrastructure
- `docker compose up -d postgres` - Start PostgreSQL database only
- `docker compose up -d redis` - Start Redis cache only
- `docker compose up -d` - Start all services (app, postgres, redis, telemetry)
- `docker compose down` - Stop and remove all containers

### Development Setup
- `make setup-commitlint` - Setup commit message linting with conventional commits

### Reactive Programming Practice
- `mvn test -Dtest=ReactiveExerciseTest` - Run reactive programming exercise tests
- Exercises located in: `src/main/java/learning/reactive/exercises/`
- Solutions available in: `src/main/java/learning/reactive/solutions/`
- Practice guide: `src/main/java/learning/reactive/README.md`

## Architecture Overview

### Domain-Driven Structure
The codebase follows a domain-driven design with clear module separation:

- **Product Management** (`com.mrtripop.product`): Core inventory functionality with JPA entities, DTOs, services, and repositories
- **Location Management** (`com.mrtripop.location`): Warehouse and address management with specification patterns  
- **Order Management** (`com.mrtripop.order`): Order processing and tracking
- **User Management** (`com.mrtripop.users`): User entities and authentication
- **Transaction Management** (`com.mrtripop.transaction`): Financial transaction handling with BigDecimal precision

### Key Architectural Patterns

**Service Layer Pattern**: Each domain has interface-based services with implementation classes
- Example: `ProductService` interface with `ProductServiceImpl` and `DatabaseManagementImpl`

**Strategy Pattern**: Implemented for file parsing with factory pattern
- Location: `learning.patterns.strategies.FileParserFactory`
- Supports CSV, JSON, XML file parsing strategies

**Repository Pattern**: Spring Data JPA repositories with custom specifications
- Example: `AddressSpecification`, `WarehouseSpecification` for dynamic queries

**Decorator Pattern**: Service layer wraps database operations with caching and business logic
- `ProductServiceImpl` decorates `DatabaseManagementImpl` with caching annotations

**Reactive Programming**: Spring WebFlux integration for reactive streams
- Location: `learning.reactive` package with exercises and solutions
- Mono and Flux publishers for asynchronous data processing
- WebClient for reactive HTTP calls
- Reactive repository patterns with simulated database operations

### Configuration and Infrastructure

**Security**: Role-based access control with Spring Security
- JDBC-based user details management
- Role hierarchy: EMPLOYEE → MANAGER → ADMIN
- HTTP Basic Authentication

**Caching**: Redis-based caching with Spring Cache abstraction
- Product caching with TTL configuration
- Cache eviction strategies in service layer

**Observability**: OpenTelemetry integration for distributed tracing
- Configuration in `otel-config.yaml`
- Maven plugin downloads OpenTelemetry Java agent

**Database**: PostgreSQL with JPA/Hibernate
- Entity auditing enabled with `@EnableJpaAuditing`
- Custom sequence generators for entity IDs
- Snake case property naming strategy

**Reactive Stack**: Spring WebFlux with Reactor
- WebFlux starter for reactive web applications
- Reactor Test for testing reactive streams
- Project Reactor for reactive programming primitives

### Data Models and Relationships

**Product Entity**: Core inventory item with extensive metadata
- SKU/code system for internal identification
- UPC/barcode for universal identification  
- Physical dimensions for storage optimization
- Audit fields with timestamp tracking

**Location Hierarchy**: Address → Warehouse relationship
- ManyToOne relationship with cascade operations
- Warehouse-specific attributes (refrigeration capabilities)

**Financial Precision**: BigDecimal usage for monetary calculations
- Dedicated test classes for BigDecimal operations
- UserBalance operations with transaction safety

### Development Practices

**Exception Handling**: Global exception handling with `@ControllerAdvice`
- Custom error responses with stack traces
- Specific handling for NullPointerException vs general exceptions

**Logging**: Log4j2 configuration with structured logging
- AOP-based logging through `GlobalAspect`
- Different log levels per package

## Environment Configuration

The application uses environment variables for configuration:
- `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD` - Database connection
- `REDIS_URL`, `REDIS_USERNAME`, `REDIS_PASSWORD` - Redis connection  
- `CACHE_REDIS_TTL` - Cache time-to-live settings
- `LOGGING_LEVEL_COM_MRTRIPOP` - Application logging level

## Code Style and Guidelines

**Primary Style Guide**: Google Java Style Guide
- Follow Google Java Style Guide as the primary coding standard
- Use Google Java Format plugin in IntelliJ IDEA for automatic formatting
- Consistent naming conventions: camelCase for methods/variables, PascalCase for classes
- No wildcard imports, explicit import statements preferred
- Line length limit of 100 characters
- Use meaningful variable and method names

**Reactive Programming Guidelines**:
- Prefer non-blocking operations with Mono/Flux over blocking calls
- Use appropriate operators: map for synchronous transformations, flatMap for async
- Handle errors gracefully with onErrorReturn, onErrorResume, or retry mechanisms
- Apply backpressure strategies for high-throughput scenarios
- Test reactive streams with StepVerifier for predictable verification

## API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`

The application exposes REST APIs with role-based access control for inventory management operations.