package com.expensesplitter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an individual share of an expense for a specific user.
 * This model tracks how much each user owes or is owed for a particular expense.
 */
public class ExpenseShare {
    private Long id;
    private Long expenseId;
    private Long userId;
    private BigDecimal shareAmount;
    private BigDecimal percentage;
    private boolean isPaid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ExpenseShare() {
        this.isPaid = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public ExpenseShare(Long expenseId, Long userId, BigDecimal shareAmount) {
        this();
        this.expenseId = expenseId;
        this.userId = userId;
        setShareAmount(shareAmount);
    }

    // Full constructor
    public ExpenseShare(Long id, Long expenseId, Long userId, BigDecimal shareAmount,
                       BigDecimal percentage, boolean isPaid, LocalDateTime paidAt,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.expenseId = expenseId;
        this.userId = userId;
        setShareAmount(shareAmount);
        setPercentage(percentage);
        this.isPaid = isPaid;
        this.paidAt = paidAt;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getExpenseId() { return expenseId; }
    public Long getUserId() { return userId; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public BigDecimal getPercentage() { return percentage; }
    public boolean isPaid() { return isPaid; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters with validation
    public void setId(Long id) { this.id = id; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public void setShareAmount(BigDecimal shareAmount) {
        if (shareAmount == null || shareAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Share amount cannot be null or negative");
        }
        this.shareAmount = shareAmount;
    }

    public void setPercentage(BigDecimal percentage) {
        if (percentage != null) {
            if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Percentage must be between 0 and 100");
            }
        }
        this.percentage = percentage;
    }

    public void setPaid(boolean paid) {
        this.isPaid = paid;
        if (paid && this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
        } else if (!paid) {
            this.paidAt = null;
        }
        updateTimestamp();
    }

    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPaid() {
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
        updateTimestamp();
    }

    public void markAsUnpaid() {
        this.isPaid = false;
        this.paidAt = null;
        updateTimestamp();
    }

    public String getFormattedShareAmount() {
        return String.format("%.2f", shareAmount);
    }

    public String getFormattedPercentage() {
        return percentage != null ? String.format("%.1f%%", percentage) : "0.0%";
    }

    public boolean isOwedMoney() {
        return shareAmount != null && shareAmount.compareTo(BigDecimal.ZERO) > 0 && !isPaid;
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ExpenseShare that = (ExpenseShare) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(expenseId, that.expenseId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, expenseId, userId);
    }

    @Override
    public String toString() {
        return "ExpenseShare{" +
                "id=" + id +
                ", expenseId=" + expenseId +
                ", userId=" + userId +
                ", shareAmount=" + shareAmount +
                ", percentage=" + percentage +
                ", isPaid=" + isPaid +
                ", paidAt=" + paidAt +
                '}';
    }

    // Builder pattern
    public static class Builder {
        private ExpenseShare expenseShare = new ExpenseShare();

        public Builder withId(Long id) {
            expenseShare.setId(id);
            return this;
        }

        public Builder withExpenseId(Long expenseId) {
            expenseShare.setExpenseId(expenseId);
            return this;
        }

        public Builder withUserId(Long userId) {
            expenseShare.setUserId(userId);
            return this;
        }

        public Builder withShareAmount(BigDecimal shareAmount) {
            expenseShare.setShareAmount(shareAmount);
            return this;
        }

        public Builder withPercentage(BigDecimal percentage) {
            expenseShare.setPercentage(percentage);
            return this;
        }

        public Builder withPaid(boolean paid) {
            expenseShare.setPaid(paid);
            return this;
        }

        public Builder withPaidAt(LocalDateTime paidAt) {
            expenseShare.setPaidAt(paidAt);
            return this;
        }

        public ExpenseShare build() {
            // Validate required fields
            if (expenseShare.expenseId == null || expenseShare.userId == null || 
                expenseShare.shareAmount == null) {
                throw new IllegalStateException("Expense ID, user ID, and share amount are required");
            }
            return expenseShare;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}