package com.expensesplitter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an expense in the expense splitter application.
 * Contains all information about a single expense including who paid,
 * amount, category, and splitting details.
 */
public class Expense {
    private Long id;
    private Long groupId;
    private Long paidByUserId;
    private String description;
    private BigDecimal amount;
    private String currency;
    private ExpenseCategory category;
    private SplitType splitType;
    private LocalDateTime expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private String receiptImagePath;
    private boolean isSettled;

    // Default constructor
    public Expense() {
        this.currency = "USD";
        this.splitType = SplitType.EQUAL;
        this.expenseDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isSettled = false;
    }

    // Constructor with required fields
    public Expense(Long groupId, Long paidByUserId, String description, BigDecimal amount) {
        this();
        this.groupId = groupId;
        this.paidByUserId = paidByUserId;
        setDescription(description);
        setAmount(amount);
    }

    // Full constructor
    public Expense(Long id, Long groupId, Long paidByUserId, String description, 
                   BigDecimal amount, String currency, ExpenseCategory category, 
                   SplitType splitType, LocalDateTime expenseDate, 
                   LocalDateTime createdAt, LocalDateTime updatedAt, 
                   String notes, String receiptImagePath, boolean isSettled) {
        this.id = id;
        this.groupId = groupId;
        this.paidByUserId = paidByUserId;
        setDescription(description);
        setAmount(amount);
        setCurrency(currency);
this.category = category != null ? category : new ExpenseCategory("Other");
        this.splitType = splitType != null ? splitType : SplitType.EQUAL;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDateTime.now();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.notes = notes;
        this.receiptImagePath = receiptImagePath;
        this.isSettled = isSettled;
    }

    // Getters
    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public Long getPaidByUserId() { return paidByUserId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public ExpenseCategory getCategory() { return category; }
    public SplitType getSplitType() { return splitType; }
    public LocalDateTime getExpenseDate() { return expenseDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getNotes() { return notes; }
    public String getReceiptImagePath() { return receiptImagePath; }
    public boolean isSettled() { return isSettled; }

    // Setters with validation
    public void setId(Long id) { this.id = id; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }
        this.description = description.trim();
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        if (currency != null && !currency.trim().isEmpty()) {
            if (currency.length() != 3) {
                throw new IllegalArgumentException("Currency code must be 3 characters");
            }
            this.currency = currency.toUpperCase().trim();
        } else {
            this.currency = "USD";
        }
    }

    public void setCategory(ExpenseCategory category) {
this.category = category != null ? category : new ExpenseCategory("Other");
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType != null ? splitType : SplitType.EQUAL;
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate != null ? expenseDate : LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void setNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new IllegalArgumentException("Notes cannot exceed 1000 characters");
        }
        this.notes = notes;
    }

    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }

    public void setSettled(boolean settled) {
        this.isSettled = settled;
        if (settled) {
            updateTimestamp();
        }
    }

    // Business methods
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSettled() {
        this.isSettled = true;
        updateTimestamp();
    }

    public void markAsUnsettled() {
        this.isSettled = false;
        updateTimestamp();
    }

    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Expense expense = (Expense) obj;
        return Objects.equals(id, expense.id) &&
               Objects.equals(groupId, expense.groupId) &&
               Objects.equals(description, expense.description) &&
               Objects.equals(amount, expense.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, description, amount);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", paidByUserId=" + paidByUserId +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", category=" + category +
                ", splitType=" + splitType +
                ", expenseDate=" + expenseDate +
                ", isSettled=" + isSettled +
                '}';
    }

    // Builder pattern
    public static class Builder {
        private Expense expense = new Expense();

        public Builder withId(Long id) {
            expense.setId(id);
            return this;
        }

        public Builder withGroupId(Long groupId) {
            expense.setGroupId(groupId);
            return this;
        }

        public Builder withPaidByUserId(Long paidByUserId) {
            expense.setPaidByUserId(paidByUserId);
            return this;
        }

        public Builder withDescription(String description) {
            expense.setDescription(description);
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            expense.setAmount(amount);
            return this;
        }

        public Builder withCurrency(String currency) {
            expense.setCurrency(currency);
            return this;
        }

        public Builder withCategory(ExpenseCategory category) {
            expense.setCategory(category);
            return this;
        }

        public Builder withSplitType(SplitType splitType) {
            expense.setSplitType(splitType);
            return this;
        }

        public Builder withExpenseDate(LocalDateTime expenseDate) {
            expense.setExpenseDate(expenseDate);
            return this;
        }

        public Builder withNotes(String notes) {
            expense.setNotes(notes);
            return this;
        }

        public Builder withReceiptImagePath(String receiptImagePath) {
            expense.setReceiptImagePath(receiptImagePath);
            return this;
        }

        public Builder withSettled(boolean settled) {
            expense.setSettled(settled);
            return this;
        }

        public Expense build() {
            // Validate required fields
            if (expense.groupId == null || expense.paidByUserId == null || 
                expense.description == null || expense.amount == null) {
                throw new IllegalStateException("Group ID, paid by user ID, description, and amount are required");
            }
            return expense;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}