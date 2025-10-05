package com.expensesplitter.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user in the expense splitter application.
 * This class follows Java OOP principles with proper encapsulation,
 * validation, and defensive copying where needed.
 */
public class User {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String passwordHash;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    // Default constructor
    public User() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public User(String username, String email, String fullName, String passwordHash) {
        this();
        setUsername(username);
        setEmail(email);
        setFullName(fullName);
        setPasswordHash(passwordHash);
    }

    // Full constructor
    public User(Long id, String username, String email, String fullName, 
                String passwordHash, String phoneNumber, LocalDateTime createdAt, 
                LocalDateTime updatedAt, boolean isActive) {
        this.id = id;
        setUsername(username);
        setEmail(email);
        setFullName(fullName);
        setPasswordHash(passwordHash);
        setPhoneNumber(phoneNumber);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.isActive = isActive;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt != null ? createdAt : null;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt != null ? updatedAt : null;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters with validation
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        this.username = username.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email cannot exceed 100 characters");
        }
        this.email = email.trim().toLowerCase();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("Full name cannot exceed 100 characters");
        }
        this.fullName = fullName.trim();
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = passwordHash;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (phoneNumber.length() > 20) {
                throw new IllegalArgumentException("Phone number cannot exceed 20 characters");
            }
            this.phoneNumber = phoneNumber.trim();
        } else {
            this.phoneNumber = null;
        }
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Business methods
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public String getDisplayName() {
        return fullName != null && !fullName.isEmpty() ? fullName : username;
    }

    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return username.substring(0, Math.min(2, username.length())).toUpperCase();
        }
        
        String[] names = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        for (int i = 0; i < Math.min(2, names.length); i++) {
            if (!names[i].isEmpty()) {
                initials.append(names[i].charAt(0));
            }
        }
        
        return initials.toString().toUpperCase();
    }

    // Helper methods
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Basic email validation regex
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return Objects.equals(id, user.id) &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }

    // Builder pattern for complex object creation
    public static class Builder {
        private User user = new User();

        public Builder withId(Long id) {
            user.setId(id);
            return this;
        }

        public Builder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public Builder withEmail(String email) {
            user.setEmail(email);
            return this;
        }

        public Builder withFullName(String fullName) {
            user.setFullName(fullName);
            return this;
        }

        public Builder withPasswordHash(String passwordHash) {
            user.setPasswordHash(passwordHash);
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            user.setPhoneNumber(phoneNumber);
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            user.setCreatedAt(createdAt);
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            user.setUpdatedAt(updatedAt);
            return this;
        }

        public Builder withActive(boolean active) {
            user.setActive(active);
            return this;
        }

        public User build() {
            // Validate required fields
            if (user.username == null || user.email == null || 
                user.fullName == null || user.passwordHash == null) {
                throw new IllegalStateException("Username, email, full name, and password hash are required");
            }
            return user;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}