package com.menekse.redisdockerizer.caching;

import com.menekse.redisdockerizer.caching.model.BookEntity;
import com.menekse.redisdockerizer.caching.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * <h2>CacheController</h2>
 *
 * This REST controller provides endpoints for managing book information stored in a Redis cache.
 * It interacts with the {@link CacheService} to perform CRUD operations on book data stored in Redis.
 * The cache allows for fast retrieval and modification of book information without repeatedly querying
 * a database, providing an efficient and scalable solution for frequently accessed data.
 *
 * <p><b>Available operations:</b></p>
 * <ul>
 *     <li><b>GET /api/cache/{id}</b> → Retrieve a book from the cache by its ID.</li>
 *     <li><b>POST /api/cache</b> → Create or update a book entry in the cache.</li>
 *     <li><b>DELETE /api/cache/{id}</b> → Delete a book from the cache by its ID.</li>
 * </ul>
 *
 * <p>The Redis cache is used to store book entities temporarily, reducing database load and improving performance
 * for repeated read operations.</p>
 *
 * <p>All business logic related to caching operations is delegated to the {@link CacheService}.</p>
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    /**
     * Retrieves a book from the Redis cache by its ID.
     * If the book is not found in the cache, a 404 NOT_FOUND status is returned.
     * The cache helps to retrieve frequently requested book data quickly without hitting the database.
     *
     * @param id the ID of the book to retrieve
     * @return {@link BookEntity} if the book is found in the cache
     * @throws ResponseStatusException 404 NOT_FOUND if the book is not found in the cache
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * GET /api/cache/123
     *
     * Response:
     * {
     *   "id": "123",
     *   "title": "Clean Code",
     *   "author": "Robert C. Martin"
     * }
     * }</pre>
     */
    @GetMapping("/{id}")
    public BookEntity getBook(@PathVariable String id) {
        BookEntity book = cacheService.getBookById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return book;
    }

    /**
     * Creates a new book or updates an existing one in the Redis cache.
     * This operation ensures that the cache contains the latest information about the book.
     *
     * @param book the {@link BookEntity} to be created or updated in the cache
     * @return the saved {@link BookEntity}
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * POST /api/cache
     * Body:
     * {
     *   "id": "123",
     *   "title": "Domain-Driven Design",
     *   "author": "Eric Evans"
     * }
     * }</pre>
     */
    @PostMapping
    public BookEntity createOrUpdateBook(@RequestBody BookEntity book) {
        return cacheService.saveBook(book);
    }

    /**
     * Deletes a book from the Redis cache by its ID.
     * This operation ensures that the book is removed from the cache when it is no longer needed.
     *
     * @param id the ID of the book to delete
     * @return confirmation message indicating the deletion status
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * DELETE /api/cache/123
     *
     * Response:
     * "Book with id 123 deleted"
     * }</pre>
     */
    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable String id) {
        cacheService.deleteBook(id);
        return "Book with id " + id + " deleted";
    }

}
