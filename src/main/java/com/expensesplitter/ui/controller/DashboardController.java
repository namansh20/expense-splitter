package com.expensesplitter.ui.controller;

import com.expensesplitter.model.*;
import com.expensesplitter.service.*;
import com.expensesplitter.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the main dashboard screen that shows expense overview,
 * recent activities, charts, and quick actions.
 */
public class DashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @FXML private Label welcomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label totalOwedLabel;
    @FXML private Label totalLentLabel;
    @FXML private Label netBalanceLabel;
    
    @FXML private TableView<RecentExpenseItem> recentExpensesTable;
    @FXML private TableColumn<RecentExpenseItem, String> expenseDescriptionColumn;
    @FXML private TableColumn<RecentExpenseItem, String> expenseAmountColumn;
    @FXML private TableColumn<RecentExpenseItem, String> expenseDateColumn;
    @FXML private TableColumn<RecentExpenseItem, String> expenseGroupColumn;
    @FXML private TableColumn<RecentExpenseItem, String> expenseStatusColumn;
    
    @FXML private TableView<DebtItem> debtsTable;
    @FXML private TableColumn<DebtItem, String> debtUserColumn;
    @FXML private TableColumn<DebtItem, String> debtAmountColumn;
    @FXML private TableColumn<DebtItem, String> debtStatusColumn;
    
    @FXML private PieChart expenseByCategoryChart;
    @FXML private BarChart<String, Number> monthlyExpensesChart;
    
    @FXML private ListView<String> notificationsList;
    @FXML private VBox quickActionsBox;
    
    @FXML private Button addExpenseButton;
    @FXML private Button createGroupButton;
    @FXML private Button settleDebtsButton;
    @FXML private Button viewReportsButton;
    
    private UserService userService;
    private ExpenseService expenseService;
    private GroupService groupService;
    
    private User currentUser;
    
    public void setServices(UserService userService, ExpenseService expenseService, GroupService groupService) {
        this.userService = userService;
        this.expenseService = expenseService;
        this.groupService = groupService;
    }
    
    @Override
public void initialize(URL location, ResourceBundle resources) {
        // Lazy-wire services from AppContext if not set
        if (this.userService == null || this.expenseService == null) {
            var ctx = com.expensesplitter.config.AppContext.getInstance();
            this.userService = ctx.getUserService();
            this.expenseService = ctx.getExpenseService();
        }
        setupTableColumns();
        setupButtonActions();
    }
    
    public void initializeData() {
        this.currentUser = userService.getCurrentUser().orElse(null);
        if (currentUser == null) {
logger.warn("No current user found, redirecting to login");
try {
    SceneManager.getInstance().switchToScene("login.fxml");
} catch (Exception ex) {
    logger.error("Error navigating to login scene", ex);
}
            return;
        }
        
        loadDashboardData();
    }
    
    private void setupTableColumns() {
        // Recent expenses table
        expenseDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        expenseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        expenseGroupColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        expenseStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Debts table
        debtUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        debtAmountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        debtStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    private void setupButtonActions() {
        addExpenseButton.setOnAction(e -> handleAddExpense());
        createGroupButton.setOnAction(e -> handleCreateGroup());
        settleDebtsButton.setOnAction(e -> handleSettleDebts());
        viewReportsButton.setOnAction(e -> handleViewReports());
    }
    
    private void loadDashboardData() {
        try {
            // Update welcome message
            welcomeLabel.setText("Welcome back, " + currentUser.getDisplayName() + "!");
            
            // Load financial summary
            loadFinancialSummary();
            
            // Load recent expenses
            loadRecentExpenses();
            
            // Load debts and credits
            loadDebtsAndCredits();
            
            // Load charts
            loadExpenseByCategoryChart();
            loadMonthlyExpensesChart();
            
            // Load notifications
            loadNotifications();
            
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            showError("Error loading dashboard data: " + e.getMessage());
        }
    }
    
    private void loadFinancialSummary() {
        ExpenseService.UserBalance userBalance = expenseService.getUserBalance(currentUser.getId());
        
        totalOwedLabel.setText(String.format("$%.2f", userBalance.getTotalOwed()));
        totalLentLabel.setText(String.format("$%.2f", userBalance.getTotalPaid()));
        
        BigDecimal netBalance = userBalance.getNetBalance();
        netBalanceLabel.setText(String.format("$%.2f", netBalance.abs()));
        
        // Color coding for net balance
        if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
            netBalanceLabel.setStyle("-fx-text-fill: green;");
            netBalanceLabel.setText("+" + netBalanceLabel.getText());
        } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
            netBalanceLabel.setStyle("-fx-text-fill: red;");
            netBalanceLabel.setText("-" + netBalanceLabel.getText());
        } else {
            netBalanceLabel.setStyle("-fx-text-fill: black;");
        }
        
        // Calculate total expenses (this would require additional service method)
        List<Expense> userExpenses = expenseService.getExpensesByUser(currentUser.getId());
        BigDecimal totalExpenses = userExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
    }
    
    private void loadRecentExpenses() {
        List<Expense> recentExpenses = expenseService.getRecentExpenses(currentUser.getId(), 10);
        
        ObservableList<RecentExpenseItem> expenseItems = FXCollections.observableArrayList();
        
        for (Expense expense : recentExpenses) {
            // This would require additional service method to get group name
            String groupName = "Group"; // Placeholder
            String status = expense.isSettled() ? "Settled" : "Pending";
            
            expenseItems.add(new RecentExpenseItem(
                expense.getDescription(),
                expense.getFormattedAmount(),
                expense.getExpenseDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                groupName,
                status
            ));
        }
        
        recentExpensesTable.setItems(expenseItems);
    }
    
    private void loadDebtsAndCredits() {
        List<ExpenseShare> userShares = expenseService.getUserExpenseShares(currentUser.getId());
        
        ObservableList<DebtItem> debtItems = FXCollections.observableArrayList();
        
        // Group unpaid shares by expense and create debt items
        Map<Long, List<ExpenseShare>> sharesByExpense = userShares.stream()
                .filter(share -> !share.isPaid())
                .collect(Collectors.groupingBy(ExpenseShare::getExpenseId));
        
        for (Map.Entry<Long, List<ExpenseShare>> entry : sharesByExpense.entrySet()) {
            Long expenseId = entry.getKey();
            List<ExpenseShare> shares = entry.getValue();
            
            Optional<Expense> expenseOpt = expenseService.getExpenseById(expenseId);
            if (expenseOpt.isPresent()) {
                Expense expense = expenseOpt.get();
                
                for (ExpenseShare share : shares) {
                    if (!share.getUserId().equals(currentUser.getId())) {
                        continue; // Skip shares for other users
                    }
                    
                    // Get the user who paid
                    String payerName = "Unknown User"; // Placeholder - would need UserService call
                    
                    debtItems.add(new DebtItem(
                        payerName,
                        String.format("$%.2f", share.getShareAmount()),
                        "Pending"
                    ));
                }
            }
        }
        
        debtsTable.setItems(debtItems);
    }
    
    private void loadExpenseByCategoryChart() {
        List<Expense> userExpenses = expenseService.getExpensesByUser(currentUser.getId());
        
        Map<ExpenseCategory, BigDecimal> categoryTotals = userExpenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.mapping(Expense::getAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<ExpenseCategory, BigDecimal> entry : categoryTotals.entrySet()) {
            pieChartData.add(new PieChart.Data(
                entry.getKey().getDisplayName(),
                entry.getValue().doubleValue()));
        }
        
        expenseByCategoryChart.setData(pieChartData);
    }
    
    private void loadMonthlyExpensesChart() {
        List<Expense> userExpenses = expenseService.getExpensesByUser(currentUser.getId());
        
        // Group expenses by month
        Map<String, BigDecimal> monthlyTotals = userExpenses.stream()
                .collect(Collectors.groupingBy(
                    expense -> expense.getExpenseDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    Collectors.mapping(Expense::getAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Expenses");
        
        for (Map.Entry<String, BigDecimal> entry : monthlyTotals.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue()));
        }
        
        monthlyExpensesChart.getData().clear();
        monthlyExpensesChart.getData().add(series);
    }
    
    private void loadNotifications() {
        ObservableList<String> notifications = FXCollections.observableArrayList();
        
        // Add some sample notifications - in real implementation, this would come from a service
        notifications.add("You owe $25.00 to John for Dinner");
        notifications.add("Sarah paid you back $15.50");
        notifications.add("New expense added to 'Trip to Vegas' group");
        notifications.add("Monthly report is ready for download");
        
        notificationsList.setItems(notifications);
    }
    
    // Button handlers
    private void handleAddExpense() {
        try {
SceneManager.getInstance().switchToScene("add-expense.fxml");
        } catch (Exception e) {
            logger.error("Error opening add expense screen", e);
            showError("Error opening add expense screen");
        }
    }
    
    private void handleCreateGroup() {
        try {
SceneManager.getInstance().switchToScene("create-group.fxml");
        } catch (Exception e) {
            logger.error("Error opening create group screen", e);
            showError("Error opening create group screen");
        }
    }
    
    private void handleSettleDebts() {
        try {
SceneManager.getInstance().switchToScene("settle-debts.fxml");
        } catch (Exception e) {
            logger.error("Error opening settle debts screen", e);
            showError("Error opening settle debts screen");
        }
    }
    
    private void handleViewReports() {
        try {
SceneManager.getInstance().switchToScene("reports.fxml");
        } catch (Exception e) {
            logger.error("Error opening reports screen", e);
            showError("Error opening reports screen");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner classes for table data
    public static class RecentExpenseItem {
        private final String description;
        private final String formattedAmount;
        private final String formattedDate;
        private final String groupName;
        private final String status;
        
        public RecentExpenseItem(String description, String formattedAmount, String formattedDate, String groupName, String status) {
            this.description = description;
            this.formattedAmount = formattedAmount;
            this.formattedDate = formattedDate;
            this.groupName = groupName;
            this.status = status;
        }
        
        // Getters
        public String getDescription() { return description; }
        public String getFormattedAmount() { return formattedAmount; }
        public String getFormattedDate() { return formattedDate; }
        public String getGroupName() { return groupName; }
        public String getStatus() { return status; }
    }
    
    public static class DebtItem {
        private final String userName;
        private final String formattedAmount;
        private final String status;
        
        public DebtItem(String userName, String formattedAmount, String status) {
            this.userName = userName;
            this.formattedAmount = formattedAmount;
            this.status = status;
        }
        
        // Getters
        public String getUserName() { return userName; }
        public String getFormattedAmount() { return formattedAmount; }
        public String getStatus() { return status; }
    }
    
    // Interface for GroupService (placeholder - would be implemented separately)
    public interface GroupService {
        // Group service methods would be defined here
    }
}