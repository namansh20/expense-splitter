package com.expensesplitter.service;

import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.ExpenseShareDAO;
import com.expensesplitter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing expenses including creation, splitting,
 * settlement, and analytics operations.
 */
public class ExpenseService {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);
    
    private final ExpenseDAO expenseDAO;
    private final ExpenseShareDAO expenseShareDAO;
    private final ExpenseSplittingService splittingService;
    
    public ExpenseService(ExpenseDAO expenseDAO, ExpenseShareDAO expenseShareDAO, 
                         ExpenseSplittingService splittingService) {
        this.expenseDAO = expenseDAO;
        this.expenseShareDAO = expenseShareDAO;
        this.splittingService = splittingService;
    }
    
    /**
     * Creates a new expense and splits it among group members
     */
    public Expense createExpense(Long groupId, Long paidByUserId, String description, 
                                BigDecimal amount, String currency, ExpenseCategory category,
                                SplitType splitType, List<Long> participantUserIds, 
                                Map<Long, BigDecimal> customShares, String notes) {
        logger.info("Creating expense: {} for group: {}", description, groupId);
        
        // Create the expense
        Expense expense = Expense.builder()
                .withGroupId(groupId)
                .withPaidByUserId(paidByUserId)
                .withDescription(description)
                .withAmount(amount)
                .withCurrency(currency)
                .withCategory(category)
                .withSplitType(splitType)
                .withNotes(notes)
                .build();
        
        Expense savedExpense = expenseDAO.save(expense);
        
        // Calculate and create expense shares
        List<ExpenseShare> shares = splittingService.splitExpense(
                savedExpense, participantUserIds, customShares);
        
        for (ExpenseShare share : shares) {
            expenseShareDAO.save(share);
        }
        
        logger.info("Expense created successfully: {}", savedExpense.getId());
        return savedExpense;
    }
    
    /**
     * Updates an existing expense
     */
    public Expense updateExpense(Long expenseId, String description, BigDecimal amount,
                                String currency, ExpenseCategory category, String notes) {
        logger.info("Updating expense: {}", expenseId);
        
        Expense expense = expenseDAO.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setCategory(category);
        expense.setNotes(notes);
        expense.updateTimestamp();
        
        Expense updatedExpense = expenseDAO.update(expense);
        
        // Recalculate expense shares if amount changed
        List<ExpenseShare> existingShares = expenseShareDAO.findByExpenseId(expenseId);
        if (!existingShares.isEmpty()) {
            recalculateShares(updatedExpense, existingShares);
        }
        
        logger.info("Expense updated successfully: {}", expenseId);
        return updatedExpense;
    }
    
    /**
     * Deletes an expense and all associated shares
     */
    public void deleteExpense(Long expenseId) {
        logger.info("Deleting expense: {}", expenseId);
        
        // Delete all expense shares first
        expenseShareDAO.deleteByExpenseId(expenseId);
        
        // Delete the expense
        expenseDAO.delete(expenseId);
        
        logger.info("Expense deleted successfully: {}", expenseId);
    }
    
    /**
     * Gets expense by ID with shares
     */
    public Optional<Expense> getExpenseById(Long expenseId) {
        return expenseDAO.findById(expenseId);
    }
    
    /**
     * Gets all expenses for a group
     */
    public List<Expense> getExpensesByGroup(Long groupId) {
        return expenseDAO.findByGroupId(groupId);
    }
    
    /**
     * Gets expenses for a user (either paid by or shared with)
     */
    public List<Expense> getExpensesByUser(Long userId) {
        List<Expense> paidExpenses = expenseDAO.findByPaidByUserId(userId);
        List<ExpenseShare> userShares = expenseShareDAO.findByUserId(userId);
        
        Set<Long> sharedExpenseIds = userShares.stream()
                .map(ExpenseShare::getExpenseId)
                .collect(Collectors.toSet());
        
        List<Expense> sharedExpenses = sharedExpenseIds.stream()
                .map(expenseDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        // Combine and deduplicate
        Set<Expense> allExpenses = new HashSet<>(paidExpenses);
        allExpenses.addAll(sharedExpenses);
        
        return new ArrayList<>(allExpenses);
    }
    
    /**
     * Gets expense shares for an expense
     */
    public List<ExpenseShare> getExpenseShares(Long expenseId) {
        return expenseShareDAO.findByExpenseId(expenseId);
    }
    
    /**
     * Gets user's expense shares
     */
    public List<ExpenseShare> getUserExpenseShares(Long userId) {
        return expenseShareDAO.findByUserId(userId);
    }
    
    /**
     * Marks an expense share as paid
     */
    public void markShareAsPaid(Long expenseId, Long userId) {
        logger.info("Marking share as paid - Expense: {}, User: {}", expenseId, userId);
        
        ExpenseShare share = expenseShareDAO.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense share not found"));
        
        share.markAsPaid();
        expenseShareDAO.update(share);
        
        // Check if all shares are paid and mark expense as settled
        checkAndMarkExpenseAsSettled(expenseId);
        
        logger.info("Share marked as paid successfully");
    }
    
    /**
     * Marks an expense share as unpaid
     */
    public void markShareAsUnpaid(Long expenseId, Long userId) {
        logger.info("Marking share as unpaid - Expense: {}, User: {}", expenseId, userId);
        
        ExpenseShare share = expenseShareDAO.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense share not found"));
        
        share.markAsUnpaid();
        expenseShareDAO.update(share);
        
        // Mark expense as unsettled
        Expense expense = expenseDAO.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        if (expense.isSettled()) {
            expense.markAsUnsettled();
            expenseDAO.update(expense);
        }
        
        logger.info("Share marked as unpaid successfully");
    }
    
    /**
     * Gets user balance summary (how much they owe and are owed)
     */
    public UserBalance getUserBalance(Long userId) {
        List<ExpenseShare> userShares = expenseShareDAO.findByUserId(userId);
        
        BigDecimal totalOwed = userShares.stream()
                .filter(share -> !share.isPaid())
                .map(ExpenseShare::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Expense> paidExpenses = expenseDAO.findByPaidByUserId(userId);
        BigDecimal totalPaid = paidExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netBalance = totalPaid.subtract(totalOwed);
        
        return new UserBalance(userId, totalOwed, totalPaid, netBalance);
    }
    
    /**
     * Gets group expense statistics
     */
    public GroupExpenseStats getGroupExpenseStats(Long groupId) {
        List<Expense> expenses = expenseDAO.findByGroupId(groupId);
        
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long settledCount = expenses.stream()
                .mapToLong(expense -> expense.isSettled() ? 1 : 0)
                .sum();
        
        Map<ExpenseCategory, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.mapping(Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        
        return new GroupExpenseStats(groupId, totalAmount, expenses.size(), 
                                   (int) settledCount, categoryTotals);
    }
    
    /**
     * Gets expenses within date range
     */
    public List<Expense> getExpensesByDateRange(Long groupId, LocalDateTime startDate, 
                                               LocalDateTime endDate) {
        return expenseDAO.findByGroupIdAndDateRange(groupId, startDate, endDate);
    }
    
    /**
     * Gets expenses by category
     */
    public List<Expense> getExpensesByCategory(Long groupId, ExpenseCategory category) {
        return expenseDAO.findByGroupIdAndCategory(groupId, category);
    }
    
    /**
     * Gets recent expenses for a user
     */
    public List<Expense> getRecentExpenses(Long userId, int limit) {
        return expenseDAO.findRecentByUserId(userId, limit);
    }
    
    /**
     * Searches expenses by description
     */
    public List<Expense> searchExpenses(Long groupId, String query) {
        return expenseDAO.searchByDescription(groupId, query);
    }
    
    // Private helper methods
    private void recalculateShares(Expense expense, List<ExpenseShare> existingShares) {
        // Get the user IDs and calculate new shares
        List<Long> userIds = existingShares.stream()
                .map(ExpenseShare::getUserId)
                .collect(Collectors.toList());
        
        List<ExpenseShare> newShares = splittingService.splitExpense(
                expense, userIds, Collections.emptyMap());
        
        // Update existing shares with new amounts
        for (int i = 0; i < existingShares.size(); i++) {
            ExpenseShare existingShare = existingShares.get(i);
            ExpenseShare newShare = newShares.stream()
                    .filter(s -> s.getUserId().equals(existingShare.getUserId()))
                    .findFirst()
                    .orElse(null);
            
            if (newShare != null) {
                existingShare.setShareAmount(newShare.getShareAmount());
                existingShare.setPercentage(newShare.getPercentage());
                existingShare.updateTimestamp();
                expenseShareDAO.update(existingShare);
            }
        }
    }
    
    private void checkAndMarkExpenseAsSettled(Long expenseId) {
        List<ExpenseShare> shares = expenseShareDAO.findByExpenseId(expenseId);
        boolean allPaid = shares.stream().allMatch(ExpenseShare::isPaid);
        
        if (allPaid) {
            Expense expense = expenseDAO.findById(expenseId)
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
            
            if (!expense.isSettled()) {
                expense.markAsSettled();
                expenseDAO.update(expense);
                logger.info("Expense marked as settled: {}", expenseId);
            }
        }
    }
    
    // Inner classes for return types
    public static class UserBalance {
        private final Long userId;
        private final BigDecimal totalOwed;
        private final BigDecimal totalPaid;
        private final BigDecimal netBalance;
        
        public UserBalance(Long userId, BigDecimal totalOwed, BigDecimal totalPaid, BigDecimal netBalance) {
            this.userId = userId;
            this.totalOwed = totalOwed;
            this.totalPaid = totalPaid;
            this.netBalance = netBalance;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public BigDecimal getTotalOwed() { return totalOwed; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public BigDecimal getNetBalance() { return netBalance; }
        
        public boolean owesMore() { return netBalance.compareTo(BigDecimal.ZERO) < 0; }
        public boolean isOwed() { return netBalance.compareTo(BigDecimal.ZERO) > 0; }
        public boolean isEven() { return netBalance.compareTo(BigDecimal.ZERO) == 0; }
    }
    
    public static class GroupExpenseStats {
        private final Long groupId;
        private final BigDecimal totalAmount;
        private final int totalExpenses;
        private final int settledExpenses;
        private final Map<ExpenseCategory, BigDecimal> categoryTotals;
        
        public GroupExpenseStats(Long groupId, BigDecimal totalAmount, int totalExpenses,
                               int settledExpenses, Map<ExpenseCategory, BigDecimal> categoryTotals) {
            this.groupId = groupId;
            this.totalAmount = totalAmount;
            this.totalExpenses = totalExpenses;
            this.settledExpenses = settledExpenses;
            this.categoryTotals = categoryTotals;
        }
        
        // Getters
        public Long getGroupId() { return groupId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public int getTotalExpenses() { return totalExpenses; }
        public int getSettledExpenses() { return settledExpenses; }
        public int getPendingExpenses() { return totalExpenses - settledExpenses; }
        public Map<ExpenseCategory, BigDecimal> getCategoryTotals() { return categoryTotals; }
        
        public double getSettlementPercentage() {
            return totalExpenses > 0 ? (double) settledExpenses / totalExpenses * 100 : 0;
        }
    }
}