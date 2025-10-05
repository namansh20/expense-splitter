package com.expensesplitter.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a group of users who share expenses.
 * This class manages group information and member relationships.
 */
public class ExpenseGroup {
    private Long id;
    private String groupName;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private List<GroupMember> members;

    // Default constructor
    public ExpenseGroup() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.members = new ArrayList<>();
    }

    // Constructor with required fields
    public ExpenseGroup(String groupName, Long createdBy) {
        this();
        setGroupName(groupName);
        setCreatedBy(createdBy);
    }

    // Full constructor
    public ExpenseGroup(Long id, String groupName, String description, Long createdBy,
                       LocalDateTime createdAt, LocalDateTime updatedAt, boolean isActive) {
        this.id = id;
        setGroupName(groupName);
        setDescription(description);
        setCreatedBy(createdBy);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.isActive = isActive;
        this.members = new ArrayList<>();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<GroupMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    // Setters with validation
    public void setId(Long id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }
        if (groupName.length() > 100) {
            throw new IllegalArgumentException("Group name cannot exceed 100 characters");
        }
        this.groupName = groupName.trim();
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        this.description = description != null ? description.trim() : null;
    }

    public void setCreatedBy(Long createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
    }

    // Business methods
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public void addMember(GroupMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        if (!members.contains(member)) {
            members.add(member);
            updateTimestamp();
        }
    }

    public void removeMember(GroupMember member) {
        if (members.remove(member)) {
            updateTimestamp();
        }
    }

    public boolean hasMember(Long userId) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getUserId(), userId) && member.isActive());
    }

    public GroupMember getMember(Long userId) {
        return members.stream()
                .filter(member -> Objects.equals(member.getUserId(), userId))
                .findFirst()
                .orElse(null);
    }

    public List<GroupMember> getActiveMembers() {
        return members.stream()
                .filter(GroupMember::isActive)
                .toList();
    }

    public List<GroupMember> getAdminMembers() {
        return members.stream()
                .filter(member -> member.isActive() && member.getRole() == GroupMemberRole.ADMIN)
                .toList();
    }

    public boolean isUserAdmin(Long userId) {
        GroupMember member = getMember(userId);
        return member != null && member.isActive() && member.getRole() == GroupMemberRole.ADMIN;
    }

    public int getActiveMemberCount() {
        return (int) members.stream()
                .filter(GroupMember::isActive)
                .count();
    }

    public String getDisplayName() {
        return groupName;
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ExpenseGroup that = (ExpenseGroup) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupName);
    }

    @Override
    public String toString() {
        return "ExpenseGroup{" +
                "id=" + id +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy=" + createdBy +
                ", isActive=" + isActive +
                ", memberCount=" + getActiveMemberCount() +
                ", createdAt=" + createdAt +
                '}';
    }

    // Builder pattern
    public static class Builder {
        private ExpenseGroup group = new ExpenseGroup();

        public Builder withId(Long id) {
            group.setId(id);
            return this;
        }

        public Builder withGroupName(String groupName) {
            group.setGroupName(groupName);
            return this;
        }

        public Builder withDescription(String description) {
            group.setDescription(description);
            return this;
        }

        public Builder withCreatedBy(Long createdBy) {
            group.setCreatedBy(createdBy);
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            group.setCreatedAt(createdAt);
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            group.setUpdatedAt(updatedAt);
            return this;
        }

        public Builder withActive(boolean active) {
            group.setActive(active);
            return this;
        }

        public Builder withMembers(List<GroupMember> members) {
            group.setMembers(members);
            return this;
        }

        public ExpenseGroup build() {
            if (group.groupName == null || group.createdBy == null) {
                throw new IllegalStateException("Group name and created by are required");
            }
            return group;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}