package learning.reactive.solutions;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Solution 5: Reactive HTTP Client with WebClient
 */
public class Solution05_WebClient {

    private final WebClient webClient;
    private final AtomicInteger failureCount = new AtomicInteger(0);

    public Solution05_WebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    public Mono<String> getUser(Long userId) {
        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Flux<String> getAllUsers() {
        return webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(String.class);
    }

    public Mono<String> createUser(String userData) {
        return webClient.post()
                .uri("/users")
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getUserWithPosts(Long userId) {
        Mono<String> userMono = webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(String.class);

        Mono<String> postsMono = webClient.get()
                .uri("/users/{id}/posts", userId)
                .retrieve()
                .bodyToMono(String.class);

        return Mono.zip(userMono, postsMono)
                .map(tuple -> "User: " + tuple.getT1() + ", Posts: " + tuple.getT2());
    }

    public Mono<String> callWithRetry(String endpoint) {
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
    }

    public Mono<String> getUserWithStatusHandling(Long userId) {
        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                    response -> Mono.error(new RuntimeException("User not found")))
                .onStatus(status -> status.is5xxServerError(),
                    response -> Mono.error(new RuntimeException("Server error")))
                .bodyToMono(String.class)
                .onErrorReturn("Error occurred while fetching user");
    }

    public Mono<String> callWithTimeout(String endpoint) {
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn("Request timed out - fallback response");
    }

    public Flux<String> getAllUsersPaginated() {
        // Simulate pagination by fetching users and comments
        Flux<String> users = webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(String.class);

        Flux<String> comments = webClient.get()
                .uri("/comments")
                .retrieve()
                .bodyToFlux(String.class)
                .take(10); // Limit comments

        return Flux.concat(users, comments);
    }

    public Mono<String> uploadFile(String fileContent) {
        return webClient.post()
                .uri("/posts") // Using posts endpoint as upload simulation
                .bodyValue("{\"title\":\"File Upload\",\"body\":\"" + fileContent + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "File uploaded successfully: " + response);
    }

    public Mono<String> callWithCircuitBreaker(String endpoint) {
        if (failureCount.get() >= 3) {
            return Mono.just("Circuit breaker open - cached response");
        }

        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(result -> failureCount.set(0))
                .doOnError(error -> failureCount.incrementAndGet())
                .onErrorReturn("Service temporarily unavailable");
    }
}