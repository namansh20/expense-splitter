package com.expensesplitter.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a member of an expense group.
 * This class manages the relationship between users and groups.
 */
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private LocalDateTime joinedAt;
    private GroupMemberRole role;
    private boolean isActive;
    
    // For display purposes (not persisted)
    private String username;
    private String fullName;

    // Default constructor
    public GroupMember() {
        this.isActive = true;
        this.joinedAt = LocalDateTime.now();
        this.role = GroupMemberRole.MEMBER;
    }

    // Constructor with required fields
    public GroupMember(Long groupId, Long userId, GroupMemberRole role) {
        this();
        setGroupId(groupId);
        setUserId(userId);
        setRole(role);
    }

    // Full constructor
    public GroupMember(Long id, Long groupId, Long userId, LocalDateTime joinedAt,
                      GroupMemberRole role, boolean isActive) {
        this.id = id;
        setGroupId(groupId);
        setUserId(userId);
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
        setRole(role);
        this.isActive = isActive;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public GroupMemberRole getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    // Setters with validation
    public void setId(Long id) {
        this.id = id;
    }

    public void setGroupId(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        this.groupId = groupId;
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.userId = userId;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public void setRole(GroupMemberRole role) {
        this.role = role != null ? role : GroupMemberRole.MEMBER;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Business methods
    public void promoteToAdmin() {
        this.role = GroupMemberRole.ADMIN;
    }

    public void demoteToMember() {
        this.role = GroupMemberRole.MEMBER;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isAdmin() {
        return role == GroupMemberRole.ADMIN && isActive;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username != null ? username : "User " + userId;
    }

    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "Member";
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GroupMember that = (GroupMember) obj;
        return Objects.equals(id, that.id) ||
               (Objects.equals(groupId, that.groupId) && Objects.equals(userId, that.userId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", userId=" + userId +
                ", role=" + role +
                ", isActive=" + isActive +
                ", joinedAt=" + joinedAt +
                ", displayName='" + getDisplayName() + '\'' +
                '}';
    }

    // Builder pattern
    public static class Builder {
        private GroupMember member = new GroupMember();

        public Builder withId(Long id) {
            member.setId(id);
            return this;
        }

        public Builder withGroupId(Long groupId) {
            member.setGroupId(groupId);
            return this;
        }

        public Builder withUserId(Long userId) {
            member.setUserId(userId);
            return this;
        }

        public Builder withJoinedAt(LocalDateTime joinedAt) {
            member.setJoinedAt(joinedAt);
            return this;
        }

        public Builder withRole(GroupMemberRole role) {
            member.setRole(role);
            return this;
        }

        public Builder withActive(boolean active) {
            member.setActive(active);
            return this;
        }

        public Builder withUsername(String username) {
            member.setUsername(username);
            return this;
        }

        public Builder withFullName(String fullName) {
            member.setFullName(fullName);
            return this;
        }

        public GroupMember build() {
            if (member.groupId == null || member.userId == null) {
                throw new IllegalStateException("Group ID and User ID are required");
            }
            return member;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}