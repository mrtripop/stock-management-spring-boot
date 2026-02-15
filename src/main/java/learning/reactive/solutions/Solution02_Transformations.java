package learning.reactive.solutions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Solution 2: Transformations and Operators
 */
public class Solution02_Transformations {

    public Flux<Integer> squareNumbers(Flux<Integer> numbers) {
        return numbers.map(n -> n * n);
    }

    public Flux<String> filterAndTransformProducts(Flux<String> productNames) {
        return productNames
                .filter(name -> name.length() > 5)
                .map(String::toUpperCase);
    }

    public Flux<String> fetchUserDetails(Flux<Long> userIds) {
        return userIds.flatMap(id -> 
            Mono.fromCallable(() -> {
                // Simulate async database call
                Thread.sleep(100);
                return "User-" + id + ": John Doe";
            })
        );
    }

    public Mono<List<String>> collectToList(Flux<String> items) {
        return items.collectList();
    }

    public Mono<Integer> sumNumbers(Flux<Integer> numbers) {
        return numbers.reduce(0, Integer::sum);
    }

    public Flux<String> zipProductsWithPrices(Flux<String> productNames, Flux<Double> prices) {
        return Flux.zip(productNames, prices)
                .map(tuple -> tuple.getT1() + ": $" + tuple.getT2());
    }

    public Flux<Integer> skipAndTake(Flux<Integer> numbers) {
        return numbers.skip(2).take(3);
    }

    public Mono<Map<String, Long>> groupAndCount(Flux<String> items) {
        return items
                .groupBy(Function.identity())
                .flatMap(group -> group.count().map(count -> 
                    Map.entry(group.key(), count)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}