package com.expensesplitter.config;

import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.ExpenseShareDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.dao.impl.InMemoryExpenseDAO;
import com.expensesplitter.dao.impl.InMemoryExpenseShareDAO;
import com.expensesplitter.dao.impl.InMemoryUserDAO;
import com.expensesplitter.model.ExpenseCategory;
import com.expensesplitter.model.SplitType;
import com.expensesplitter.service.CurrencyService;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.ExpenseSplittingService;
import com.expensesplitter.service.ExportService;
import com.expensesplitter.service.UserService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Application-wide context and dependency container.
 * Provides initialized DAO and service singletons and seeds demo data.
 */
public class AppContext {

    private static AppContext instance;

    // DAOs
    private final UserDAO userDAO;
    private final ExpenseDAO expenseDAO;
    private final ExpenseShareDAO expenseShareDAO;

    // Services
    private final ExpenseSplittingService splittingService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final CurrencyService currencyService;
    private final ExportService exportService;

    private AppContext() {
        // Initialize in-memory DAOs for a working out-of-the-box setup
        this.userDAO = new InMemoryUserDAO();
        this.expenseDAO = new InMemoryExpenseDAO();
        this.expenseShareDAO = new InMemoryExpenseShareDAO();

        // Initialize services
        this.splittingService = new ExpenseSplittingService();
        this.expenseService = new ExpenseService(expenseDAO, expenseShareDAO, splittingService);
        this.userService = new UserService(userDAO);
        this.currencyService = new CurrencyService();
        this.exportService = new ExportService();

        // Seed demo data for quick start
        seedDemoData();
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public UserService getUserService() { return userService; }
    public ExpenseService getExpenseService() { return expenseService; }
    public ExpenseSplittingService getSplittingService() { return splittingService; }
    public CurrencyService getCurrencyService() { return currencyService; }
    public ExportService getExportService() { return exportService; }

    public UserDAO getUserDAO() { return userDAO; }
    public ExpenseDAO getExpenseDAO() { return expenseDAO; }
    public ExpenseShareDAO getExpenseShareDAO() { return expenseShareDAO; }

    private void seedDemoData() {
        try {
            // Create demo users
            var demo = userService.registerUser("demo", "demo@example.com", "Demo User", "password");
            var friend = userService.registerUser("friend", "friend@example.com", "Friend User", "password");

            Long groupId = 1L; // simple demo group id

            // Create some demo expenses
            expenseService.createExpense(
                    groupId,
                    demo.getId(),
                    "Dinner at Bistro",
                    new BigDecimal("100.00"),
                    "USD",
                    new ExpenseCategory("Food"),
                    SplitType.EQUAL,
                    Arrays.asList(demo.getId(), friend.getId()),
                    java.util.Collections.emptyMap(),
                    "Friday night dinner"
            );

            expenseService.createExpense(
                    groupId,
                    friend.getId(),
                    "Cab Ride",
                    new BigDecimal("40.00"),
                    "USD",
                    new ExpenseCategory("Transport"),
                    SplitType.EQUAL,
                    Arrays.asList(demo.getId(), friend.getId()),
                    java.util.Collections.emptyMap(),
                    "Airport to hotel"
            );
        } catch (Exception ignored) {
            // If seeding fails, continue without demo data
        }
    }
}