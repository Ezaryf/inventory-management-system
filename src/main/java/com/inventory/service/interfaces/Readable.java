package com.inventory.service.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for read operations (ISP - Interface Segregation
 * Principle).
 * 
 * @param <T>  Entity type
 * @param <ID> ID type
 */
public interface Readable<T, ID> {

    /**
     * Find entity by ID.
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities.
     */
    List<T> findAll();

    /**
     * Check if entity exists by ID.
     */
    boolean existsById(ID id);
}
