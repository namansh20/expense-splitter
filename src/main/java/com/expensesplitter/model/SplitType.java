package com.expensesplitter.model;

/**
 * Enum representing the different ways an expense can be split among group members.
 */
public enum SplitType {
    EQUAL("Equal Split", "Split equally among all participants"),
    PERCENTAGE("Percentage Split", "Split based on custom percentages"),
    CUSTOM("Custom Amount", "Custom amounts for each participant"),
    BY_SHARES("By Shares", "Split based on share ratios");

    private final String displayName;
    private final String description;

    SplitType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static SplitType fromString(String type) {
        for (SplitType splitType : SplitType.values()) {
            if (splitType.name().equalsIgnoreCase(type) || 
                splitType.displayName.equalsIgnoreCase(type)) {
                return splitType;
            }
        }
        throw new IllegalArgumentException("Unknown split type: " + type);
    }
}