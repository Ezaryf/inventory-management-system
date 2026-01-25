package com.inventory.service.interfaces;
public interface Writable<T, ID> {
    T save(T entity);
    T update(ID id, T entity);
    void deleteById(ID id);
}
