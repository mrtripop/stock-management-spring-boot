# Gemini Project Context

## Project Overview

This is a Spring Boot project for a stock management system. It's designed to be a learning project for Spring Boot best practices, architecture design, and various related technologies.

The project uses a modern Java stack, including:

*   **Framework:** Spring Boot 3.4.2
*   **Language:** Java 17
*   **Build Tool:** Maven
*   **Database:** PostgreSQL
*   **Caching:** Redis
*   **API Documentation:** Swagger (OpenAPI)
*   **Observability:** OpenTelemetry
*   **Development Tools:** Lombok, MapStruct

The application is structured with a clear separation of concerns, with packages for different domains like `product`, `order`, and `users`.

## Building and Running

### Prerequisites

*   Java 17
*   Maven
*   Docker

### Running the Application

1.  **Set up commitlint (first time only):**
    ```bash
    make setup-commitlint
    ```

2.  **Start the PostgreSQL database:**
    ```bash
    docker compose up -d postgres --build
    ```

3.  **Run the Spring Boot application:**
    ```bash
    mvn spring-boot:run
    ```

### API Documentation

Once the application is running, you can access the Swagger UI for API documentation at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Development Conventions

*   **Commit Messages:** The project uses [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) for its commit messages. A `commitlint` setup is included to enforce this.
*   **Code Style:** The project follows the [Google Java Format](https://github.com/google/google-java-format).
*   **Design Patterns:** The codebase makes use of several design patterns, including SOLID principles, Facade, Builder, and Strategy.
*   **Data Transfer Objects (DTOs):** DTOs are used to transfer data between different layers of the application.
