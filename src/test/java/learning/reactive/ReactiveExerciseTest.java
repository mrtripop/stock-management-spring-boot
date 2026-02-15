package learning.reactive;

import java.time.Duration;

import learning.reactive.exercises.Exercise01_MonoFluxBasics;
import learning.reactive.solutions.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** Test class for all reactive programming exercises Use this to verify your implementations */
class ReactiveExerciseTest {

  // Exercise 1 Tests - Mono and Flux Basics
  @Test
  void testMonoBasics() {
     Exercise01_MonoFluxBasics solution = new Exercise01_MonoFluxBasics();

    // Test simple Mono
    StepVerifier.create(solution.createSimpleMono())
        .expectNext("Hello Reactive World")
        .verifyComplete();

    // Test simple Flux
    StepVerifier.create(solution.createSimpleFlux()).expectNext(1, 2, 3, 4, 5).verifyComplete();

    // Test Mono from callable - success case
    StepVerifier.create(solution.createMonoFromCallable(123L))
        .expectNext("User data for ID: 123")
        .verifyComplete();

    // Test Mono from callable - error case
    StepVerifier.create(solution.createMonoFromCallable(null))
        .verifyError(IllegalArgumentException.class);

    // Test Flux from list
    StepVerifier.create(solution.createFluxFromList())
        .expectNext("Phone", "TV", "Clock")
        .verifyComplete();

    // Test empty Mono
    StepVerifier.create(solution.createEmptyMono()).verifyComplete();

    // Test error Mono
    StepVerifier.create(solution.createErrorMono()).verifyError(RuntimeException.class);

    // Test infinite Flux (limited to first 3 elements)
    StepVerifier.create(solution.createInfiniteFlux().take(3)).expectNext(2, 4, 6).verifyComplete();

    // Test delayed Flux with virtual time
    StepVerifier.withVirtualTime(solution::createDelayedFlux)
        .expectSubscription()
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(1)
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(2)
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(3)
        .verifyComplete();
  }

  // Exercise 2 Tests - Transformations
  @Test
  void testTransformations() {
    Solution02_Transformations solution = new Solution02_Transformations();

    // Test square numbers
    Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);
    StepVerifier.create(solution.squareNumbers(numbers))
        .expectNext(1, 4, 9, 16, 25)
        .verifyComplete();

    // Test filter and transform
    Flux<String> products = Flux.just("Phone", "Laptop", "TV", "Mouse", "Keyboard");
    StepVerifier.create(solution.filterAndTransformProducts(products))
        .expectNext("LAPTOP", "KEYBOARD")
        .verifyComplete();
  }

  // Exercise 3 Tests - Error Handling
  @Test
  void testErrorHandling() {
    Solution03_ErrorHandling solution = new Solution03_ErrorHandling();

    // Test safe division
    StepVerifier.create(solution.safeDivision(10, 2)).expectNext(5).verifyComplete();

    StepVerifier.create(solution.safeDivision(10, 0)).expectNext(-1).verifyComplete();

    // Test network call with fallback
    StepVerifier.create(solution.networkCallWithFallback(false))
        .expectNext("Network data")
        .verifyComplete();

    StepVerifier.create(solution.networkCallWithFallback(true))
        .expectNext("Fallback data")
        .verifyComplete();
  }

  // Utility test for delayed operations
  @Test
  void testDelayedOperations() {
    Solution01_MonoFluxBasics solution = new Solution01_MonoFluxBasics();

    StepVerifier.create(solution.createDelayedFlux())
        .expectNext(1)
        .expectNext(2)
        .expectNext(3)
        .verifyComplete();
  }

  // Test infinite flux (with take to limit)
  @Test
  void testInfiniteFlux() {
    Solution01_MonoFluxBasics solution = new Solution01_MonoFluxBasics();

    StepVerifier.create(solution.createInfiniteFlux().take(3)).expectNext(2, 4, 6).verifyComplete();
  }

  // Integration test combining multiple operations
  @Test
  void testCombinedOperations() {
    Solution02_Transformations solution = new Solution02_Transformations();

    Flux<String> names = Flux.just("Apple", "Banana", "Cherry");
    Flux<Double> prices = Flux.just(1.0, 2.0, 3.0);

    StepVerifier.create(solution.zipProductsWithPrices(names, prices))
        .expectNext("Apple: $1.0", "Banana: $2.0", "Cherry: $3.0")
        .verifyComplete();
  }

  // Test repository operations
  @Test
  void testRepositoryOperations() {
    Solution06_ReactiveRepository repository = new Solution06_ReactiveRepository();

    // Test save and find
    StepVerifier.create(repository.save("Test Product").flatMap(saved -> repository.findById(1L)))
        .expectNextMatches(result -> result.contains("Test Product"))
        .verifyComplete();

    // Test count
    StepVerifier.create(repository.count()).expectNext(1L).verifyComplete();
  }

  // Performance test with timing
  @Test
  void testPerformanceWithTiming() {
    Solution01_MonoFluxBasics solution = new Solution01_MonoFluxBasics();

    StepVerifier.withVirtualTime(() -> solution.createDelayedFlux())
        .expectSubscription()
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(1)
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(2)
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(3)
        .verifyComplete();
  }

  // Test error scenarios
  @Test
  void testErrorScenarios() {
    Solution03_ErrorHandling solution = new Solution03_ErrorHandling();

    // Test timeout
    StepVerifier.create(solution.operationWithTimeout(3, 1))
        .expectNext("Operation timed out")
        .verifyComplete();

    StepVerifier.create(solution.operationWithTimeout(1, 3))
        .expectNext("Operation completed")
        .verifyComplete();
  }

  // Example of how to test your own implementations
  @Test
  void testYourImplementation() {
    // Uncomment and use your exercise classes
    // Exercise01_MonoFluxBasics exercise = new Exercise01_MonoFluxBasics();

    // StepVerifier.create(exercise.createSimpleMono())
    //         .expectNext("Hello Reactive World")
    //         .verifyComplete();
  }
}
