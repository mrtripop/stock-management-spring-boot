# Stock Management by Spring Boot

This project aim to learn Spring Boot best practice, architecture design, and improve my skill. Concept of this project
for simulate manage stock system feature to know how it's work and what design is.

## Prerequisite

- [Homebrew](https://brew.sh/)
- [Maven 3.9.5](https://formulae.brew.sh/formula/maven#default)
- [Amazon Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- [Java version manager](https://www.jenv.be/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/?section=mac)
- [Google Java Format](https://plugins.jetbrains.com/plugin/8527-google-java-format)
- [SonarLint](https://plugins.jetbrains.com/plugin/7973-sonarlint)
- [CommitLint](https://www.notion.so/Commitlint-on-local-ea1ec27b07b444f5b1b19d1b5506cbbd)
- [GPG signing key](https://www.notion.so/Commit-Signature-Verification-5eff1efc706340149c38ef93d3c58a0d)

## Important

If you need to contribute with the project, first step, you need to set up commitlint for verify the commit message.
I prepare for these steps for setting up by open `terminal` and run `make setup-commitlint`.</br>

## Configuration

Go to `src/main/resourse` file `application.yml`<br/>

**Datasource Connection**

Port `5432` is default port of the PostgreSQL

- `localhost` - Local environment is not using docker network
- `postgres` - Local environment is using docker network

Credentials of the PostgreSQL to establish connection

- `postgres` is a setting value for `username` and `password`

**Redis Connection**

Port `6379` is default port of the Redis

- `localhost` - Local environment is not using docker network
- `redis` - Local environment is using docker network

Credentials of the Redis to establish connection

- `default` is default username of the Redis.
- `redis` is setting value in the docker compose `--requirepass` argument

## How to run

Note: change `datasource config` correctly before start application.

1. Start database container at first.

```shell
docker compose up -d postgres --build
```

2. Check docker image

```shell
docker image ls
```

3. Check docker container running

```shell
docker container ls
```

4. Start application from terminal

```shell
mvn spring-boot:run
```

5. Stop docker compose and remove container

```shell
docker compose down
```

## URLs Resource

In this section, we define the API endpoint & URLs for any resource as a Swagger. Please run the service and access API
specification via web browser.

```shell
http://localhost:8080/swagger-ui/index.html
```

## Basic Knowledge

**General**

- [Conventional Commit](https://www.conventionalcommits.org/en/v1.0.0/)
- [Java Code Style Guild](https://www.cs.cornell.edu/courses/JavaAndDS/JavaStyle.html)
- [Read request body in SNAKE](https://stackoverflow.com/questions/70710979/how-to-auto-convert-camel-case-request-body-to-snake-case-protobuf-message-in-sp)
- [Data Transfer Object(DTO)](https://www.javaguides.net/2022/12/spring-boot-dto-example-tutorial.html)
- [Canonical Logging Context]

**Code Pattern**

- [SOLID]
- [Facade](https://refactoring.guru/design-patterns/facade/java/example)
- [Builder]
- [Strategies Design Pattern](https://medium.com/codex/implementing-the-strategy-design-pattern-in-spring-boot-df3adb9ceb4a)

**Design System**

- [Database Entity of Inventory System](https://vertabelo.com/blog/data-model-for-inventory-management-system/)
- [Writing Address in English](https://medium.com/@pakapolper/writing-an-address-in-english-29dfbdc3d1ba)
- Database Naming Convention Best Practice
    - [Stack Overflow](https://stackoverflow.com/questions/7662/database-table-and-column-naming-conventions)
    - [Microsoft SQL Server Sample - GitHub](https://github.com/microsoft/sql-server-samples/blob/master/samples/databases/adventure-works/README.md)

**Development**

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Postgresql](https://www.postgresql.org/)
- [Unit test in Spring Boot]
- [Integration test in Spring Boot]

**Deployment**

- [Logging Format - Better Stack](https://betterstack.com/community/guides/logging/log-formatting/)
- [Log Level]
- [OpenTelemetry Logging](https://opentelemetry.io/docs/specs/otel/logs/)
- [Three pillars of Observability](https://www.oreilly.com/library/view/distributed-systems-observability/9781492033431/ch04.html)
- [Setup OpenTelemetry in Spring Boot](https://www.notion.so/Setup-OpenTelemetry-in-Spring-Boot-f273e32194af44fda8e46a7fecea9b4e?pvs=4)
- [Spring Boot with Actuator]
- [Kubernetes]

## Technical Challenge

**Java**

- [Stream Internals - Java 8](https://www.linkedin.com/pulse/stream-internals-java-8-dhinesh-kumar/)
- [Object mapping - MapStruct](https://phayao.medium.com/มาใช้-mapstruct-ใน-java-กันเถอะ-4b54bd0a3219)
- [BigDecimal in Java](https://medium.com/@mrtripop/ทำความรู้จักกับ-bigdecimalใน-java-กัน-a8fe3cd26640)

**Spring Boot**

- API Security
    - [Long-live Credentials]
    - [Authentication & Authorization]
- API Performance
    - [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/redis.html)
- Aspect Oriented Programming
    - [Spring AOP Tutorial](https://howtodoinjava.com/spring-aop-tutorial/)
    - [Config Spring AOP](https://www.digitalocean.com/community/tutorials/spring-aop-example-tutorial-aspect-advice-pointcut-joinpoint-annotations)
    - [Spring AOP for Logging](https://www.baeldung.com/spring-aspect-oriented-programming-logging)
    - [Spring docs @ControllerAdvice](https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-advice.html)
    - [Github issue @ControllerAdvice](https://github.com/spring-projects/spring-framework/issues/25070)
- Race Condition
- [Map query param into POJOs](https://stackoverflow.com/questions/16942193/spring-mvc-complex-object-as-get-requestparam)
    - **Note**: Above, setter method must input string as argument, then string value convert into expect data
      type.
- Prometheus Metric & Grafana Dashboard

**Spring Email**

- [Amazon SES](https://docs.aws.amazon.com/ses/latest/dg/creating-identities.html#just-verify-email-proc)
- [Baedung Spring Email](https://www.baeldung.com/spring-email)

**Spring Data JPA**

- [Getting started with Spring Data JPA](https://spring.io/blog/2011/02/10/getting-started-with-spring-data-jpa)
- [Advanced Spring Data JPA - Specifications and Querydls](https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl)
- [Advanced Spring Data JPA - Specification](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)
- [JPA Metamodel Generator](https://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/#whatisit)
- [JPA Metamodel Generator - Baedung](https://www.baeldung.com/hibernate-criteria-queries-metamodel)
- [Spring Data REST Relationship](https://www.baeldung.com/spring-data-rest-relationships)

**Redis Cache**

- [Connect to Redis server via Redisinsight](https://stackoverflow.com/questions/64295255/redisinsight-on-docker-and-redis-on-docker-could-not-connect-error-99-connecti)
- [Redis Sentinal]
- [Redis Cluster with Leader-Follower]

**RabbitMQ**

- [Dead-Letter Message]

### Reactive Programming & Spring WebFlux

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Reactor Core Documentation](https://projectreactor.io/docs/core/release/reference/)
- [Spring WebFlux Tutorial](https://www.baeldung.com/spring-webflux)
- [Reactive Streams Specification](https://www.reactive-streams.org/)
- [WebFlux vs Spring MVC](https://www.baeldung.com/spring-webflux-vs-spring-mvc)
- [Mono and Flux Guide](https://www.baeldung.com/reactor-core)
- [WebFlux Testing](https://www.baeldung.com/spring-webflux-testing)
- [Reactive Streams Specification](https://www.reactive-streams.org/)
- [WebFlux vs Spring MVC](https://www.baeldung.com/spring-webflux-vs-spring-mvc)
- [Mono and Flux Guide](https://www.baeldung.com/reactor-core)
- [WebFlux Testing](https://www.baeldung.com/spring-webflux-testing)
- [Reactive Database Access](https://www.baeldung.com/spring-data-r2dbc)

## Best Practices & Performance Optimization

### Java & Spring Boot Development

**1. Declarative Collection Processing**
Prefer using Java Stream API (`Collectors.toMap`, `toList`, etc.) over manual loops for better readability and maintainability.
- **Before:** Manual `HashMap` initialization and `forEach` loop.
- **After:** `list.stream().collect(Collectors.toMap(Key::getId, Function.identity()))`.

**2. Coding to Interfaces**
Always use interface types (e.g., `Map`, `List`) for variables and method parameters instead of concrete implementations (e.g., `HashMap`, `ArrayList`). This follows the principle of "coding to an interface," making the code more flexible and easier to test.

**3. Efficient Null Checks**
For local variables where performance is critical or for simple checks, prefer `variable != null` over `Optional.ofNullable(variable).isPresent()`. This avoids unnecessary object allocation and improves code clarity.

**4. Batch Processing & Lookups**
When processing large datasets (e.g., CSV imports):
- **Use Hash-based Lookups:** Store database records in a `Map` ($O(1)$ lookup) instead of nested loops or repeated database queries ($O(N)$). This reduces complexity from $O(N^2)$ to $O(N)$.
- **Memory Efficiency:** A `HashMap` storing 1,000 object references has negligible memory overhead (approx. 32-64 KB), while providing significant performance gains during batch updates.

## Reactive Programming Practice Guide

### Learning Path

**Phase 1: Fundamentals**

1. Understanding Reactive Streams concepts
2. Mono and Flux basics
3. Operators: map, filter, flatMap
4. Error handling in reactive streams

**Phase 2: Spring WebFlux**

1. WebFlux configuration
2. Reactive controllers and handlers
3. WebClient for reactive HTTP calls
4. Reactive data access with R2DBC

**Phase 3: Advanced Topics**

1. Backpressure handling
2. Custom operators
3. Reactive security
4. Performance optimization

### Exercises Directory Structure

```
src/main/java/learning/reactive/
├── exercises/
│   ├── Exercise01_MonoFluxBasics.java
│   ├── Exercise02_Transformations.java
│   ├── Exercise03_ErrorHandling.java
│   ├── Exercise04_WebFluxControllers.java
│   ├── Exercise05_WebClient.java
│   └── Exercise06_ReactiveRepository.java
├── solutions/
│   ├── Solution01_MonoFluxBasics.java
│   ├── Solution02_Transformations.java
│   ├── Solution03_ErrorHandling.java
│   ├── Solution04_WebFluxControllers.java
│   ├── Solution05_WebClient.java
│   └── Solution06_ReactiveRepository.java
└── README.md
```

### Quick Start Commands

```bash
# Add WebFlux dependency to pom.xml
# Create reactive exercises
mkdir -p src/main/java/learning/reactive/{exercises,solutions}

# Run specific reactive test
mvn test -Dtest=ReactiveExerciseTest

# Start application in reactive mode
mvn spring-boot:run -Dspring.profiles.active=reactive
```

### Key Concepts to Master

1. **Publisher Types**: Mono (0-1 item), Flux (0-N items)
2. **Hot vs Cold Streams**: Understanding when subscription matters
3. **Threading Models**: Event loops vs traditional thread pools
4. **Backpressure**: Managing flow control in reactive streams
5. **Error Handling**: onError, retry, fallback strategies
6. **Testing**: StepVerifier for reactive testing

## Reference

- [PostgreSQL with docker volume](https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/)
- [Redisinsight tool](https://hub.docker.com/r/redis/redisinsight)
- [No validator could be found for constraint 'NotEmpty' validating type 'Integer'](https://stackoverflow.com/a/56096275/22370509)

## API Testing


```

Two Ways to Run Agent Teams
                                                                                                                                                                                                 
  1. Sequential (Standard — what you've been doing)
                                                                                                                                                                                                 
  Run agents one at a time, each in a fresh chat, in the order the phases dictate:

  Chat 1: /bmad-brainstorming → /bmad-create-prd
  Chat 2: /bmad-create-architecture
  Chat 3: /bmad-create-epics-and-stories
  Chat 4: /bmad-sprint-planning                                                                                                                                                                  
  Chat 5: /bmad-create-story → /bmad-dev-story → /bmad-code-review
                                                                                                                                                                                                 
  Each agent reads the artifacts the previous one produced. This is the recommended default — it's how the framework is designed.

  2. Party Mode (Multi-Agent Discussion)

  Invoke /bmad-party-mode to bring multiple agents into one conversation:

  /bmad-party-mode Should we use monolith or microservices for this pharmacy system?

  BMad Master (orchestrator) selects the most relevant agents (Architect, Dev, PM) and they debate in character — agreeing, disagreeing, building on each other's ideas. You steer the           
  discussion.
                                                                                                                                                                                                 
  Best for: Big decisions, brainstorming, retrospectives, or when you want diverse perspectives fast.

  Key Rules

  - Always start fresh chats between different workflows/skills
  - Invoke agents by name to load their persona: /bmad-agent-pm, /bmad-agent-architect, /bmad-agent-dev
  - Use /bmad-help anytime to know what to run next — it tracks what's done
  - Party Mode is the only multi-agent-in-one-room feature; the rest is sequential handoff                                                                                                       
   
  For your current project, you're already following the sequential flow correctly. If you ever hit a decision point where you want the whole team to weigh in, that's when Party Mode shines.   
                  
```



