package learning.reactive.solutions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Solution 1: Mono and Flux Basics
 */
public class Solution01_MonoFluxBasics {

    public Mono<String> createSimpleMono() {
        return Mono.just("Hello Reactive World");
    }

    public Flux<Integer> createSimpleFlux() {
        return Flux.range(1, 5);
    }

    public Mono<String> createMonoFromCallable(Long userId) {
        return Mono.fromCallable(() -> {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            // Simulate database call
            Thread.sleep(100);
            return "User data for ID: " + userId;
        });
    }

    public Flux<String> createFluxFromList() {
        List<String> products = Arrays.asList("Laptop", "Phone", "Tablet", "Monitor", "Keyboard");
        return Flux.fromIterable(products);
    }

    public Mono<String> createEmptyMono() {
        return Mono.empty();
    }

    public Mono<String> createErrorMono() {
        return Mono.error(new RuntimeException("Service unavailable"));
    }

    public Flux<Integer> createInfiniteFlux() {
        AtomicInteger counter = new AtomicInteger(0);
        return Flux.generate(sink -> {
            int next = counter.addAndGet(2);
            sink.next(next);
        });
    }

    public Flux<Integer> createDelayedFlux() {
        return Flux.range(1, 3)
                .delayElements(Duration.ofSeconds(1));
    }
}