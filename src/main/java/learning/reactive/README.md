# Reactive Programming Practice Guide

## Overview

This directory contains comprehensive exercises and solutions for learning reactive programming with Java and Spring WebFlux. The exercises are designed to progress from basic concepts to advanced reactive patterns.

## Prerequisites

- Java 17+
- Spring Boot 3.x
- Basic understanding of Java streams
- Familiarity with Spring Framework

## Exercise Structure

### Phase 1: Fundamentals
- **Exercise 1**: Mono and Flux basics
- **Exercise 2**: Transformations and operators
- **Exercise 3**: Error handling strategies

### Phase 2: Spring WebFlux
- **Exercise 4**: Reactive controllers and endpoints
- **Exercise 5**: WebClient for HTTP calls
- **Exercise 6**: Reactive repository patterns

## Quick Start

1. **Add Dependencies**: Ensure WebFlux dependencies are in pom.xml
2. **Run Exercises**: Start with Exercise01 and work through each one
3. **Check Solutions**: Compare your implementation with provided solutions
4. **Run Tests**: Use the test files to verify your implementations

## Running the Exercises

### Method 1: Interactive Development
```bash
# Start the application
mvn spring-boot:run

# Test reactive endpoints
curl http://localhost:8080/api/reactive/hello
curl http://localhost:8080/api/reactive/products
```

### Method 2: Unit Testing
```bash
# Run specific test class
mvn test -Dtest=ReactiveExerciseTest

# Run all reactive tests
mvn test -Dtest=*Reactive*
```

## Key Concepts Covered

### 1. Publisher Types
- **Mono**: 0-1 item publisher
- **Flux**: 0-N items publisher
- Cold vs Hot streams
- Subscription lifecycle

### 2. Operators
- **Transformation**: map, flatMap, concatMap
- **Filtering**: filter, take, skip
- **Combination**: zip, merge, concat
- **Error Handling**: onErrorReturn, onErrorResume, retry

### 3. Backpressure
- Understanding flow control
- Buffer strategies
- Overflow handling

### 4. Threading
- Event loops vs thread pools
- Scheduler types (parallel, elastic, immediate)
- Thread-safe operations

## Exercise Guidelines

### Before Starting Each Exercise:
1. Read the JavaDoc comments carefully
2. Understand the expected input/output
3. Think about which operators to use
4. Consider error scenarios

### While Working:
1. Start with simple implementations
2. Add error handling gradually
3. Test with different inputs
4. Use debugging techniques (log, doOnNext)

### After Completion:
1. Compare with provided solutions
2. Run tests to verify correctness
3. Experiment with variations
4. Understand performance implications

## Common Patterns

### Creating Publishers
```java
// From single value
Mono<String> mono = Mono.just("Hello");

// From multiple values
Flux<Integer> flux = Flux.just(1, 2, 3);

// From collection
Flux<String> fromList = Flux.fromIterable(Arrays.asList("a", "b", "c"));

// From callable
Mono<String> fromCallable = Mono.fromCallable(() -> "result");
```

### Transformations
```java
// Simple mapping
flux.map(x -> x * 2)

// Async transformation
flux.flatMap(x -> Mono.fromCallable(() -> process(x)))

// Filtering
flux.filter(x -> x > 0)
```

### Error Handling
```java
// Return default on error
mono.onErrorReturn("default")

// Handle and recover
mono.onErrorResume(error -> Mono.just("recovered"))

// Retry on failure
mono.retryWhen(Retry.max(3))
```

## Testing Reactive Code

Use `StepVerifier` for testing reactive streams:

```java
@Test
public void testMonoCreation() {
    Mono<String> mono = Mono.just("Hello");
    
    StepVerifier.create(mono)
        .expectNext("Hello")
        .verifyComplete();
}
```

## Common Pitfalls

1. **Blocking Operations**: Avoid `block()` in production code
2. **Thread Safety**: Be careful with shared mutable state
3. **Memory Leaks**: Always handle subscriptions properly
4. **Error Propagation**: Understand how errors flow through operators
5. **Cold Streams**: Remember that most streams are cold by default

## Performance Tips

1. Use appropriate schedulers for different workloads
2. Consider backpressure strategies for high-throughput scenarios
3. Batch operations when possible (buffer, window)
4. Use `publishOn` and `subscribeOn` carefully
5. Profile memory usage with infinite streams

## Next Steps

After completing these exercises:

1. **Advanced Topics**: Custom operators, hot streams, reactive security
2. **Integration**: R2DBC for reactive database access
3. **Testing**: Advanced testing patterns with TestPublisher
4. **Production**: Monitoring, debugging, and optimization
5. **Ecosystem**: RSocket, Spring Cloud Gateway, reactive messaging

## Resources

- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Reactive Streams Specification](https://www.reactive-streams.org/)
- [Reactor Debugging Guide](https://projectreactor.io/docs/core/release/reference/#debugging)

## Troubleshooting

### Common Issues:
- **Nothing happens**: Remember to subscribe to publishers
- **Blocking errors**: Check for blocking calls in reactive context
- **Memory issues**: Verify proper stream termination
- **Performance problems**: Review scheduler usage and operator choice

### Debugging Tips:
- Use `.log()` operator to see stream events
- Add `.checkpoint()` for better stack traces
- Use IDE debugger with breakpoints in operators
- Monitor thread usage with profilers