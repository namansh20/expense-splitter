package com.expensesplitter.ui.controller;

import com.expensesplitter.model.User;
import com.expensesplitter.service.UserService;
import com.expensesplitter.util.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the login screen that handles user authentication
 * and registration functionality.
 */
public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    // Login form controls
    @FXML private VBox loginContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button showRegisterButton;
    @FXML private Label loginErrorLabel;
    @FXML private ProgressIndicator loginProgressIndicator;
    @FXML private CheckBox rememberMeCheckBox;
    
    // Registration form controls
    @FXML private VBox registrationContainer;
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regFullNameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button showLoginButton;
    @FXML private Label registrationErrorLabel;
    @FXML private ProgressIndicator registrationProgressIndicator;
    
    // Common controls
    @FXML private Label appTitleLabel;
    @FXML private Label appSubtitleLabel;
    
    private UserService userService;
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupValidation();
        showLoginForm();
        
        // Hide progress indicators initially
        loginProgressIndicator.setVisible(false);
        registrationProgressIndicator.setVisible(false);
        
        // Set initial focus
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    private void setupEventHandlers() {
        // Login form handlers
        loginButton.setOnAction(e -> handleLogin());
        showRegisterButton.setOnAction(e -> showRegistrationForm());
        
        // Registration form handlers
        registerButton.setOnAction(e -> handleRegistration());
        showLoginButton.setOnAction(e -> showLoginForm());
        
        // Enter key handlers
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        
        regUsernameField.setOnAction(e -> regEmailField.requestFocus());
        regEmailField.setOnAction(e -> regFullNameField.requestFocus());
        regFullNameField.setOnAction(e -> regPasswordField.requestFocus());
        regPasswordField.setOnAction(e -> regConfirmPasswordField.requestFocus());
        regConfirmPasswordField.setOnAction(e -> handleRegistration());
    }
    
    private void setupValidation() {
        // Real-time validation for registration form
        regPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswords();
        });
        
        regConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswords();
        });
        
        regEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });
    }
    
    private void validatePasswords() {
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        
        if (!password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                regConfirmPasswordField.setStyle("-fx-border-color: red;");
            } else {
                regConfirmPasswordField.setStyle("");
            }
        } else {
            regConfirmPasswordField.setStyle("");
        }
    }
    
    private void validateEmail() {
        String email = regEmailField.getText();
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                regEmailField.setStyle("-fx-border-color: red;");
            } else {
                regEmailField.setStyle("");
            }
        } else {
            regEmailField.setStyle("");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter both username/email and password");
            return;
        }
        
        // Clear previous error
        clearLoginError();
        
        // Show progress indicator
        setLoginInProgress(true);
        
        // Create background task for authentication
        Task<Optional<User>> loginTask = new Task<Optional<User>>() {
            @Override
            protected Optional<User> call() throws Exception {
                return userService.authenticate(usernameOrEmail, password);
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoginInProgress(false);
                
                Optional<User> userOpt = loginTask.getValue();
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    logger.info("User logged in successfully: {}", user.getUsername());
                    
                    try {
                        // Navigate to dashboard
                        SceneManager.getInstance().showScene("dashboard.fxml");
                    } catch (Exception ex) {
                        logger.error("Error navigating to dashboard", ex);
                        showLoginError("Error loading dashboard: " + ex.getMessage());
                    }
                } else {
                    showLoginError("Invalid username/email or password");
                }
            });
        });
        
        loginTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoginInProgress(false);
                Throwable exception = loginTask.getException();
                logger.error("Login failed", exception);
                showLoginError("Login failed: " + exception.getMessage());
            });
        });
        
        // Run the task in background thread
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    private void handleRegistration() {
        // Validate input fields
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String fullName = regFullNameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        
        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            showRegistrationError("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showRegistrationError("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            showRegistrationError("Password must be at least 6 characters long");
            return;
        }
        
        if (!isValidEmail(email)) {
            showRegistrationError("Please enter a valid email address");
            return;
        }
        
        // Clear previous error
        clearRegistrationError();
        
        // Show progress indicator
        setRegistrationInProgress(true);
        
        // Create background task for registration
        Task<User> registrationTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return userService.registerUser(username, email, fullName, password);
            }
        };
        
        registrationTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setRegistrationInProgress(false);
                
                User user = registrationTask.getValue();
                logger.info("User registered successfully: {}", user.getUsername());
                
                // Show success message and switch to login form
                showRegistrationSuccess("Registration successful! You can now login with your credentials.");
                clearRegistrationForm();
                showLoginForm();
                
                // Pre-fill login form
                usernameField.setText(username);
                passwordField.requestFocus();
            });
        });
        
        registrationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setRegistrationInProgress(false);
                Throwable exception = registrationTask.getException();
                logger.error("Registration failed", exception);
                showRegistrationError("Registration failed: " + exception.getMessage());
            });
        });
        
        // Run the task in background thread
        Thread registrationThread = new Thread(registrationTask);
        registrationThread.setDaemon(true);
        registrationThread.start();
    }
    
    private void showLoginForm() {
        loginContainer.setVisible(true);
        registrationContainer.setVisible(false);
        clearLoginError();
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    private void showRegistrationForm() {
        loginContainer.setVisible(false);
        registrationContainer.setVisible(true);
        clearRegistrationError();
        Platform.runLater(() -> regUsernameField.requestFocus());
    }
    
    private void setLoginInProgress(boolean inProgress) {
        loginProgressIndicator.setVisible(inProgress);
        loginButton.setDisable(inProgress);
        usernameField.setDisable(inProgress);
        passwordField.setDisable(inProgress);
        showRegisterButton.setDisable(inProgress);
    }
    
    private void setRegistrationInProgress(boolean inProgress) {
        registrationProgressIndicator.setVisible(inProgress);
        registerButton.setDisable(inProgress);
        regUsernameField.setDisable(inProgress);
        regEmailField.setDisable(inProgress);
        regFullNameField.setDisable(inProgress);
        regPasswordField.setDisable(inProgress);
        regConfirmPasswordField.setDisable(inProgress);
        showLoginButton.setDisable(inProgress);
    }
    
    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void clearLoginError() {
        loginErrorLabel.setVisible(false);
        loginErrorLabel.setText("");
    }
    
    private void showRegistrationError(String message) {
        registrationErrorLabel.setText(message);
        registrationErrorLabel.setVisible(true);
        registrationErrorLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showRegistrationSuccess(String message) {
        registrationErrorLabel.setText(message);
        registrationErrorLabel.setVisible(true);
        registrationErrorLabel.setStyle("-fx-text-fill: green;");
    }
    
    private void clearRegistrationError() {
        registrationErrorLabel.setVisible(false);
        registrationErrorLabel.setText("");
    }
    
    private void clearLoginForm() {
        usernameField.clear();
        passwordField.clear();
        rememberMeCheckBox.setSelected(false);
    }
    
    private void clearRegistrationForm() {
        regUsernameField.clear();
        regEmailField.clear();
        regFullNameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        
        // Clear any validation styling
        regEmailField.setStyle("");
        regPasswordField.setStyle("");
        regConfirmPasswordField.setStyle("");
    }
    
    // Method to check if user is already logged in
    public void checkExistingSession() {
        if (userService != null && userService.isLoggedIn()) {
            if (userService.validateSession()) {
                // User has valid session, navigate to dashboard
                try {
                    SceneManager.getInstance().showScene("dashboard.fxml");
                } catch (Exception e) {
                    logger.error("Error navigating to dashboard", e);
                    // Stay on login screen if navigation fails
                }
            }
        }
    }
}