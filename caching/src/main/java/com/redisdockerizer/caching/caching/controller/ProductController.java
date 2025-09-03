package com.redisdockerizer.caching.caching.controller;

import com.redisdockerizer.caching.caching.exception.ProductNotFoundException;
import com.redisdockerizer.caching.caching.model.Product;
import com.redisdockerizer.caching.caching.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * @return list of products (possibly empty).
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Retrieves a single product by its unique identifier.
     * <p>
     * Use this endpoint to validate per-entity cache entries. A first call
     * typically results in a cache miss; repeated calls should be cache hits
     * until the entry is invalidated by a write operation.
     *
     * @param id the product UUID.
     * @return the product if found.
     * @throws ProductNotFoundException if the product does not exist (mapped to HTTP 404).
     */
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getById(id)
                .orElseThrow(ProductNotFoundException::new);
    }

    /**
     * Creates a new product.
     * <p>
     * This operation should invalidate or update relevant cache entries (e.g.,
     * list caches) to prevent stale reads. The request body is validated and
     * any provided {@code id} is ignored in favor of a newly generated one.
     *
     * @param product the product payload (without an ID).
     * @return the persisted product.
     */
    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        product.setId(null);
        return productService.create(product);
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
     * @return the updated product if it exists.
     * @throws ProductNotFoundException if the product does not exist (mapped to HTTP 404).
     */
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id,
                                 @Valid @RequestBody Product product) {
        return productService.update(id, product)
                .orElseThrow(ProductNotFoundException::new);
    }

    /**
     * Deletes a product by its unique identifier.
     * <p>
     * Intended to validate cache eviction on delete operations. After deletion,
     * later reads of the same ID should miss both the cache and the backing store.
     *
     * @param id the product UUID to delete.
     * @throws ProductNotFoundException if the product does not exist (mapped to HTTP 404).
     */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        boolean deleted = productService.delete(id);
        if (!deleted) {
            throw new ProductNotFoundException();
        }
    }

    /**
     * Returns the total number of products.
     * <p>
     * Can be used to validate whether aggregate/list caches are refreshed after
     * write operations. Depending on your caching strategy, this endpoint may or
     * may not be cached.
     *
     * @return total product count.
     */
    @GetMapping("/count")
    public long getProductCount() {
        return productService.count();
    }
}