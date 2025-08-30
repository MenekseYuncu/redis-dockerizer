package com.redisdockerizer.caching.caching.service;

import com.redisdockerizer.caching.caching.model.Product;
import com.redisdockerizer.caching.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for {@link Product} operations.
 * <p>
 * This service coordinates repository access and cache interactions for reads and writes.
 * The cache is keyed by the product's {@link UUID}.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Retrieve all products (non-cached).
     *
     * @return list of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Retrieve a product by its ID (cached).
     * <p>
     * On the first invocation for a given ID, the repository is hit (slower, includes artificial delay).
     * Later invocations return the cached result (faster).
     *
     * @param id product identifier
     * @return an {@link Optional} containing the product if found, otherwise empty
     */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getById(UUID id) {
        return productRepository.findById(id);
    }

    /**
     * Create a new product and populate the cache for its ID.
     *
     * @param product the product to create
     * @return the created product
     */
    @CachePut(value = "products", key = "#result.id")
    public Product create(Product product) {
        return productRepository.save(product);
    }

    /**
     * Update an existing product and refresh its cache entry.
     *
     * @param id      product identifier
     * @param product updated product data
     * @return an {@link Optional} containing the updated product if it existed, otherwise empty
     */
    @CachePut(value = "products", key = "#id")
    public Optional<Product> update(UUID id, Product product) {
        return productRepository.update(id, product);
    }

    /**
     * Deletes a product by its identifier and removes its cache entry if present.
     *
     * @param id the unique identifier of the product to delete
     * @return {@code true} if the product was successfully deleted,
     * {@code false} if no product was found with the given identifier
     */
    @CacheEvict(value = "products", key = "#id")
    public boolean delete(UUID id) {
        return productRepository.deleteById(id);
    }

    /**
     * Count the total number of products.
     *
     * @return number of products
     */
    public long count() {
        return productRepository.count();
    }
}