package com.inventory.service.interfaces;

/**
 * Generic interface for write operations (ISP - Interface Segregation
 * Principle).
 * 
 * @param <T>  Entity type
 * @param <ID> ID type
 */
public interface Writable<T, ID> {

    /**
     * Save a new entity.
     */
    T save(T entity);

    /**
     * Update an existing entity.
     */
    T update(ID id, T entity);

    /**
     * Delete entity by ID.
     */
    void deleteById(ID id);
}
