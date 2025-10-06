package com.expensesplitter.dao.impl;

import com.expensesplitter.dao.ExpenseShareDAO;
import com.expensesplitter.model.ExpenseShare;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryExpenseShareDAO implements ExpenseShareDAO {
    private final ConcurrentMap<Long, ExpenseShare> shares = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public ExpenseShare save(ExpenseShare expenseShare) {
        long id = idSeq.getAndIncrement();
        expenseShare.setId(id);
        shares.put(id, expenseShare);
        return expenseShare;
    }

    @Override
    public ExpenseShare update(ExpenseShare expenseShare) {
        if (expenseShare.getId() == null) {
            return save(expenseShare);
        }
        shares.put(expenseShare.getId(), expenseShare);
        return expenseShare;
    }

    @Override
    public void delete(Long shareId) {
        shares.remove(shareId);
    }

    @Override
    public void deleteByExpenseId(Long expenseId) {
        shares.values().removeIf(s -> expenseId.equals(s.getExpenseId()));
    }

    @Override
    public Optional<ExpenseShare> findById(Long shareId) {
        return Optional.ofNullable(shares.get(shareId));
    }

    @Override
    public List<ExpenseShare> findByExpenseId(Long expenseId) {
        return shares.values().stream()
                .filter(s -> expenseId.equals(s.getExpenseId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseShare> findByUserId(Long userId) {
        return shares.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExpenseShare> findByExpenseIdAndUserId(Long expenseId, Long userId) {
        return shares.values().stream()
                .filter(s -> expenseId.equals(s.getExpenseId()) && userId.equals(s.getUserId()))
                .findFirst();
    }

    @Override
    public List<ExpenseShare> findUnpaidByUserId(Long userId) {
        return shares.values().stream()
                .filter(s -> userId.equals(s.getUserId()) && !s.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseShare> findPaidByUserId(Long userId) {
        return shares.values().stream()
                .filter(s -> userId.equals(s.getUserId()) && s.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseShare> findByExpenseIds(List<Long> expenseIds) {
        return shares.values().stream()
                .filter(s -> expenseIds.contains(s.getExpenseId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseShare> findByExpenseIdAndUserIds(Long expenseId, List<Long> userIds) {
        return shares.values().stream()
                .filter(s -> expenseId.equals(s.getExpenseId()) && userIds.contains(s.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public long countUnpaidByUserId(Long userId) {
        return shares.values().stream()
                .filter(s -> userId.equals(s.getUserId()) && !s.isPaid())
                .count();
    }

    @Override
    public List<ExpenseShare> findAll() {
        return new ArrayList<>(shares.values());
    }
}