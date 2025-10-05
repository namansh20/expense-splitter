# Expense Splitter Java Application

A comprehensive desktop application built with Java and JavaFX for splitting expenses among groups, featuring data visualization, database persistence, and scheduled tasks.

## Features

- **Multi-user Support**: Create and manage multiple users
- **Group Management**: Create groups and manage members with admin/member roles
- **Expense Tracking**: Add, edit, and delete expenses with categories
- **Flexible Splitting**: Support for equal, percentage, and custom splits
- **Balance Calculation**: Real-time calculation of who owes what to whom
- **Settlement Suggestions**: Smart suggestions for settling group balances
- **Data Visualization**: Charts showing expense breakdowns over time and by category
- **CSV Import/Export**: Export data for external analysis or backup
- **Database Persistence**: MySQL database for reliable data storage
- **Scheduled Tasks**: Automatic backups and notifications
- **Modern UI**: Clean JavaFX interface with intuitive navigation

## Technology Stack

- **Java 17+**: Modern Java features and improved performance
- **JavaFX 21**: Rich desktop UI framework
- **MySQL 8.0+**: Robust database for data persistence
- **JDBC**: Direct database connectivity with HikariCP connection pooling
- **JFreeChart**: Professional data visualization and charting
- **Maven**: Dependency management and build automation
- **SLF4J + Logback**: Comprehensive logging framework
- **JUnit 5**: Unit testing framework

## Architecture

The application follows a layered architecture pattern:

```
src/main/java/com/expensesplitter/
├── model/          # Domain objects (User, Group, Expense, etc.)
├── dao/            # Data Access Objects for database operations
├── service/        # Business logic and services
├── ui/             # JavaFX UI components
│   ├── controller/ # FXML controllers
│   ├── view/       # Custom UI components
│   └── chart/      # Chart components
├── util/           # Utility classes
└── config/         # Configuration management
```

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- MySQL 8.0 or higher
- Maven 3.8 or higher
- Git

## Database Setup

1. Install MySQL and create a database:
   ```sql
   CREATE DATABASE expense_splitter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Create a database user:
   ```sql
   CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
   GRANT ALL PRIVILEGES ON expense_splitter.* TO 'expense_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Run the database schema script:
   ```bash
   mysql -u expense_user -p expense_splitter < sql/01_create_database.sql
   ```

4. (Optional) Insert sample data:
   ```bash
   mysql -u expense_user -p expense_splitter < sql/02_insert_sample_data.sql
   ```

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd expense-splitter-java
   ```

2. **Configure database connection**:
   Edit `src/main/resources/application.properties` to match your database settings:
   ```properties
   database.url=jdbc:mysql://localhost:3306/expense_splitter
   database.username=expense_user
   database.password=expense_password
   ```

3. **Install dependencies**:
   ```bash
   mvn clean install
   ```

4. **Run the application**:
   ```bash
   mvn javafx:run
   ```

   Or alternatively:
   ```bash
   mvn clean compile exec:java
   ```

## Building for Distribution

1. **Create executable JAR**:
   ```bash
   mvn clean package
   ```

2. **Run the packaged JAR**:
   ```bash
   java -jar target/expense-splitter-java-1.0.0-jar-with-dependencies.jar
   ```

## Running Tests

Execute all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=ExpenseSplittingServiceTest
```

## Configuration

The application can be configured through `src/main/resources/application.properties`:

- **Database settings**: Connection URL, credentials, pool settings
- **UI preferences**: Theme, window size, colors
- **Export settings**: Default directories, CSV format options
- **Scheduled tasks**: Backup intervals, notification settings
- **Security**: Password requirements, session timeouts

## Usage Guide

### Getting Started
1. Launch the application
2. Create user accounts for group members
3. Create expense groups and add members
4. Start adding and tracking expenses

### Managing Expenses
- **Add Expense**: Click "Add Expense" and fill in details
- **Split Types**: Choose between equal, percentage, or custom splits
- **Categories**: Organize expenses by categories (food, transport, etc.)
- **Settlements**: Record payments between group members

### Viewing Reports
- **Dashboard**: Overview of your groups and balances
- **Charts**: Visual breakdown by category and time
- **Balance Summary**: See who owes what to whom
- **Export Data**: Download expense data as CSV

## Development

### Project Structure
- `/src/main/java`: Application source code
- `/src/test/java`: Unit tests
- `/src/main/resources`: Configuration files and assets
- `/sql`: Database scripts
- `/docs`: Additional documentation

### Code Style
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Document public APIs with Javadoc
- Write unit tests for business logic

### Contributing
1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database exists and user has proper permissions

**JavaFX Module Issues**
- Ensure Java 17+ is installed
- Verify JavaFX modules are properly configured
- Check Maven JavaFX plugin settings

**Application Won't Start**
- Check log files in `/logs` directory
- Verify all dependencies are installed
- Ensure database schema is created

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please create an issue in the project repository.

---

**Version**: 1.0.0  
**Last Updated**: March 2024