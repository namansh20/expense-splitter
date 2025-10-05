package com.expensesplitter.service;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseShare;
import com.expensesplitter.model.User;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting data in various formats (CSV, PDF, Excel).
 * Provides functionality to export expenses, reports, and user data.
 */
public class ExportService {
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Exports expenses to CSV format
     */
    public void exportExpensesToCSV(List<Expense> expenses, String filePath) throws IOException {
        logger.info("Exporting {} expenses to CSV: {}", expenses.size(), filePath);
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            // Write headers
            String[] headers = {
                "ID", "Description", "Amount", "Currency", "Category", 
                "Split Type", "Paid By", "Group ID", "Date", "Notes", "Settled"
            };
            csvWriter.writeNext(headers);
            
            // Write expense data
            for (Expense expense : expenses) {
                String[] data = {
                    expense.getId() != null ? expense.getId().toString() : "",
                    expense.getDescription(),
                    expense.getAmount().toString(),
                    expense.getCurrency(),
                    expense.getCategory() != null ? expense.getCategory().name() : "",
                    expense.getSplitType() != null ? expense.getSplitType().name() : "",
                    expense.getPaidByUserId() != null ? expense.getPaidByUserId().toString() : "",
                    expense.getGroupId() != null ? expense.getGroupId().toString() : "",
                    expense.getExpenseDate() != null ? expense.getExpenseDate().format(DATE_FORMAT) : "",
                    expense.getNotes() != null ? expense.getNotes() : "",
                    Boolean.toString(expense.isSettled())
                };
                csvWriter.writeNext(data);
            }
        }
        
        logger.info("Successfully exported expenses to CSV: {}", filePath);
    }
    
    /**
     * Exports expense shares to CSV format
     */
    public void exportExpenseSharesToCSV(List<ExpenseShare> shares, String filePath) throws IOException {
        logger.info("Exporting {} expense shares to CSV: {}", shares.size(), filePath);
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            // Write headers
            String[] headers = {
                "ID", "Expense ID", "User ID", "Share Amount", "Percentage", 
                "Is Paid", "Paid Date", "Created Date"
            };
            csvWriter.writeNext(headers);
            
            // Write share data
            for (ExpenseShare share : shares) {
                String[] data = {
                    share.getId() != null ? share.getId().toString() : "",
                    share.getExpenseId() != null ? share.getExpenseId().toString() : "",
                    share.getUserId() != null ? share.getUserId().toString() : "",
                    share.getShareAmount().toString(),
                    share.getPercentage() != null ? share.getPercentage().toString() : "",
                    Boolean.toString(share.isPaid()),
                    share.getPaidAt() != null ? share.getPaidAt().format(DATE_FORMAT) : "",
                    share.getCreatedAt() != null ? share.getCreatedAt().format(DATE_FORMAT) : ""
                };
                csvWriter.writeNext(data);
            }
        }
        
        logger.info("Successfully exported expense shares to CSV: {}", filePath);
    }
    
    /**
     * Exports user balance report to CSV
     */
    public void exportBalanceReportToCSV(List<ExpenseService.UserBalance> balances, 
                                        List<User> users, String filePath) throws IOException {
        logger.info("Exporting balance report to CSV: {}", filePath);
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            // Write headers
            String[] headers = {
                "User ID", "Username", "Full Name", "Total Owed", "Total Paid", 
                "Net Balance", "Status"
            };
            csvWriter.writeNext(headers);
            
            // Create user lookup map
            var userMap = users.stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
            
            // Write balance data
            for (ExpenseService.UserBalance balance : balances) {
                User user = userMap.get(balance.getUserId());
                String username = user != null ? user.getUsername() : "Unknown";
                String fullName = user != null ? user.getFullName() : "Unknown";
                
                String status;
                if (balance.isOwed()) {
                    status = "Owes Money";
                } else if (balance.owesMore()) {
                    status = "Is Owed Money";
                } else {
                    status = "Even";
                }
                
                String[] data = {
                    balance.getUserId().toString(),
                    username,
                    fullName,
                    balance.getTotalOwed().toString(),
                    balance.getTotalPaid().toString(),
                    balance.getNetBalance().toString(),
                    status
                };
                csvWriter.writeNext(data);
            }
        }
        
        logger.info("Successfully exported balance report to CSV: {}", filePath);
    }
    
    /**
     * Generates a simple text report
     */
    public void generateTextReport(List<Expense> expenses, 
                                  List<ExpenseService.UserBalance> balances,
                                  String filePath) throws IOException {
        logger.info("Generating text report: {}", filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("EXPENSE SPLITTER REPORT");
            writer.println("========================");
            writer.println("Generated on: " + java.time.LocalDateTime.now().format(DATE_FORMAT));
            writer.println();
            
            // Summary section
            writer.println("SUMMARY");
            writer.println("-------");
            writer.println("Total Expenses: " + expenses.size());
            
            BigDecimal totalAmount = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            writer.println("Total Amount: $" + totalAmount.toString());
            
            long settledExpenses = expenses.stream()
                    .mapToLong(expense -> expense.isSettled() ? 1 : 0)
                    .sum();
            writer.println("Settled Expenses: " + settledExpenses);
            writer.println("Pending Expenses: " + (expenses.size() - settledExpenses));
            writer.println();
            
            // User balances section
            writer.println("USER BALANCES");
            writer.println("-------------");
            for (ExpenseService.UserBalance balance : balances) {
                writer.printf("User %d: Net Balance $%.2f%n", 
                        balance.getUserId(), balance.getNetBalance());
            }
            writer.println();
            
            // Recent expenses section
            writer.println("RECENT EXPENSES");
            writer.println("---------------");
            expenses.stream()
                    .limit(10)
                    .forEach(expense -> {
                        writer.printf("%s - $%.2f (%s) - %s%n",
                                expense.getExpenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                expense.getAmount(),
                                expense.getCurrency(),
                                expense.getDescription());
                    });
        }
        
        logger.info("Successfully generated text report: {}", filePath);
    }
    
    /**
     * Exports debt settlements to CSV
     */
    public void exportDebtSettlementsToCSV(List<ExpenseSplittingService.DebtSettlement> settlements,
                                          String filePath) throws IOException {
        logger.info("Exporting {} debt settlements to CSV: {}", settlements.size(), filePath);
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            // Write headers
            String[] headers = {"From User ID", "To User ID", "Amount"};
            csvWriter.writeNext(headers);
            
            // Write settlement data
            for (ExpenseSplittingService.DebtSettlement settlement : settlements) {
                String[] data = {
                    settlement.getFromUserId().toString(),
                    settlement.getToUserId().toString(),
                    settlement.getAmount().toString()
                };
                csvWriter.writeNext(data);
            }
        }
        
        logger.info("Successfully exported debt settlements to CSV: {}", filePath);
    }
    
    /**
     * Gets the default export directory
     */
    public String getDefaultExportDirectory() {
        String userHome = System.getProperty("user.home");
        String exportDir = userHome + File.separator + "ExpenseSplitterExports";
        
        // Create directory if it doesn't exist
        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return exportDir;
    }
    
    /**
     * Generates a unique filename with timestamp
     */
    public String generateFilename(String prefix, String extension) {
        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
    
    /**
     * Validates if the file path is writable
     */
    public boolean isPathWritable(String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            
            if (parentDir != null && !parentDir.exists()) {
                return parentDir.mkdirs();
            }
            
            return parentDir != null && parentDir.canWrite();
        } catch (Exception e) {
            logger.error("Error checking if path is writable: " + filePath, e);
            return false;
        }
    }
}