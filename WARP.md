# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Common Development Commands

### Building and Running
```bash
# Clean and install dependencies
mvn clean install

# Run the application (primary method)
mvn javafx:run

# Alternative run method
mvn clean compile exec:java

# Create executable JAR
mvn clean package

# Run the packaged JAR
java -jar target/expense-splitter-java-1.0.0-jar-with-dependencies.jar
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ExpenseSplittingServiceTest

# Run tests with coverage (if configured)
mvn clean test jacoco:report
```

### Database Operations
```bash
# Initialize database schema
mysql -u expense_user -p expense_splitter < sql/01_create_database.sql

# Load sample data
mysql -u expense_user -p expense_splitter < sql/02_insert_sample_data.sql

# Connect to database for manual queries
mysql -u expense_user -p expense_splitter
```

### Development Utilities
```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Generate project reports
mvn site

# Clean up generated files
mvn clean

# Compile without running tests
mvn compile -DskipTests
```

## Architecture Overview

### Technology Stack
- **Java 17+**: Modern Java with records, pattern matching, text blocks
- **JavaFX 21**: Desktop UI framework with FXML-based scenes
- **Maven**: Build automation and dependency management
- **MySQL 8.0+**: Primary database with HikariCP connection pooling
- **SLF4J + Logback**: Structured logging framework
- **JFreeChart**: Data visualization and charting

### Core Architecture Patterns
- **Layered Architecture**: Clear separation between model, DAO, service, and UI layers
- **Singleton Pattern**: Used for DatabaseConfig and SceneManager utilities
- **Builder Pattern**: Implemented in domain models (User, ExpenseGroup)
- **MVC Pattern**: FXML controllers manage UI logic, services handle business logic

### Package Structure
```
com.expensesplitter/
├── model/              # Domain objects and entities
│   ├── User.java       # User entity with validation and builders
│   ├── ExpenseGroup.java
│   ├── GroupMember.java
│   ├── ExpenseCategory.java
│   └── SplitType.java
├── dao/                # Data Access Objects (not yet implemented)
├── service/            # Business logic services (not yet implemented)  
├── ui/                 # JavaFX UI components
│   ├── controller/     # FXML controllers (not yet implemented)
│   ├── view/           # Custom UI components (not yet implemented)
│   └── chart/          # Chart components (not yet implemented)
├── config/             # Configuration management
│   └── DatabaseConfig.java  # Singleton database configuration
└── util/               # Utility classes
    └── SceneManager.java     # JavaFX scene management singleton
```

### Database Design
- **Normalized schema** with proper foreign key relationships
- **HikariCP connection pooling** for optimal performance
- **MySQL-specific optimizations** (prepared statement caching, batch operations)
- **Database views** for complex queries (user_balances view)
- **Proper indexing** on frequently queried columns

### Key Architectural Decisions
1. **JavaFX Scene Management**: SceneManager singleton handles navigation and scene caching
2. **Database Connection Pooling**: HikariCP manages connections efficiently
3. **Configuration Management**: Properties-based configuration with fallback defaults
4. **Domain Model Validation**: Input validation at the model level with meaningful exceptions
5. **Defensive Programming**: Null checks, immutable collections, and proper error handling

## Configuration

### Database Configuration
Edit `src/main/resources/application.properties`:
```properties
database.url=jdbc:mysql://localhost:3306/expense_splitter?useSSL=false&serverTimezone=UTC
database.username=expense_user
database.password=expense_password
```

### UI and Application Settings
Key configuration properties:
- `ui.theme=light` - UI theme selection
- `ui.default.width=1200` - Default window width
- `currency.default=USD` - Default currency
- `dev.mode=false` - Development mode toggle

## Development Guidelines

### Working with Models
- Domain models include comprehensive validation in setters
- Use Builder pattern for complex object creation
- Models implement proper equals(), hashCode(), and toString() methods
- Defensive copying used for collections and date objects

### Database Development
- Use HikariCP connection pooling through DatabaseConfig singleton
- Always close connections properly (use try-with-resources)
- Database schema changes should be versioned in sql/ directory
- Follow existing naming conventions (snake_case for database, camelCase for Java)

### JavaFX Development  
- FXML files should be placed in `src/main/resources/fxml/`
- CSS stylesheets go in `src/main/resources/css/`
- Use SceneManager for navigation between scenes
- Controllers should be lightweight, business logic belongs in service classes

### Testing Strategy
- Unit tests for models and utility classes
- Service layer tests with H2 in-memory database
- Integration tests for database operations
- Mock external dependencies (database connections, file system)

## Important Notes

### Database Dependencies
- Requires MySQL 8.0+ running locally or remotely
- Database schema must be initialized before first run
- Connection pool settings can be tuned in application.properties

### JavaFX Runtime
- Requires Java 17+ with JavaFX modules
- Application uses JavaFX Maven plugin for execution
- Icon files should be placed in `src/main/resources/icons/`

### Logging Configuration
- Uses SLF4J with Logback implementation  
- Log files written to `logs/expense-splitter.log`
- Debug level logging enabled for application packages
- Log rotation configured (10MB max size, 30 days retention)

### Performance Considerations
- Scene caching implemented in SceneManager for faster navigation
- Database connection pooling prevents connection overhead
- MySQL-specific query optimizations enabled in HikariCP
- PreparedStatement caching enabled for better performance