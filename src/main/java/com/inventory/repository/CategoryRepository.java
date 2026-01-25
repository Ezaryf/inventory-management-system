package com.inventory.repository;
import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :id")
    Optional<Category> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
