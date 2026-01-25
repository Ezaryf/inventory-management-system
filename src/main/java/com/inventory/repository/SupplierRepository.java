package com.inventory.repository;
import com.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.email) = LOWER(:email) AND s.id != :id")
    Optional<Supplier> findByEmailIgnoreCaseAndIdNot(String email, Long id);
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.companyName) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Supplier> findByCompanyNameContainingIgnoreCase(String name);
}
