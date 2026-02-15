package learning.reactive.exercises;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * Exercise 3: Error Handling in Reactive Streams
 * 
 * Learn how to handle errors gracefully in reactive programming:
 * - onErrorReturn, onErrorResume
 * - retry mechanisms
 * - timeout handling
 */
public class Exercise03_ErrorHandling {

    /**
     * TODO: Handle division by zero with a default value
     * If denominator is 0, return -1 instead of error
     * 
     * @param numerator the numerator
     * @param denominator the denominator
     * @return Mono<Integer> with result or default value
     */
    public Mono<Integer> safeDivision(int numerator, int denominator) {
        // Your code here
        // Create a Mono that performs division but handles divide by zero
        return null;
    }

    /**
     * TODO: Handle network call with fallback
     * Simulate a network call that might fail
     * If it fails, return fallback data
     * 
     * @param shouldFail whether the call should fail
     * @return Mono<String> with data or fallback
     */
    public Mono<String> networkCallWithFallback(boolean shouldFail) {
        // Your code here
        // Simulate network call, use onErrorReturn for fallback
        return null;
    }

    /**
     * TODO: Retry failed operations
     * Simulate an unreliable service that fails randomly
     * Retry up to 3 times before giving up
     * 
     * @param failureRate probability of failure (0.0 to 1.0)
     * @return Mono<String> with result after potential retries
     */
    public Mono<String> retryableOperation(double failureRate) {
        // Your code here
        // Use Math.random() to simulate random failures
        // Apply retry logic
        return null;
    }

    /**
     * TODO: Handle timeout scenarios
     * Create a Mono that takes too long and apply timeout
     * Provide fallback value when timeout occurs
     * 
     * @param delaySeconds how long the operation takes
     * @param timeoutSeconds timeout threshold
     * @return Mono<String> with result or timeout fallback
     */
    public Mono<String> operationWithTimeout(int delaySeconds, int timeoutSeconds) {
        // Your code here
        // Create delayed operation and apply timeout with fallback
        return null;
    }

    /**
     * TODO: Filter out errors and continue processing
     * Given a Flux that might emit errors, continue with valid items only
     * 
     * @param items flux that might contain errors
     * @return Flux<Integer> with only successful items
     */
    public Flux<Integer> continueOnError(Flux<Integer> items) {
        // Your code here
        // Use onErrorContinue or similar to skip errors
        return null;
    }

    /**
     * TODO: Transform errors to different types
     * Convert any RuntimeException to a custom business exception
     * 
     * @param operation mono that might throw RuntimeException
     * @return Mono<String> with transformed error
     */
    public Mono<String> transformError(Mono<String> operation) {
        // Your code here
        // Use onErrorMap to transform exceptions
        return null;
    }

    /**
     * TODO: Exponential backoff retry
     * Implement retry with exponential backoff (1s, 2s, 4s)
     * 
     * @param unreliableService mono that might fail
     * @return Mono<String> with exponential backoff retry
     */
    public Mono<String> exponentialBackoffRetry(Mono<String> unreliableService) {
        // Your code here
        // Use retryWhen with exponential backoff
        return null;
    }

    /**
     * TODO: Circuit breaker pattern simulation
     * After 3 consecutive failures, return cached value immediately
     * 
     * @param externalService mono representing external service call
     * @return Mono<String> with circuit breaker logic
     */
    public Mono<String> circuitBreakerPattern(Mono<String> externalService) {
        // Your code here
        // Implement basic circuit breaker logic
        return null;
    }
}