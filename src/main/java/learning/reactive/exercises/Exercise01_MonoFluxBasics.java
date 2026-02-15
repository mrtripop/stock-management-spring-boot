package learning.reactive.exercises;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercise 1: Mono and Flux Basics
 *
 * <p>Learn the fundamental building blocks of reactive programming: - Creating Mono and Flux
 * publishers - Basic subscription patterns - Understanding cold vs hot streams
 */
public class Exercise01_MonoFluxBasics {

  /**
   * TODO: Create a Mono that emits a single string "Hello Reactive World"
   *
   * @return Mono<String>
   */
  public Mono<String> createSimpleMono() {
    // Your code here
    return Mono.just("Hello Reactive World");
  }

  /**
   * TODO: Create a Flux that emits integers from 1 to 5
   *
   * @return Flux<Integer>
   */
  public Flux<Integer> createSimpleFlux() {
    // Your code here
//    return Flux.just(1, 2, 3, 4, 5);
    return Flux.range(1, 5);
  }

  /**
   * TODO: Create a Mono from a callable that might throw an exception Simulate a database call that
   * returns a user ID
   *
   * @param userId the user ID to fetch
   * @return Mono<String> representing the user data
   */
  public Mono<String> createMonoFromCallable(Long userId) {
    // Your code here - simulate fetching user data
    // Handle the case where userId is null (should emit error)
    return Mono.fromCallable(() -> {
      if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
      return "User data for ID: " + userId;
    });
  }

  /**
   * TODO: Create a Flux that emits product names Use Flux.fromIterable() with a list of product
   * names
   *
   * @return Flux<String>
   */
  public Flux<String> createFluxFromList() {
    // Your code here - create a list of product names and convert to Flux
    List<String> productNames = List.of("Phone", "TV", "Clock");
    return Flux.fromIterable(productNames);
  }

  /**
   * TODO: Create an empty Mono This is useful for operations that might not return a value
   *
   * @return Mono<String>
   */
  public Mono<String> createEmptyMono() {
    // Your code here
    return Mono.empty();
  }

  /**
   * TODO: Create a Mono that emits an error Use a custom exception with message "Service
   * unavailable"
   *
   * @return Mono<String>
   */
  public Mono<String> createErrorMono() {
    // Your code here
    return Mono.error(new RuntimeException("Service unavailable"));
  }

  /**
   * TODO: Create a Flux that generates infinite sequence of even numbers Starting from 2, 4, 6, 8,
   * ... Use Flux.generate() or Flux.create()
   *
   * @return Flux<Integer>
   */
  public Flux<Integer> createInfiniteFlux() {
    // Your code here
    AtomicInteger counter = new AtomicInteger(0);
    return Flux.create(
        e -> {
          var next = counter.addAndGet(2);
          e.next(next);
        });
  }

  /**
   * TODO: Create a Flux with delay between emissions Emit numbers 1, 2, 3 with 1 second delay
   * between each
   *
   * @return Flux<Integer>
   */
  public Flux<Integer> createDelayedFlux() {
    // Your code here
    return Flux.range(1, 3).delayElements(Duration.ofSeconds(1));
  }
}
