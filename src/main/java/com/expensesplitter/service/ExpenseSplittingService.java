package com.expensesplitter.service;

import com.expensesplitter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for implementing various expense splitting algorithms
 * including equal split, percentage-based split, custom amounts, and debt optimization.
 */
public class ExpenseSplittingService {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseSplittingService.class);
    private static final int SCALE = 2; // Decimal places for currency calculations
    
    /**
     * Splits an expense based on the specified split type
     */
    public List<ExpenseShare> splitExpense(Expense expense, List<Long> participantUserIds, 
                                          Map<Long, BigDecimal> customShares) {
        logger.debug("Splitting expense {} of type {}", expense.getId(), expense.getSplitType());
        
        switch (expense.getSplitType()) {
            case EQUAL:
                return splitEqual(expense, participantUserIds);
            case PERCENTAGE:
                return splitByPercentage(expense, customShares);
            case CUSTOM:
                return splitCustom(expense, customShares);
            case BY_SHARES:
                return splitByShares(expense, customShares);
            default:
                throw new IllegalArgumentException("Unsupported split type: " + expense.getSplitType());
        }
    }
    
    /**
     * Splits expense equally among all participants
     */
    public List<ExpenseShare> splitEqual(Expense expense, List<Long> participantUserIds) {
        if (participantUserIds.isEmpty()) {
            throw new IllegalArgumentException("No participants provided for expense split");
        }
        
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal shareAmount = totalAmount.divide(
                BigDecimal.valueOf(participantUserIds.size()), SCALE, RoundingMode.HALF_UP);
        
        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        
        // Calculate shares for all but the last participant
        for (int i = 0; i < participantUserIds.size() - 1; i++) {
            Long userId = participantUserIds.get(i);
            ExpenseShare share = createExpenseShare(expense.getId(), userId, shareAmount, 
                    calculatePercentage(shareAmount, totalAmount));
            shares.add(share);
            allocatedAmount = allocatedAmount.add(shareAmount);
        }
        
        // Last participant gets the remainder to handle rounding
        Long lastUserId = participantUserIds.get(participantUserIds.size() - 1);
        BigDecimal lastShare = totalAmount.subtract(allocatedAmount);
        ExpenseShare lastExpenseShare = createExpenseShare(expense.getId(), lastUserId, lastShare,
                calculatePercentage(lastShare, totalAmount));
        shares.add(lastExpenseShare);
        
        logger.debug("Split {} equally among {} participants", totalAmount, participantUserIds.size());
        return shares;
    }
    
    /**
     * Splits expense by custom percentages
     */
    public List<ExpenseShare> splitByPercentage(Expense expense, Map<Long, BigDecimal> percentageShares) {
        if (percentageShares.isEmpty()) {
            throw new IllegalArgumentException("No percentage shares provided");
        }
        
        // Validate percentages sum to 100
        BigDecimal totalPercentage = percentageShares.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("Percentages must sum to 100%");
        }
        
        BigDecimal totalAmount = expense.getAmount();
        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        
        List<Map.Entry<Long, BigDecimal>> entries = new ArrayList<>(percentageShares.entrySet());
        
        // Calculate shares for all but the last participant
        for (int i = 0; i < entries.size() - 1; i++) {
            Map.Entry<Long, BigDecimal> entry = entries.get(i);
            Long userId = entry.getKey();
            BigDecimal percentage = entry.getValue();
            
            BigDecimal shareAmount = totalAmount.multiply(percentage)
                    .divide(new BigDecimal("100"), SCALE, RoundingMode.HALF_UP);
            
            ExpenseShare share = createExpenseShare(expense.getId(), userId, shareAmount, percentage);
            shares.add(share);
            allocatedAmount = allocatedAmount.add(shareAmount);
        }
        
        // Last participant gets the remainder to handle rounding
        Map.Entry<Long, BigDecimal> lastEntry = entries.get(entries.size() - 1);
        BigDecimal lastShare = totalAmount.subtract(allocatedAmount);
        ExpenseShare lastExpenseShare = createExpenseShare(expense.getId(), lastEntry.getKey(), 
                lastShare, lastEntry.getValue());
        shares.add(lastExpenseShare);
        
        logger.debug("Split {} by percentages among {} participants", totalAmount, percentageShares.size());
        return shares;
    }
    
    /**
     * Splits expense by custom amounts
     */
    public List<ExpenseShare> splitCustom(Expense expense, Map<Long, BigDecimal> customAmounts) {
        if (customAmounts.isEmpty()) {
            throw new IllegalArgumentException("No custom amounts provided");
        }
        
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal sumCustomAmounts = customAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sumCustomAmounts.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("Custom amounts must sum to total expense amount");
        }
        
        List<ExpenseShare> shares = customAmounts.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    BigDecimal shareAmount = entry.getValue();
                    BigDecimal percentage = calculatePercentage(shareAmount, totalAmount);
                    
                    return createExpenseShare(expense.getId(), userId, shareAmount, percentage);
                })
                .collect(Collectors.toList());
        
        logger.debug("Split {} by custom amounts among {} participants", totalAmount, customAmounts.size());
        return shares;
    }
    
    /**
     * Splits expense by shares (ratios)
     */
    public List<ExpenseShare> splitByShares(Expense expense, Map<Long, BigDecimal> shareRatios) {
        if (shareRatios.isEmpty()) {
            throw new IllegalArgumentException("No share ratios provided");
        }
        
        BigDecimal totalShares = shareRatios.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalShares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total shares must be greater than zero");
        }
        
        BigDecimal totalAmount = expense.getAmount();
        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        
        List<Map.Entry<Long, BigDecimal>> entries = new ArrayList<>(shareRatios.entrySet());
        
        // Calculate shares for all but the last participant
        for (int i = 0; i < entries.size() - 1; i++) {
            Map.Entry<Long, BigDecimal> entry = entries.get(i);
            Long userId = entry.getKey();
            BigDecimal userShares = entry.getValue();
            
            BigDecimal shareAmount = totalAmount.multiply(userShares)
                    .divide(totalShares, SCALE, RoundingMode.HALF_UP);
            BigDecimal percentage = calculatePercentage(shareAmount, totalAmount);
            
            ExpenseShare share = createExpenseShare(expense.getId(), userId, shareAmount, percentage);
            shares.add(share);
            allocatedAmount = allocatedAmount.add(shareAmount);
        }
        
        // Last participant gets the remainder to handle rounding
        Map.Entry<Long, BigDecimal> lastEntry = entries.get(entries.size() - 1);
        BigDecimal lastShare = totalAmount.subtract(allocatedAmount);
        BigDecimal lastPercentage = calculatePercentage(lastShare, totalAmount);
        ExpenseShare lastExpenseShare = createExpenseShare(expense.getId(), lastEntry.getKey(), 
                lastShare, lastPercentage);
        shares.add(lastExpenseShare);
        
        logger.debug("Split {} by shares among {} participants", totalAmount, shareRatios.size());
        return shares;
    }
    
    /**
     * Optimizes debt settlements to minimize number of transactions
     * This implements a simplified debt optimization algorithm
     */
public List<DebtSettlement> optimizeDebts(List<ExpenseService.UserBalance> userBalances) {
        logger.debug("Optimizing debts for {} users", userBalances.size());
        
        List<DebtSettlement> settlements = new ArrayList<>();
        
        // Separate creditors (those who are owed money) and debtors (those who owe money)
List<ExpenseService.UserBalance> creditors = userBalances.stream()
                .filter(balance -> balance.getNetBalance().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getNetBalance().compareTo(a.getNetBalance()))
                .collect(Collectors.toList());
        
List<ExpenseService.UserBalance> debtors = userBalances.stream()
                .filter(balance -> balance.getNetBalance().compareTo(BigDecimal.ZERO) < 0)
                .sorted((a, b) -> a.getNetBalance().compareTo(b.getNetBalance()))
                .collect(Collectors.toList());
        
        // Create mutable copies for calculation
Map<Long, BigDecimal> creditorBalances = creditors.stream()
                .collect(Collectors.toMap(ExpenseService.UserBalance::getUserId, ExpenseService.UserBalance::getNetBalance));
Map<Long, BigDecimal> debtorBalances = debtors.stream()
                .collect(Collectors.toMap(ExpenseService.UserBalance::getUserId, 
                        balance -> balance.getNetBalance().abs()));
        
        // Match debts with credits
        for (Long debtorId : debtorBalances.keySet()) {
            BigDecimal debtAmount = debtorBalances.get(debtorId);
            
            while (debtAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Find the creditor with the highest balance
                Optional<Long> creditorIdOpt = creditorBalances.entrySet().stream()
                        .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey);
                
                if (!creditorIdOpt.isPresent()) {
                    break; // No more creditors
                }
                
                Long creditorId = creditorIdOpt.get();
                BigDecimal creditAmount = creditorBalances.get(creditorId);
                
                // Calculate settlement amount (minimum of debt and credit)
                BigDecimal settlementAmount = debtAmount.min(creditAmount);
                
                // Create settlement
                settlements.add(new DebtSettlement(debtorId, creditorId, settlementAmount));
                
                // Update balances
                debtAmount = debtAmount.subtract(settlementAmount);
                creditorBalances.put(creditorId, creditAmount.subtract(settlementAmount));
            }
            
            debtorBalances.put(debtorId, debtAmount);
        }
        
        logger.debug("Generated {} debt settlements", settlements.size());
        return settlements;
    }
    
    /**
     * Calculates what each user owes to each other user in a group
     */
    public Map<Long, Map<Long, BigDecimal>> calculateUserDebts(Long groupId, 
                                                              List<ExpenseShare> allShares,
                                                              List<Expense> allExpenses) {
        Map<Long, Map<Long, BigDecimal>> userDebts = new HashMap<>();
        
        // Group expenses by who paid
        Map<Long, List<Expense>> expensesByPayer = allExpenses.stream()
                .filter(expense -> expense.getGroupId().equals(groupId))
                .collect(Collectors.groupingBy(Expense::getPaidByUserId));
        
        // Calculate what each user owes to each payer
        for (ExpenseShare share : allShares) {
            if (!share.isPaid()) {
                Expense expense = allExpenses.stream()
                        .filter(e -> e.getId().equals(share.getExpenseId()))
                        .findFirst()
                        .orElse(null);
                
                if (expense != null && expense.getGroupId().equals(groupId)) {
                    Long owerId = share.getUserId();
                    Long payerId = expense.getPaidByUserId();
                    
                    if (!owerId.equals(payerId)) {
                        userDebts.computeIfAbsent(owerId, k -> new HashMap<>())
                                .merge(payerId, share.getShareAmount(), BigDecimal::add);
                    }
                }
            }
        }
        
        return userDebts;
    }
    
    // Helper methods
    private ExpenseShare createExpenseShare(Long expenseId, Long userId, 
                                           BigDecimal shareAmount, BigDecimal percentage) {
        return ExpenseShare.builder()
                .withExpenseId(expenseId)
                .withUserId(userId)
                .withShareAmount(shareAmount)
                .withPercentage(percentage)
                .build();
    }
    
    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(new BigDecimal("100"))
                .divide(total, SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * Inner class representing a debt settlement between two users
     */
    public static class DebtSettlement {
        private final Long fromUserId;
        private final Long toUserId;
        private final BigDecimal amount;
        
        public DebtSettlement(Long fromUserId, Long toUserId, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.toUserId = toUserId;
            this.amount = amount;
        }
        
        public Long getFromUserId() { return fromUserId; }
        public Long getToUserId() { return toUserId; }
        public BigDecimal getAmount() { return amount; }
        
        @Override
        public String toString() {
            return String.format("DebtSettlement{from=%d, to=%d, amount=%.2f}", 
                    fromUserId, toUserId, amount);
        }
    }
}