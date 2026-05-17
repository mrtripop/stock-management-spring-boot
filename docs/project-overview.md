# Project Overview

This document provides a high-level overview of the Stock Management Spring Boot project.

## Project Description
This is a Spring Boot project for a stock management system. It's designed to be a learning project for Spring Boot best practices, architecture design, and various related technologies.

## Technology Stack
*   **Framework:** Spring Boot 3.4.2
*   **Language:** Java 17
*   **Build Tool:** Maven
*   **Database:** PostgreSQL
*   **Caching:** Redis
*   **API Documentation:** Swagger (OpenAPI)
*   **Observability:** OpenTelemetry
*   **Development Tools:** Lombok, MapStruct

## Core Modules
The application is structured into the following core modules, each representing a distinct domain or set of features:

*   **`inventory`**: Manages stock levels, warehouses, and inventory movements.
*   **`location`**: Handles geographical locations, storage bins, and physical addresses.
*   **`order`**: Processes customer orders, order fulfillment, and status tracking.
*   **`product`**: Defines product details, categories, and pricing.
*   **`transaction`**: Manages financial and stock-related transactions.
*   **`users`**: Handles user authentication, authorization, and user profiles.

## Building and Running
(Refer to the main `README.md` and `GEMINI.md` for detailed instructions.)