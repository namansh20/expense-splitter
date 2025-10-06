package com.expensesplitter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expensesplitter.config.DatabaseConfig;
import com.expensesplitter.util.SceneManager;

import java.io.IOException;
import java.util.Objects;

/**
 * Main JavaFX Application class for the Expense Splitter application.
 * This class manages the application lifecycle and initial setup.
 */
public class ExpenseSplitterApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpenseSplitterApplication.class);
    private static final String APP_TITLE = "Expense Splitter";
    private static final String APP_VERSION = "1.0.0";
    
    private Stage primaryStage;
    private SceneManager sceneManager;

    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing Expense Splitter Application v{}", APP_VERSION);
        
        try {
            // Initialize database connection
            DatabaseConfig.getInstance().initializeDatabase();
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            setupPrimaryStage();
            initializeSceneManager();
            loadInitialScene();
            
            primaryStage.show();
            logger.info("Application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }

    private void setupPrimaryStage() {
        primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Center the window
        primaryStage.centerOnScreen();
        
        // Set application icon
        try {
            Image icon = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icons/app-icon.png")));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Could not load application icon", e);
        }
        
        // Handle window close event
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application closing...");
            cleanup();
        });
    }

    private void initializeSceneManager() {
        sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);
    }

    private void loadInitialScene() throws IOException {
        // Check if user is logged in (implement authentication logic)
        boolean isUserLoggedIn = checkUserAuthentication();
        
if (isUserLoggedIn) {
            sceneManager.switchToScene("dashboard.fxml");
        } else {
            sceneManager.switchToScene("login.fxml");
        }
    }

private boolean checkUserAuthentication() {
        try {
            return com.expensesplitter.config.AppContext.getInstance()
                    .getUserService().isLoggedIn();
        } catch (Exception e) {
            return false;
        }
    }

    private void showErrorAndExit(String message) {
        logger.error("Critical error: {}", message);
        // TODO: Show error dialog to user
        System.err.println("Critical Error: " + message);
        System.exit(1);
    }

    private void cleanup() {
        try {
            // Close database connections
            DatabaseConfig.getInstance().closeDatabase();
            logger.info("Database connections closed");
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        cleanup();
        super.stop();
    }

    /**
     * Get the primary stage of the application.
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Get the scene manager instance.
     * @return the scene manager
     */
    public SceneManager getSceneManager() {
        return sceneManager;
    }

    /**
     * Main method to launch the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Expense Splitter Application...");
        
        // Set system properties for better JavaFX experience
        System.setProperty("javafx.preloader", "com.expensesplitter.ui.AppPreloader");
        System.setProperty("prism.lcdtext", "false");
        
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Failed to launch application", e);
            System.err.println("Failed to launch application: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Get application information.
     * @return application info string
     */
    public static String getApplicationInfo() {
        return APP_TITLE + " v" + APP_VERSION;
    }

    /**
     * Get application version.
     * @return version string
     */
    public static String getVersion() {
        return APP_VERSION;
    }
}