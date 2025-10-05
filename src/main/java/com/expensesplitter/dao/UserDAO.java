package com.expensesplitter.dao;

import com.expensesplitter.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity operations.
 * Defines the contract for user data persistence and retrieval.
 */
public interface UserDAO {
    
    /**
     * Saves a new user to the database
     */
    User save(User user);
    
    /**
     * Updates an existing user in the database
     */
    User update(User user);
    
    /**
     * Deletes a user by ID
     */
    void delete(Long userId);
    
    /**
     * Finds a user by ID
     */
    Optional<User> findById(Long userId);
    
    /**
     * Finds a user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Searches users by username or email containing the query string
     */
    List<User> searchUsers(String query);
    
    /**
     * Gets all active users
     */
    List<User> findAllActive();
    
    /**
     * Gets all users (including inactive)
     */
    List<User> findAll();
    
    /**
     * Checks if a username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Checks if an email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Gets users by IDs
     */
    List<User> findByIds(List<Long> userIds);
}