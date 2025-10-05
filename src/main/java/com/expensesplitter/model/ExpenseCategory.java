package com.expensesplitter.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an expense category for organizing expenses.
 */
public class ExpenseCategory {
    private Long id;
    private String categoryName;
    private String description;
    private String colorCode;
    private String icon;
    private boolean isDefault;
    private LocalDateTime createdAt;

    // Default constructor
    public ExpenseCategory() {
        this.isDefault = false;
        this.createdAt = LocalDateTime.now();
        this.colorCode = "#007bff"; // Default blue color
    }

    // Constructor with required fields
    public ExpenseCategory(String categoryName) {
        this();
        setCategoryName(categoryName);
    }

    // Full constructor
    public ExpenseCategory(Long id, String categoryName, String description,
                          String colorCode, String icon, boolean isDefault,
                          LocalDateTime createdAt) {
        this.id = id;
        setCategoryName(categoryName);
        setDescription(description);
        setColorCode(colorCode);
        setIcon(icon);
        this.isDefault = isDefault;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters with validation
    public void setId(Long id) {
        this.id = id;
    }

    public void setCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (categoryName.length() > 50) {
            throw new IllegalArgumentException("Category name cannot exceed 50 characters");
        }
        this.categoryName = categoryName.trim();
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("Description cannot exceed 200 characters");
        }
        this.description = description != null ? description.trim() : null;
    }

    public void setColorCode(String colorCode) {
        if (colorCode != null && !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Color code must be in hex format (#RRGGBB)");
        }
        this.colorCode = colorCode != null ? colorCode : "#007bff";
    }

    public void setIcon(String icon) {
        if (icon != null && icon.length() > 50) {
            throw new IllegalArgumentException("Icon cannot exceed 50 characters");
        }
        this.icon = icon != null ? icon.trim() : null;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    public String getDisplayName() {
        return categoryName;
    }

    public String getDisplayIcon() {
        return icon != null ? icon : "📦"; // Default icon
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ExpenseCategory that = (ExpenseCategory) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryName);
    }

    @Override
    public String toString() {
        return "ExpenseCategory{" +
                "id=" + id +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", icon='" + icon + '\'' +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }

    // Builder pattern
    public static class Builder {
        private ExpenseCategory category = new ExpenseCategory();

        public Builder withId(Long id) {
            category.setId(id);
            return this;
        }

        public Builder withCategoryName(String categoryName) {
            category.setCategoryName(categoryName);
            return this;
        }

        public Builder withDescription(String description) {
            category.setDescription(description);
            return this;
        }

        public Builder withColorCode(String colorCode) {
            category.setColorCode(colorCode);
            return this;
        }

        public Builder withIcon(String icon) {
            category.setIcon(icon);
            return this;
        }

        public Builder withDefault(boolean isDefault) {
            category.setDefault(isDefault);
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            category.setCreatedAt(createdAt);
            return this;
        }

        public ExpenseCategory build() {
            if (category.categoryName == null) {
                throw new IllegalStateException("Category name is required");
            }
            return category;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}