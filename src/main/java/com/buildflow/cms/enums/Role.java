package com.buildflow.cms.enums;

public enum Role {
    ADMIN("Admin"),
    PROJECT_MANAGER("Project Manager"),
    SITE_ENGINEER("Site Engineer"),
    CONTRACTOR("Contractor"),
    SUPPLIER("Supplier"),
    CLIENT("Client");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
