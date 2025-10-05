package com.expensesplitter.dao;

import com.expensesplitter.model.ExpenseShare;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for ExpenseShare entity operations.
 * Defines the contract for expense share data persistence and retrieval.
 */
public interface ExpenseShareDAO {
    
    /**
     * Saves a new expense share to the database
     */
    ExpenseShare save(ExpenseShare expenseShare);
    
    /**
     * Updates an existing expense share in the database
     */
    ExpenseShare update(ExpenseShare expenseShare);
    
    /**
     * Deletes an expense share by ID
     */
    void delete(Long shareId);
    
    /**
     * Deletes all expense shares for a specific expense
     */
    void deleteByExpenseId(Long expenseId);
    
    /**
     * Finds an expense share by ID
     */
    Optional<ExpenseShare> findById(Long shareId);
    
    /**
     * Gets all expense shares for a specific expense
     */
    List<ExpenseShare> findByExpenseId(Long expenseId);
    
    /**
     * Gets all expense shares for a specific user
     */
    List<ExpenseShare> findByUserId(Long userId);
    
    /**
     * Finds a specific expense share for an expense and user
     */
    Optional<ExpenseShare> findByExpenseIdAndUserId(Long expenseId, Long userId);
    
    /**
     * Gets unpaid expense shares for a user
     */
    List<ExpenseShare> findUnpaidByUserId(Long userId);
    
    /**
     * Gets paid expense shares for a user
     */
    List<ExpenseShare> findPaidByUserId(Long userId);
    
    /**
     * Gets all expense shares for multiple expenses
     */
    List<ExpenseShare> findByExpenseIds(List<Long> expenseIds);
    
    /**
     * Gets expense shares by expense and user IDs
     */
    List<ExpenseShare> findByExpenseIdAndUserIds(Long expenseId, List<Long> userIds);
    
    /**
     * Counts unpaid shares for a user
     */
    long countUnpaidByUserId(Long userId);
    
    /**
     * Gets all expense shares (for admin purposes)
     */
    List<ExpenseShare> findAll();
}