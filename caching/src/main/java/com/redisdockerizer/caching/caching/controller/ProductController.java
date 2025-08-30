package com.redisdockerizer.caching.caching.controller;

import com.redisdockerizer.caching.caching.model.Product;
import com.redisdockerizer.caching.caching.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller exposing CRUD endpoints for {@link Product} resources.
 * <p>
 * These endpoints are intentionally simple and are primarily used to verify
 * and benchmark Redis-based caching strategies at the service layer
 * ({@link ProductService}). Typical experiments include:
 * <ul>
 *   <li>Measuring cache hits/misses on read endpoints</li>
 *   <li>Validating cache invalidation on create/update/delete paths</li>
 *   <li>Assessing end-to-end latency improvements under load</li>
 * </ul>
 *
 * <h2>HTTP Base Path</h2>
 * <pre>/api/products</pre>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Input validation is enforced via {@code jakarta.validation} annotations.</li>
 *   <li>All identifiers are UUIDs.</li>
 *   <li>Service-level caching logic (e.g., Spring Cache with Redis) is expected
 *       to be implemented within {@link ProductService}.</li>
 * </ul>
 */
@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Retrieves all products.
     * <p>
     * Intended to exercise read-through cache behavior when the service layer
     * is backed by Redis. Later identical requests should serve data from
     * cache (given a stable dataset and proper cache configuration).
     *
     * @return {@code 200 OK} with the complete list of products (possibly empty).
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a single product by its unique identifier.
     * <p>
     * Use this endpoint to validate per-entity cache entries. A first call
     * typically results in a cache miss; repeated calls should be cache hits
     * until the entry is invalidated by a write operation.
     *
     * @param id the product UUID.
     * @return {@code 200 OK} with the product if found; otherwise {@code 404 Not Found}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Optional<Product> product = productService.getById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new product.
     * <p>
     * This operation should invalidate or update relevant cache entries (e.g.,
     * list caches) to prevent stale reads. The request body is validated and
     * any provided {@code id} is ignored in favor of a newly generated one.
     *
     * @param product the product payload (without an ID).
     * @return {@code 201 Created} with the persisted product.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        product.setId(null);
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing product.
     * <p>
     * Use to verify write-through behavior and entity-level cache invalidation.
     * On success, associated cache entries (entity and relevant collections)
     * should be evicted or refreshed to ensure later reads are consistent.
     *
     * @param id      the product UUID to update.
     * @param product the new product state (validated).
     * @return {@code 200 OK} with the updated product if it exists; otherwise {@code 404 Not Found}.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id,
                                                 @Valid @RequestBody Product product) {
        Optional<Product> updated = productService.update(id, product);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a product by its unique identifier.
     * <p>
     * Intended to validate cache eviction on delete operations. After deletion,
     * later reads of the same ID should miss both the cache and the backing store.
     *
     * @param id the product UUID to delete.
     * @return {@code 204 No Content} if deletion succeeded; otherwise {@code 404 Not Found}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        boolean deleted = productService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Returns the total number of products.
     * <p>
     * Can be used to validate whether aggregate/list caches are refreshed after
     * write operations. Depending on your caching strategy, this endpoint may or
     * may not be cached.
     *
     * @return {@code 200 OK} with the total product count.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getProductCount() {
        long count = productService.count();
        return ResponseEntity.ok(count);
    }
}