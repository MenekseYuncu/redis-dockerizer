package com.redisdockerizer.caching.caching.service;

import com.redisdockerizer.caching.caching.model.BookEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <h2>CacheService</h2>
 *
 * The CacheService class is responsible for managing book data stored in both an in-memory database
 * (represented by a {@link HashMap}) and a Redis cache. This service provides methods for retrieving,
 * saving, and deleting book entries, with caching capabilities for fast data retrieval.
 * <p>
 * The service uses Spring's caching annotations to interact with the Redis cache:
 * - {@link Cacheable} is used to cache the result of retrieving a book.
 * - {@link CachePut} is used to update the cache whenever a book is saved or updated.
 * - {@link CacheEvict} is used to remove a book from the cache when it is deleted.
 * <p>
 * Caching improves the application's performance by avoiding unnecessary database lookups and
 * reducing the load on backend systems for frequently accessed data.
 */
@Service
public class CacheService {

    private final Map<String, BookEntity> database = new HashMap<>();

    /**
     * Initializes the service with some default book entries.
     * These books are stored in both the in-memory database and the Redis cache once cached operations are called.
     */
    @PostConstruct
    public void initDefaultBook() {
        database.put("1", new BookEntity("1", "Default Book", "System", 2000));
        database.put("2", new BookEntity("2", "Default Book 1", "System", 2010));
        database.put("3", new BookEntity("3", "Default Book 2", "System", 2020));
    }

    /**
     * Retrieves a book by its ID from the in-memory database and caches it using the Redis cache.
     * If the book is not found in the cache, it will be retrieved from the database and stored in the cache.
     * The cached result will be associated with the book's ID as the key.
     *
     * @param id the ID of the book to retrieve
     * @return the {@link BookEntity} object corresponding to the given ID, or null if not found
     */
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public BookEntity getBookById(String id) {
        simulateSlowService();
        return database.get(id);
    }

    /**
     * Saves or updates a book in the in-memory database and updates the cache with the new book information.
     * This ensures that the cache is in sync with the database.
     *
     * @param book the {@link BookEntity} to be saved or updated
     * @return the saved or updated {@link BookEntity}
     */
    @CachePut(value = "books", key = "#book.id")
    public BookEntity saveBook(BookEntity book) {
        database.put(book.getId(), book);
        return book;
    }

    /**
     * Deletes a book from both the in-memory database and the Redis cache.
     * The cache is immediately evicted to ensure that outdated data is removed.
     *
     * @param id the ID of the book to delete
     */
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(String id) {
        database.remove(id);
    }

    /**
     * Simulates a delay to mimic the behavior of slow database queries.
     * This delay is used to demonstrate the caching mechanism's performance benefits.
     */
    private void simulateSlowService() {
        try {
            Thread.sleep(2000); // Simulate slow DB query with a 2-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
