package com.expensesplitter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration and connection management using HikariCP connection pool.
 * This class follows the Singleton pattern to ensure single database configuration.
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private HikariDataSource dataSource;
    private Properties properties;

    private DatabaseConfig() {
        loadProperties();
        initializeDataSource();
    }

    /**
     * Get the singleton instance of DatabaseConfig.
     * @return DatabaseConfig instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Load application properties from the configuration file.
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                logger.warn("Unable to find application.properties file");
                setDefaultProperties();
                return;
            }
            
            properties.load(input);
            logger.info("Application properties loaded successfully");
            
        } catch (IOException e) {
            logger.error("Error loading application properties", e);
            setDefaultProperties();
        }
    }

    /**
     * Set default database properties if configuration file is not found.
     */
    private void setDefaultProperties() {
        properties.setProperty("database.url", 
            "jdbc:mysql://localhost:3306/expense_splitter?useSSL=false&serverTimezone=UTC");
        properties.setProperty("database.username", "expense_user");
        properties.setProperty("database.password", "expense_password");
        properties.setProperty("database.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("database.pool.maximum-pool-size", "10");
        properties.setProperty("database.pool.minimum-idle", "2");
        properties.setProperty("database.pool.connection-timeout", "30000");
        properties.setProperty("database.pool.idle-timeout", "600000");
        properties.setProperty("database.pool.max-lifetime", "1800000");
        
        logger.info("Using default database properties");
    }

    /**
     * Initialize HikariCP data source with configuration properties.
     */
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Basic database connection settings
            config.setJdbcUrl(properties.getProperty("database.url"));
            config.setUsername(properties.getProperty("database.username"));
            config.setPassword(properties.getProperty("database.password"));
            config.setDriverClassName(properties.getProperty("database.driver"));
            
            // Connection pool settings
            config.setMaximumPoolSize(
                Integer.parseInt(properties.getProperty("database.pool.maximum-pool-size", "10")));
            config.setMinimumIdle(
                Integer.parseInt(properties.getProperty("database.pool.minimum-idle", "2")));
            config.setConnectionTimeout(
                Long.parseLong(properties.getProperty("database.pool.connection-timeout", "30000")));
            config.setIdleTimeout(
                Long.parseLong(properties.getProperty("database.pool.idle-timeout", "600000")));
            config.setMaxLifetime(
                Long.parseLong(properties.getProperty("database.pool.max-lifetime", "1800000")));
            
            // Additional HikariCP optimizations
            config.setPoolName("ExpenseSplitterConnectionPool");
            config.setConnectionTestQuery("SELECT 1");
            config.setLeakDetectionThreshold(60000);
            
            // MySQL specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP data source initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Get a database connection from the connection pool.
     * @return Database connection
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Initialize the database by creating tables if they don't exist.
     * This method can be called during application startup.
     */
    public void initializeDatabase() {
        logger.info("Initializing database...");
        
        try (Connection connection = getConnection()) {
            // Test the connection
            if (connection.isValid(5)) {
                logger.info("Database connection is valid");
            } else {
                throw new SQLException("Database connection is not valid");
            }
            
            // Additional initialization logic can be added here
            // such as running database migration scripts
            
        } catch (SQLException e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
    }

    /**
     * Close the database connection pool.
     * This method should be called during application shutdown.
     */
    public void closeDatabase() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Get a property value from the configuration.
     * @param key Property key
     * @return Property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get a property value with a default fallback.
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get an integer property value.
     * @param key Property key
     * @param defaultValue Default value if property is not found or invalid
     * @return Integer property value
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get a boolean property value.
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Boolean property value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Check if the data source is initialized and available.
     * @return true if data source is available, false otherwise
     */
    public boolean isDataSourceAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Get connection pool statistics for monitoring.
     * @return Connection pool statistics as a formatted string
     */
    public String getConnectionPoolStats() {
        if (dataSource == null) {
            return "DataSource not initialized";
        }
        
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Total: %d, Pending: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}