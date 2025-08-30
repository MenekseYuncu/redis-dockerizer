package com.redisdockerizer.caching.caching.repository;

import com.redisdockerizer.caching.caching.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of a {@link Product} repository.
 * <p>
 * This repository stores products in a {@link ConcurrentHashMap} instead of
 * a real database. It is primarily used for demonstration and testing.
 * <p>
 * To simulate the effect of caching, the {@link #findById(UUID)} method
 * includes an artificial delay of 1 second before returning results.
 */
@Repository
public class ProductRepository {

    private final ConcurrentHashMap<UUID, Product> products = new ConcurrentHashMap<>();

    /**
     * Retrieve all products from the repository.
     *
     * @return list of all products
     */
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    /**
     * Retrieve a product by its unique identifier.
     * <p>
     * An artificial delay of 1 second is added to simulate
     * database latency and highlight caching effects.
     *
     * @param id product identifier
     * @return an {@link Optional} containing the product if found, otherwise empty
     */
    public Optional<Product> findById(UUID id) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Optional.ofNullable(products.get(id));
    }

    /**
     * Save a product to the repository.
     * <p>
     * If the product does not already have an ID, a new UUID will be generated.
     *
     * @param product the product to be saved
     * @return the saved product instance
     */
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }
        products.put(product.getId(), product);
        return product;
    }

    /**
     * Save multiple products to the repository in a batch operation.
     * <p>
     * For each product, if it does not already have an ID, a new UUID will be generated.
     *
     * @param productList list of products to be saved
     * @return list of saved products
     */
    public List<Product> saveAll(List<Product> productList) {
        return productList.stream()
                .map(this::save)
                .toList();
    }

    /**
     * Updates an existing product in the repository with the provided details.
     * If the product ID does not exist or the input parameters are invalid, 
     * an empty {@link Optional} is returned.
     *
     * @param productId the unique identifier of the product to be updated
     * @param updatedProduct the product object containing updated details
     * @return an {@link Optional} containing the updated product if successful, otherwise empty
     */
    public Optional<Product> update(UUID productId, Product updatedProduct) {
        if (productId == null || updatedProduct == null) {
            return Optional.empty();
        }
        if (!products.containsKey(productId)) {
            return Optional.empty();
        }

        return Optional.of(updateExistingProduct(productId, updatedProduct));
    }

    /**
     * Updates an existing product in the repository with new data.
     * This method replaces the product with the specified ID using 
     * the details provided in the {@code updatedProduct} parameter.
     *
     * @param productId      the unique identifier of the product to be updated
     * @param updatedProduct the product object containing updated information
     * @return the updated product instance
     */
    private Product updateExistingProduct(UUID productId, Product updatedProduct) {
        updatedProduct.setId(productId);
        products.put(productId, updatedProduct);
        return updatedProduct;
    }

    /**
     * Delete a product by its identifier.
     *
     * @param id product identifier
     * @return {@code true} if a product was removed, {@code false} otherwise
     */
    public boolean deleteById(UUID id) {
        Product removed = products.remove(id);
        return removed != null;
    }

    /**
     * Count the total number of products in the repository.
     *
     * @return number of products
     */
    public long count() {
        return products.size();
    }
}