package com.expensesplitter.dao.impl;

import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryExpenseDAO implements ExpenseDAO {
    private final ConcurrentMap<Long, Expense> expenses = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public Expense save(Expense expense) {
        long id = idSeq.getAndIncrement();
        expense.setId(id);
        expenses.put(id, expense);
        return expense;
    }

    @Override
    public Expense update(Expense expense) {
        if (expense.getId() == null) {
            return save(expense);
        }
        expenses.put(expense.getId(), expense);
        return expense;
    }

    @Override
    public void delete(Long expenseId) {
        expenses.remove(expenseId);
    }

    @Override
    public Optional<Expense> findById(Long expenseId) {
        return Optional.ofNullable(expenses.get(expenseId));
    }

    @Override
    public List<Expense> findByGroupId(Long groupId) {
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByPaidByUserId(Long userId) {
        return expenses.values().stream()
                .filter(e -> userId.equals(e.getPaidByUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByGroupIdAndDateRange(Long groupId, LocalDateTime startDate, LocalDateTime endDate) {
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .filter(e -> !e.getExpenseDate().isBefore(startDate) && !e.getExpenseDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByGroupIdAndCategory(Long groupId, ExpenseCategory category) {
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .filter(e -> category.equals(e.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findRecentByUserId(Long userId, int limit) {
        return expenses.values().stream()
                .filter(e -> userId.equals(e.getPaidByUserId()))
                .sorted(Comparator.comparing(Expense::getExpenseDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> searchByDescription(Long groupId, String query) {
        String q = query.toLowerCase();
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .filter(e -> e.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByGroupIds(List<Long> groupIds) {
        return expenses.values().stream()
                .filter(e -> groupIds.contains(e.getGroupId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findSettledByGroupId(Long groupId) {
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .filter(Expense::isSettled)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findUnsettledByGroupId(Long groupId) {
        return expenses.values().stream()
                .filter(e -> groupId.equals(e.getGroupId()))
                .filter(e -> !e.isSettled())
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByIds(List<Long> expenseIds) {
        return expenseIds.stream()
                .map(expenses::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public long countByGroupId(Long groupId) {
        return expenses.values().stream().filter(e -> groupId.equals(e.getGroupId())).count();
    }

    @Override
    public List<Expense> findAll() {
        return new ArrayList<>(expenses.values());
    }
}