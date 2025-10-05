package com.expensesplitter.dao;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Expense entity operations.
 * Defines the contract for expense data persistence and retrieval.
 */
public interface ExpenseDAO {
    
    /**
     * Saves a new expense to the database
     */
    Expense save(Expense expense);
    
    /**
     * Updates an existing expense in the database
     */
    Expense update(Expense expense);
    
    /**
     * Deletes an expense by ID
     */
    void delete(Long expenseId);
    
    /**
     * Finds an expense by ID
     */
    Optional<Expense> findById(Long expenseId);
    
    /**
     * Gets all expenses for a group
     */
    List<Expense> findByGroupId(Long groupId);
    
    /**
     * Gets all expenses paid by a specific user
     */
    List<Expense> findByPaidByUserId(Long userId);
    
    /**
     * Gets expenses within a date range for a group
     */
    List<Expense> findByGroupIdAndDateRange(Long groupId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets expenses by category for a group
     */
    List<Expense> findByGroupIdAndCategory(Long groupId, ExpenseCategory category);
    
    /**
     * Gets recent expenses for a user (limited count)
     */
    List<Expense> findRecentByUserId(Long userId, int limit);
    
    /**
     * Searches expenses by description containing the query string
     */
    List<Expense> searchByDescription(Long groupId, String query);
    
    /**
     * Gets all expenses for multiple groups
     */
    List<Expense> findByGroupIds(List<Long> groupIds);
    
    /**
     * Gets settled expenses for a group
     */
    List<Expense> findSettledByGroupId(Long groupId);
    
    /**
     * Gets unsettled expenses for a group
     */
    List<Expense> findUnsettledByGroupId(Long groupId);
    
    /**
     * Gets expenses by IDs
     */
    List<Expense> findByIds(List<Long> expenseIds);
    
    /**
     * Counts total expenses for a group
     */
    long countByGroupId(Long groupId);
    
    /**
     * Gets all expenses (for admin purposes)
     */
    List<Expense> findAll();
}