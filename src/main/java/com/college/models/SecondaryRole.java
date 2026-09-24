package com.college.models;

/**
 * Lightweight view of a secondary role attached to a user.
 * Used in API payloads where only the role identity is needed
 * (id/code/name) — not the full permission tree.
 */
public class SecondaryRole {
    private int id;
    private String code;
    private String name;

    public SecondaryRole() {
    }

    public SecondaryRole(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

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
}