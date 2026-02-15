package learning.reactive.exercises;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Exercise 2: Transformations and Operators
 * 
 * Learn how to transform reactive streams using various operators:
 * - map, flatMap, filter
 * - collect, reduce operations
 * - combining multiple streams
 */
public class Exercise02_Transformations {

    /**
     * TODO: Transform a Flux of integers to their square values
     * Input: [1, 2, 3, 4, 5]
     * Output: [1, 4, 9, 16, 25]
     * 
     * @param numbers input flux of integers
     * @return Flux<Integer> with squared values
     */
    public Flux<Integer> squareNumbers(Flux<Integer> numbers) {
        // Your code here
        return null;
    }

    /**
     * TODO: Filter and transform product names
     * Keep only products with name length > 5 and convert to uppercase
     * 
     * @param productNames flux of product names
     * @return Flux<String> filtered and uppercased
     */
    public Flux<String> filterAndTransformProducts(Flux<String> productNames) {
        // Your code here
        return null;
    }

    /**
     * TODO: Use flatMap to simulate async database lookup
     * For each user ID, return a Mono<String> with user details
     * 
     * @param userIds flux of user IDs
     * @return Flux<String> with user details
     */
    public Flux<String> fetchUserDetails(Flux<Long> userIds) {
        // Your code here
        // Simulate async database call for each user ID
        // Return something like "User-{id}: John Doe"
        return null;
    }

    /**
     * TODO: Collect all items from a Flux into a List
     * 
     * @param items flux of strings
     * @return Mono<List<String>> containing all items
     */
    public Mono<java.util.List<String>> collectToList(Flux<String> items) {
        // Your code here
        return null;
    }

    /**
     * TODO: Reduce a Flux of integers to their sum
     * 
     * @param numbers flux of integers
     * @return Mono<Integer> with the sum
     */
    public Mono<Integer> sumNumbers(Flux<Integer> numbers) {
        // Your code here
        return null;
    }

    /**
     * TODO: Combine two Flux streams by zipping them together
     * Create pairs of (productName, price)
     * 
     * @param productNames flux of product names
     * @param prices flux of prices
     * @return Flux<String> formatted as "ProductName: $Price"
     */
    public Flux<String> zipProductsWithPrices(Flux<String> productNames, Flux<Double> prices) {
        // Your code here
        return null;
    }

    /**
     * TODO: Take only the first 3 items and skip the first 2
     * From a Flux of numbers [1,2,3,4,5,6,7,8,9,10]
     * Should return [3,4,5]
     * 
     * @param numbers flux of integers
     * @return Flux<Integer> with skipped and limited items
     */
    public Flux<Integer> skipAndTake(Flux<Integer> numbers) {
        // Your code here
        return null;
    }

    /**
     * TODO: Group items by category and count them
     * Input: ["laptop", "phone", "laptop", "tablet", "phone", "laptop"]
     * Output should be a Mono with Map<String, Long> showing counts
     * 
     * @param items flux of product categories
     * @return Mono<java.util.Map<String, Long>> with category counts
     */
    public Mono<java.util.Map<String, Long>> groupAndCount(Flux<String> items) {
        // Your code here
        return null;
    }
}