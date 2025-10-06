package com.expensesplitter.dao.impl;

import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryUserDAO implements UserDAO {
    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public User save(User user) {
        long id = idSeq.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            return save(user);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> searchUsers(String query) {
        String q = query.toLowerCase();
        return users.values().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllActive() {
        return users.values().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<User> findByIds(List<Long> userIds) {
        return userIds.stream()
                .map(users::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}