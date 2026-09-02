package com.college.utils;

import com.college.models.Permission;
import com.college.models.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest {

    @Test
    void administratorIsGrantedNewPermissionsWithoutAnExplicitMapping() {
        Role admin = new Role();
        admin.setCode("ADMIN");

        assertTrue(PermissionService.grantsPermission(admin, "CREATE_LIBRARY"));
    }

    @Test
    void regularRoleStillRequiresAnExplicitPermission() {
        Role faculty = new Role();
        faculty.setCode("FACULTY");
        Permission permission = new Permission();
        permission.setCode("VIEW_STUDENT");
        faculty.addPermission(permission);

        assertTrue(PermissionService.grantsPermission(faculty, "VIEW_STUDENT"));
        assertFalse(PermissionService.grantsPermission(faculty, "CREATE_STUDENT"));
    }
}
