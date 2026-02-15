package learning.reactive.solutions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Solution 3: Error Handling in Reactive Streams
 */
public class Solution03_ErrorHandling {

    public Mono<Integer> safeDivision(int numerator, int denominator) {
        return Mono.fromCallable(() -> {
            if (denominator == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return numerator / denominator;
        }).onErrorReturn(-1);
    }

    public Mono<String> networkCallWithFallback(boolean shouldFail) {
        return Mono.fromCallable(() -> {
            if (shouldFail) {
                throw new RuntimeException("Network error");
            }
            return "Network data";
        }).onErrorReturn("Fallback data");
    }

    public Mono<String> retryableOperation(double failureRate) {
        return Mono.fromCallable(() -> {
            if (Math.random() < failureRate) {
                throw new RuntimeException("Random failure");
            }
            return "Success";
        }).retryWhen(Retry.max(3));
    }

    public Mono<String> operationWithTimeout(int delaySeconds, int timeoutSeconds) {
        return Mono.fromCallable(() -> {
            Thread.sleep(delaySeconds * 1000);
            return "Operation completed";
        })
        .timeout(Duration.ofSeconds(timeoutSeconds))
        .onErrorReturn("Operation timed out");
    }

    public Flux<Integer> continueOnError(Flux<Integer> items) {
        return items.onErrorContinue((throwable, item) -> {
            System.err.println("Error processing item: " + item + ", error: " + throwable.getMessage());
        });
    }

    public Mono<String> transformError(Mono<String> operation) {
        return operation.onErrorMap(RuntimeException.class, 
            ex -> new IllegalStateException("Business error: " + ex.getMessage(), ex));
    }

    public Mono<String> exponentialBackoffRetry(Mono<String> unreliableService) {
        return unreliableService.retryWhen(
            Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(8))
        );
    }

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private boolean circuitOpen = false;

    public Mono<String> circuitBreakerPattern(Mono<String> externalService) {
        if (circuitOpen) {
            return Mono.just("Circuit breaker open - cached response");
        }

        return externalService
                .doOnSuccess(result -> failureCount.set(0))
                .doOnError(error -> {
                    if (failureCount.incrementAndGet() >= 3) {
                        circuitOpen = true;
                        // In real implementation, would schedule circuit reset
                    }
                })
                .onErrorReturn("Service unavailable");
    }
}