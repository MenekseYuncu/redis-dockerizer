package com.redisdockerizer.caching.caching.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.caching.caching.model.Product;
import com.redisdockerizer.caching.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * {@code ProductDataLoader} is executed on application startup and is responsible
 * for initializing product data if the database is empty. The data is loaded from
 * a JSON file located at {@code resources/data/products.json}.
 *
 * <p>This component ensures that the application always has initial data available
 * for products when running for the first time.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    /**
     * Invoked at application startup. Checks whether product data exists in the database.
     * If the repository is empty, products are loaded from a JSON file.
     *
     * @param args command-line arguments passed to the application
     */
    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            loadProductsFromJson();
        } else {
            log.info("ProductDataLoader: Products already exist in the database (Total: {})", productRepository.count());
        }
    }

    /**
     * Loads product data from {@code products.json} located in the classpath and saves
     * it into the database. If the file cannot be found or an error occurs while reading,
     * appropriate error logs will be generated.
     */
    private void loadProductsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/products.json");

            if (!resource.exists()) {
                log.error("ProductDataLoader: products.json file not found in resources.");
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<Product> products = objectMapper.readValue(
                        inputStream,
                        new TypeReference<>() {
                        }
                );

                productRepository.saveAll(products);

                log.info("ProductDataLoader: {} products successfully loaded into the database.", products.size());
            }

        } catch (IOException e) {
            log.error("ProductDataLoader: Error while reading products.json: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("ProductDataLoader: Unexpected error occurred: {}", e.getMessage(), e);
        }
    }
}