package learning.reactive.exercises;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * Exercise 4: Spring WebFlux Controllers
 * 
 * Learn how to create reactive REST controllers:
 * - Reactive endpoints returning Mono/Flux
 * - Server-Sent Events (SSE)
 * - Handling request/response reactively
 */
@RestController
@RequestMapping("/api/reactive")
public class Exercise04_WebFluxControllers {

    /**
     * TODO: Create a simple GET endpoint that returns a Mono<String>
     * URL: GET /api/reactive/hello
     * 
     * @return Mono<String> with greeting message
     */
    @GetMapping("/hello")
    public Mono<String> hello() {
        // Your code here
        return null;
    }

    /**
     * TODO: Create an endpoint that returns a list of products as Flux
     * URL: GET /api/reactive/products
     * 
     * @return Flux<Product> with sample products
     */
    @GetMapping("/products")
    public Flux<String> getProducts() {
        // Your code here
        // Return a Flux of product names with some delay between emissions
        return null;
    }

    /**
     * TODO: Create an endpoint that accepts a product ID and returns product details
     * URL: GET /api/reactive/products/{id}
     * Handle case where product is not found
     * 
     * @param id product ID
     * @return Mono<String> with product details or 404
     */
    @GetMapping("/products/{id}")
    public Mono<String> getProduct(@PathVariable Long id) {
        // Your code here
        // Simulate database lookup with delay
        // Return empty Mono for id > 10 (not found case)
        return null;
    }

    /**
     * TODO: Create a POST endpoint that accepts product data
     * URL: POST /api/reactive/products
     * 
     * @param productName request body with product name
     * @return Mono<String> with creation response
     */
    @PostMapping("/products")
    public Mono<String> createProduct(@RequestBody String productName) {
        // Your code here
        // Simulate saving product with delay
        // Return success message with generated ID
        return null;
    }

    /**
     * TODO: Create a Server-Sent Events endpoint
     * URL: GET /api/reactive/events
     * Stream real-time stock updates every 2 seconds
     * 
     * @return Flux<String> as Server-Sent Events
     */
    @GetMapping(value = "/events", produces = "text/event-stream")
    public Flux<String> stockUpdates() {
        // Your code here
        // Create infinite flux that emits stock updates every 2 seconds
        // Format: "Stock Update: PRODUCT-{random} - Price: ${random}"
        return null;
    }

    /**
     * TODO: Create an endpoint that demonstrates backpressure
     * URL: GET /api/reactive/stream
     * Return a large stream of data with controlled emission rate
     * 
     * @param count number of items to emit
     * @param delayMs delay between emissions in milliseconds
     * @return Flux<String> with controlled emission rate
     */
    @GetMapping("/stream")
    public Flux<String> streamData(@RequestParam(defaultValue = "100") int count,
                                   @RequestParam(defaultValue = "100") int delayMs) {
        // Your code here
        // Create Flux that emits 'count' items with 'delayMs' delay between each
        return null;
    }

    /**
     * TODO: Create an endpoint that combines multiple async operations
     * URL: GET /api/reactive/combined/{userId}
     * Fetch user data, user orders, and user preferences in parallel
     * 
     * @param userId user ID
     * @return Mono<String> with combined user information
     */
    @GetMapping("/combined/{userId}")
    public Mono<String> getCombinedUserData(@PathVariable Long userId) {
        // Your code here
        // Create 3 separate Mono operations (user, orders, preferences)
        // Combine them using zip or similar operator
        return null;
    }

    /**
     * TODO: Create an endpoint that handles file upload reactively
     * URL: POST /api/reactive/upload
     * Process uploaded content and return processing status
     * 
     * @param content file content as string
     * @return Mono<String> with processing result
     */
    @PostMapping("/upload")
    public Mono<String> uploadFile(@RequestBody String content) {
        // Your code here
        // Simulate file processing with delay
        // Return processing status and file size
        return null;
    }
}