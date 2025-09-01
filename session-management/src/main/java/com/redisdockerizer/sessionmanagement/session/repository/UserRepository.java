package com.redisdockerizer.sessionmanagement.session.repository;

import com.redisdockerizer.sessionmanagement.session.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository provides CRUD operations for managing User entities in a Redis data store.
 * It extends the CrudRepository interface from Spring Data, with User as the domain type and
 * String as the ID type.
 * <p>
 * This repository interface is used to interact with and manage User entities, allowing
 * for basic CRUD (Create, Read, Update, Delete) operations, as well as additional query
 * methods that can be defined if needed.
 * <p>
 * Annotations:
 * - {@code @Repository}: Indicates that this interface is a Spring Data repository, which
 *   is a specialization of the Component annotation, enabling Spring to detect it during classpath scanning.
 * <p>
 * Usage:
 * This interface is automatically implemented by Spring Data at runtime, so you do not
 * need to provide an implementation. Instances of the repository can be injected and used
 * in service or controller classes.
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {
}