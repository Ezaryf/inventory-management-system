package com.inventory.service.interfaces;
import java.util.List;
import java.util.Optional;
public interface Readable<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean existsById(ID id);
}
