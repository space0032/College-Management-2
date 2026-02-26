package com.college.test;

import com.college.dao.PermissionDAO;
import com.college.models.Permission;

public class DumpPermissions {
    public static void main(String[] args) {
        PermissionDAO dao = new PermissionDAO();
        for (Permission p : dao.getAllPermissions()) {
            System.out.println(p.getCode());
        }
    }
}
