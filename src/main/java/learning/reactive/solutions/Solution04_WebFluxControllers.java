package learning.reactive.solutions;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Random;

/**
 * Solution 4: Spring WebFlux Controllers
 */
@RestController
@RequestMapping("/api/reactive")
public class Solution04_WebFluxControllers {

    private final Random random = new Random();

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello from Reactive Spring!")
                .delayElement(Duration.ofMillis(100));
    }

    @GetMapping("/products")
    public Flux<String> getProducts() {
        return Flux.just("Laptop", "Phone", "Tablet", "Monitor", "Keyboard")
                .delayElements(Duration.ofMillis(500));
    }

    @GetMapping("/products/{id}")
    public Mono<String> getProduct(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            Thread.sleep(200); // Simulate database lookup
            if (id > 10) {
                return null; // Not found
            }
            return "Product " + id + ": Sample Product";
        })
        .filter(result -> result != null)
        .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }

    @PostMapping("/products")
    public Mono<String> createProduct(@RequestBody String productName) {
        return Mono.fromCallable(() -> {
            Thread.sleep(300); // Simulate saving
            Long generatedId = random.nextLong(1000);
            return "Created product: " + productName + " with ID: " + generatedId;
        });
    }

    @GetMapping(value = "/events", produces = "text/event-stream")
    public Flux<String> stockUpdates() {
        return Flux.interval(Duration.ofSeconds(2))
                .map(sequence -> {
                    String product = "PRODUCT-" + random.nextInt(100);
                    double price = 10.0 + random.nextDouble() * 90.0;
                    return String.format("Stock Update: %s - Price: $%.2f", product, price);
                });
    }

    @GetMapping("/stream")
    public Flux<String> streamData(@RequestParam(defaultValue = "100") int count,
                                   @RequestParam(defaultValue = "100") int delayMs) {
        return Flux.range(1, count)
                .map(i -> "Data item " + i)
                .delayElements(Duration.ofMillis(delayMs));
    }

    @GetMapping("/combined/{userId}")
    public Mono<String> getCombinedUserData(@PathVariable Long userId) {
        Mono<String> userData = Mono.fromCallable(() -> {
            Thread.sleep(100);
            return "User: " + userId;
        });

        Mono<String> orderData = Mono.fromCallable(() -> {
            Thread.sleep(150);
            return "Orders: 5";
        });

        Mono<String> preferencesData = Mono.fromCallable(() -> {
            Thread.sleep(120);
            return "Preferences: Dark mode";
        });

        return Mono.zip(userData, orderData, preferencesData)
                .map(tuple -> String.format("%s, %s, %s", 
                    tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    @PostMapping("/upload")
    public Mono<String> uploadFile(@RequestBody String content) {
        return Mono.fromCallable(() -> {
            Thread.sleep(500); // Simulate file processing
            int size = content.length();
            return String.format("File processed successfully. Size: %d bytes", size);
        });
    }
}