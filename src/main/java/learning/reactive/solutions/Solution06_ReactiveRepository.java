package learning.reactive.solutions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution 6: Reactive Repository Pattern
 */
public class Solution06_ReactiveRepository {

    private final Map<Long, String> database = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    public Mono<String> findById(Long id) {
        return Mono.fromCallable(() -> {
            Thread.sleep(50); // Simulate database delay
            return database.get(id);
        })
        .filter(result -> result != null);
    }

    public Flux<String> findAll() {
        return Flux.fromIterable(database.values())
                .delayElements(Duration.ofMillis(100));
    }

    public Mono<String> save(String productName) {
        return Mono.fromCallable(() -> {
            Thread.sleep(100); // Simulate database save
            Long id = nextId++;
            String productWithId = "Product[" + id + "]: " + productName;
            database.put(id, productWithId);
            return productWithId;
        });
    }

    public Mono<String> update(Long id, String productName) {
        return Mono.fromCallable(() -> {
            Thread.sleep(80); // Simulate database update
            if (!database.containsKey(id)) {
                throw new RuntimeException("Product not found with ID: " + id);
            }
            String updatedProduct = "Product[" + id + "]: " + productName;
            database.put(id, updatedProduct);
            return updatedProduct;
        });
    }

    public Mono<String> deleteById(Long id) {
        return Mono.fromCallable(() -> {
            Thread.sleep(60); // Simulate database delete
            String removed = database.remove(id);
            if (removed == null) {
                throw new RuntimeException("Product not found with ID: " + id);
            }
            return "Deleted product: " + removed;
        });
    }

    public Flux<String> findByNameContaining(String namePattern) {
        return Flux.fromIterable(database.values())
                .filter(product -> product.toLowerCase().contains(namePattern.toLowerCase()))
                .delayElements(Duration.ofMillis(50));
    }

    public Mono<Long> count() {
        return Mono.fromCallable(() -> {
            Thread.sleep(30); // Simulate count query
            return (long) database.size();
        });
    }

    public Mono<Boolean> existsById(Long id) {
        return Mono.fromCallable(() -> {
            Thread.sleep(20); // Simulate existence check
            return database.containsKey(id);
        });
    }

    public Flux<String> saveAll(Flux<String> productNames) {
        return productNames.flatMap(this::save);
    }

    public Flux<String> findAllPaginated(int page, int size) {
        return Flux.fromIterable(database.values())
                .skip(page * size)
                .take(size)
                .delayElements(Duration.ofMillis(50));
    }

    public Mono<String> saveWithInventory(String productName, int quantity) {
        return Mono.fromCallable(() -> {
            Thread.sleep(150); // Simulate transactional operations
            
            // Simulate transaction - both operations succeed or both fail
            if (Math.random() < 0.1) { // 10% chance of failure
                throw new RuntimeException("Transaction failed");
            }
            
            Long id = nextId++;
            String product = "Product[" + id + "]: " + productName;
            database.put(id, product);
            
            // Simulate inventory update
            String result = product + " with inventory: " + quantity;
            return result;
        });
    }

    public Mono<String> findByIdWithConnectionHandling(Long id) {
        return Mono.fromCallable(() -> {
            // Simulate random connection failures
            if (Math.random() < 0.3) { // 30% chance of connection failure
                throw new RuntimeException("Database connection failed");
            }
            
            Thread.sleep(100);
            return database.get(id);
        })
        .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
        .filter(result -> result != null)
        .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }
}