package com.expensesplitter.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for managing JavaFX scenes and navigation.
 * This class follows the Singleton pattern and provides methods
 * for switching between different scenes in the application.
 */
public class SceneManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private static SceneManager instance;
    
    private Stage primaryStage;
    private Map<String, Scene> sceneCache;
    private Scene currentScene;
    
    // Default scene dimensions
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;

    private SceneManager() {
        sceneCache = new HashMap<>();
    }

    /**
     * Get the singleton instance of SceneManager.
     * @return SceneManager instance
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Set the primary stage for the application.
     * @param primaryStage The primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Switch to a scene identified by the FXML filename.
     * @param fxmlFileName The FXML file name (e.g., "dashboard.fxml")
     * @throws IOException if the FXML file cannot be loaded
     */
    public void switchToScene(String fxmlFileName) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not set");
        }

        Scene scene = getScene(fxmlFileName);
        primaryStage.setScene(scene);
        currentScene = scene;
        
        logger.info("Switched to scene: {}", fxmlFileName);
    }

    /**
     * Switch to a scene with custom dimensions.
     * @param fxmlFileName The FXML file name
     * @param width Scene width
     * @param height Scene height
     * @throws IOException if the FXML file cannot be loaded
     */
    public void switchToScene(String fxmlFileName, double width, double height) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not set");
        }

        Scene scene = getScene(fxmlFileName, width, height);
        primaryStage.setScene(scene);
        currentScene = scene;
        
        // Resize the stage if necessary
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();
        
        logger.info("Switched to scene: {} ({}x{})", fxmlFileName, width, height);
    }

    /**
     * Get or create a scene from FXML file.
     * @param fxmlFileName The FXML file name
     * @return The Scene object
     * @throws IOException if the FXML file cannot be loaded
     */
    public Scene getScene(String fxmlFileName) throws IOException {
        return getScene(fxmlFileName, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Get or create a scene from FXML file with custom dimensions.
     * @param fxmlFileName The FXML file name
     * @param width Scene width
     * @param height Scene height
     * @return The Scene object
     * @throws IOException if the FXML file cannot be loaded
     */
    public Scene getScene(String fxmlFileName, double width, double height) throws IOException {
        String cacheKey = fxmlFileName + "_" + width + "x" + height;
        
        // Check cache first
        Scene cachedScene = sceneCache.get(cacheKey);
        if (cachedScene != null) {
            logger.debug("Retrieved cached scene: {}", fxmlFileName);
            return cachedScene;
        }

        // Load FXML file
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/" + fxmlFileName));
        
        if (loader.getLocation() == null) {
            // Try alternative path
            loader.setLocation(getClass().getResource("/" + fxmlFileName));
        }
        
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: " + fxmlFileName);
        }

        try {
            Pane root = loader.load();
            Scene scene = new Scene(root, width, height);
            
            // Apply CSS stylesheets if available
            applyStylesheets(scene);
            
            // Cache the scene for future use
            sceneCache.put(cacheKey, scene);
            
            logger.debug("Created new scene: {}", fxmlFileName);
            return scene;
            
        } catch (IOException e) {
            logger.error("Failed to load FXML file: {}", fxmlFileName, e);
            throw e;
        }
    }

    /**
     * Apply CSS stylesheets to the scene.
     * @param scene The scene to apply stylesheets to
     */
    private void applyStylesheets(Scene scene) {
        try {
            // Try to load the main application stylesheet
            String mainStylesheet = Objects.requireNonNull(
                getClass().getResource("/css/application.css")).toExternalForm();
            scene.getStylesheets().add(mainStylesheet);
            logger.debug("Applied main stylesheet to scene");
        } catch (Exception e) {
            logger.warn("Main stylesheet not found or could not be applied", e);
        }

        try {
            // Try to load theme-specific stylesheet
            String themeStylesheet = Objects.requireNonNull(
                getClass().getResource("/css/light-theme.css")).toExternalForm();
            scene.getStylesheets().add(themeStylesheet);
            logger.debug("Applied theme stylesheet to scene");
        } catch (Exception e) {
            logger.debug("Theme stylesheet not found, using default styling");
        }
    }

    /**
     * Get the FXML loader for a specific file.
     * This method allows access to the controller after loading.
     * @param fxmlFileName The FXML file name
     * @return FXMLLoader instance
     * @throws IOException if the FXML file cannot be found
     */
    public FXMLLoader getFXMLLoader(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/" + fxmlFileName));
        
        if (loader.getLocation() == null) {
            loader.setLocation(getClass().getResource("/" + fxmlFileName));
        }
        
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: " + fxmlFileName);
        }
        
        return loader;
    }

    /**
     * Load a scene and return both the scene and its controller.
     * @param fxmlFileName The FXML file name
     * @return SceneControllerPair containing both scene and controller
     * @throws IOException if the FXML file cannot be loaded
     */
    public <T> SceneControllerPair<T> loadSceneWithController(String fxmlFileName) throws IOException {
        return loadSceneWithController(fxmlFileName, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Load a scene with custom dimensions and return both the scene and its controller.
     * @param fxmlFileName The FXML file name
     * @param width Scene width
     * @param height Scene height
     * @return SceneControllerPair containing both scene and controller
     * @throws IOException if the FXML file cannot be loaded
     */
    public <T> SceneControllerPair<T> loadSceneWithController(String fxmlFileName, 
            double width, double height) throws IOException {
        FXMLLoader loader = getFXMLLoader(fxmlFileName);
        Pane root = loader.load();
        Scene scene = new Scene(root, width, height);
        
        applyStylesheets(scene);
        
        T controller = loader.getController();
        
        return new SceneControllerPair<>(scene, controller);
    }

    /**
     * Clear the scene cache to free up memory.
     */
    public void clearCache() {
        sceneCache.clear();
        logger.info("Scene cache cleared");
    }

    /**
     * Get the current active scene.
     * @return The current scene, or null if no scene is active
     */
    public Scene getCurrentScene() {
        return currentScene;
    }

    /**
     * Get the primary stage.
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Check if a scene is cached.
     * @param fxmlFileName The FXML file name
     * @return true if the scene is cached, false otherwise
     */
    public boolean isSceneCached(String fxmlFileName) {
        String cacheKey = fxmlFileName + "_" + DEFAULT_WIDTH + "x" + DEFAULT_HEIGHT;
        return sceneCache.containsKey(cacheKey);
    }

    /**
     * Get the number of cached scenes.
     * @return Number of cached scenes
     */
    public int getCacheSize() {
        return sceneCache.size();
    }

    /**
     * Inner class to hold both Scene and Controller instances.
     */
    public static class SceneControllerPair<T> {
        private final Scene scene;
        private final T controller;

        public SceneControllerPair(Scene scene, T controller) {
            this.scene = scene;
            this.controller = controller;
        }

        public Scene getScene() {
            return scene;
        }

        public T getController() {
            return controller;
        }
    }
}