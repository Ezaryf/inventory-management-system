package com.inventory.repository;

import com.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username.
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);
}
