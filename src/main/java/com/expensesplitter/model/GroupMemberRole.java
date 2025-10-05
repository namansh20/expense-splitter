package com.expensesplitter.model;

/**
 * Enum representing the role of a member in an expense group.
 */
public enum GroupMemberRole {
    ADMIN("Admin"),
    MEMBER("Member");

    private final String displayName;

    GroupMemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static GroupMemberRole fromString(String role) {
        for (GroupMemberRole memberRole : GroupMemberRole.values()) {
            if (memberRole.name().equalsIgnoreCase(role) || 
                memberRole.displayName.equalsIgnoreCase(role)) {
                return memberRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}