package com.college.models;

/**
 * Permission Model for RBAC
 */
public class Permission {
    private int id;
    private String code;
    private String name;
    private String category;
    private String description;

    public Permission() {
    }

    public Permission(int id, String code, String name, String category) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            return this.code.equals(((Permission) obj).code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
