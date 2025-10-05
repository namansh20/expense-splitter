package com.expensesplitter.service;

import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user operations including authentication,
 * registration, profile management, and user search functionality.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserDAO userDAO;
    private User currentUser;
    
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Registers a new user with encrypted password
     */
    public User registerUser(String username, String email, String fullName, String password) {
        logger.info("Registering new user: {}", username);
        
        // Check if username or email already exists
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Hash the password
        String passwordHash = hashPassword(password);
        
        User user = User.builder()
                .withUsername(username)
                .withEmail(email)
                .withFullName(fullName)
                .withPasswordHash(passwordHash)
                .withActive(true)
                .build();
        
        User savedUser = userDAO.save(user);
        logger.info("User registered successfully: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Authenticates a user with username/email and password
     */
    public Optional<User> authenticate(String usernameOrEmail, String password) {
        logger.info("Authenticating user: {}", usernameOrEmail);
        
        User user = userDAO.findByUsername(usernameOrEmail)
                .or(() -> userDAO.findByEmail(usernameOrEmail))
                .orElse(null);
        
        if (user != null && user.isActive() && verifyPassword(password, user.getPasswordHash())) {
            this.currentUser = user;
            logger.info("User authenticated successfully: {}", user.getId());
            return Optional.of(user);
        }
        
        logger.warn("Authentication failed for user: {}", usernameOrEmail);
        return Optional.empty();
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getId());
            this.currentUser = null;
        }
    }
    
    /**
     * Gets the currently logged in user
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
    
    /**
     * Updates user profile information
     */
    public User updateProfile(Long userId, String fullName, String email, String phoneNumber) {
        logger.info("Updating profile for user: {}", userId);
        
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if email is changing and new email doesn't exist
        if (!user.getEmail().equals(email)) {
            if (userDAO.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already exists");
            }
        }
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.updateTimestamp();
        
        User updatedUser = userDAO.update(user);
        
        // Update current user if it's the same
        if (currentUser != null && currentUser.getId().equals(userId)) {
            this.currentUser = updatedUser;
        }
        
        logger.info("Profile updated successfully for user: {}", userId);
        return updatedUser;
    }
    
    /**
     * Changes user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", userId);
        
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        String newPasswordHash = hashPassword(newPassword);
        user.setPasswordHash(newPasswordHash);
        user.updateTimestamp();
        
        userDAO.update(user);
        logger.info("Password changed successfully for user: {}", userId);
    }
    
    /**
     * Searches for users by username or email
     */
    public List<User> searchUsers(String query) {
        logger.debug("Searching users with query: {}", query);
        return userDAO.searchUsers(query);
    }
    
    /**
     * Gets user by ID
     */
    public Optional<User> getUserById(Long userId) {
        return userDAO.findById(userId);
    }
    
    /**
     * Gets user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    /**
     * Gets user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }
    
    /**
     * Deactivates a user account
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user: {}", userId);
        
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.deactivate();
        userDAO.update(user);
        
        // Logout if current user is being deactivated
        if (currentUser != null && currentUser.getId().equals(userId)) {
            logout();
        }
        
        logger.info("User deactivated: {}", userId);
    }
    
    /**
     * Activates a user account
     */
    public void activateUser(Long userId) {
        logger.info("Activating user: {}", userId);
        
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.activate();
        userDAO.update(user);
        
        logger.info("User activated: {}", userId);
    }
    
    /**
     * Gets all active users
     */
    public List<User> getAllActiveUsers() {
        return userDAO.findAllActive();
    }
    
    /**
     * Checks if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Validates user session and refreshes current user data
     */
    public boolean validateSession() {
        if (currentUser == null) {
            return false;
        }
        
        Optional<User> refreshedUser = userDAO.findById(currentUser.getId());
        if (refreshedUser.isPresent() && refreshedUser.get().isActive()) {
            this.currentUser = refreshedUser.get();
            return true;
        } else {
            logout();
            return false;
        }
    }
    
    // Private helper methods
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            
            // Combine salt and hashed password
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            
            // Extract salt and hash
            byte[] salt = new byte[16];
            byte[] storedHashBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, storedHashBytes, 0, storedHashBytes.length);
            
            // Hash the provided password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            
            // Compare the hashes
            return MessageDigest.isEqual(hashedPassword, storedHashBytes);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
}